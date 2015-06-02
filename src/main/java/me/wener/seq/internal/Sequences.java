package me.wener.seq.internal;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import me.wener.seq.SequenceException;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class Sequences
{

    private static final Multiplication FUNC_NEG = new Multiplication(-1);

    private Sequences()
    {
    }

    public static Supplier<Long> supplier(AtomicLong v)
    {
        return new AtomicSupplier(v);
    }

    public static void check(long min, long max, long inc, long cache, boolean cycle)
    {
        checkArgument(cache >= 1, "Cache must >= 1");
        checkArgument(inc != 0, "Increment must != 0");
        checkArgument(max > min, "Max <= Min");
        checkArgument(!(max == Long.MAX_VALUE && min == Long.MIN_VALUE), "No max and min");

        if (inc < 0)
        {
            checkArgument(max != Long.MAX_VALUE, "Ascending without max");
        } else
        {
            checkArgument(min != Long.MIN_VALUE, "Descending without min");
        }

        if (min == Long.MIN_VALUE)
        {
            checkArgument(inc < 0, "Unbound descending minvalue require increment < 0");
            checkArgument(!cycle, "Unbound descending with cycle");
        }
        if (max == Long.MAX_VALUE)
        {
            checkArgument(inc > 0, "Unbound ascending maxvalue require increment > 0");
            checkArgument(!cycle, "Unbound ascending with cycle");
        }
    }

    /**
     * Create with a atomic long
     */
    public static Supplier<Long> create(long min, long max, long inc, long cache, boolean cycle)
    {
        return create(supplier(new AtomicLong()), min, max, inc, cache, cycle);
    }

    private static <T, F> Supplier<T> compose(Supplier<F> supplier, Function<? super F, T> function)
    {
        return Suppliers.compose(function, supplier);
    }

    private static <A, B, C> Supplier<C> compose(Supplier<A> supplier, Function<A, ? extends B> a, Function<B, C> b)
    {
        return Suppliers.compose(MoreFunctions.compose(a, b), supplier);
    }

    public static Supplier<Long> create(final Supplier<Long> src, final long minVal, final long maxVal, final long inc, final long cache, final boolean cycle)
    {
        check(minVal, maxVal, inc, cache, cycle);
        Supplier<Long> r = src;
        if (cache > 1)
        {
            r = new Scale(r, cache);
        }

        int sig = Long.signum(inc);
        long min = minVal;
        long max = maxVal;
        if (sig > 0)
        {
            if (inc == 1)
            {
                if (maxVal == Long.MAX_VALUE)
                {
                    if (minVal == 0)
                    {
                        return r;
                    } else
                    {
                        return compose(r, new Addition(minVal));
                    }
                }
                return compose(r, new AscRange(minVal, maxVal, cycle));
            }

            if (maxVal == Long.MAX_VALUE)
            {
                if (minVal == 0)
                {
                    return compose(r, new Multiplication(inc));
                } else
                {
                    return compose(r, new Factor(inc, minVal));
                }
            }
            return compose(r, new LongAscSequence(minVal, maxVal, inc, cycle));
        } else
        {
            if (inc == -1)
            {
                if (minVal == Long.MIN_VALUE)
                {
                    if (maxVal == 0)
                    {
                        return compose(r, FUNC_NEG);
                    } else
                    {
                        return compose(r, new Factor(-1, maxVal));
                    }
                }
                return compose(r, FUNC_NEG, new DescRange(minVal, maxVal, cycle));
            }

            if (minVal == Long.MIN_VALUE)
            {
                if (maxVal == 0)
                {
                    return Suppliers.compose(new Multiplication(inc), r);
                } else
                {
                    return Suppliers.compose(new Factor(inc, maxVal), r);
                }
            }
            return compose(r, new LongDescSequence(minVal, maxVal, inc, cycle));
        }
    }

    @NotThreadSafe
    private static class Scale implements Supplier<Long>
    {
        final Supplier<Long> original;
        final long scale;
        Long v, c;
        Long margin;

        private Scale(Supplier<Long> original, long scale)
        {
            this.original = original;
            this.scale = scale;
        }

        @Override
        public Long get()
        {
            if (v == null || v >= margin)
            {
                v = original.get() * scale;
                margin = v + scale;
            }
            c = v;
            v++;
            return c;
        }
    }

    @ThreadSafe
    private static class AscRange implements Function<Long, Long>
    {
        final long min;
        final long max;
        final long distance;
        final boolean cycle;

        private AscRange(long min, long max, boolean cycle)
        {
            this.min = min;
            this.max = max;
            this.cycle = cycle;
            this.distance = max - min + 1;
        }

        @Override
        public Long apply(final Long v)
        {
            long c = v + min;
            if (c > max)
            {
                if (cycle)
                {
                    c = v % distance + min;
                } else
                {
                    throw new SequenceException("No more value, exceed the max");
                }
            }
            return c;
        }
    }

    @ThreadSafe
    private static class DescRange implements Function<Long, Long>
    {
        final long min;
        final long max;
        final long distance;
        final boolean cycle;

        private DescRange(long min, long max, boolean cycle)
        {
            this.min = min;
            this.max = max;
            this.cycle = cycle;
            this.distance = max - min + 1;
        }

        @Override
        public Long apply(final Long v)
        {
            long c = v + max;
            if (c < min)
            {
                if (cycle)
                {
                    c = v % distance + max;
                } else
                {
                    throw new SequenceException("No more value, exceed the max");
                }
            }
            return c;
        }
    }

    private static class LongAscSequence implements Function<Long, Long>
    {
        final long min;
        final long max;
        final long inc;
        final long num;
        final boolean cycle;
        volatile boolean more = true;

        private LongAscSequence(long min, long max, long inc, boolean cycle)
        {
            this.min = min;
            this.max = max - min;// 计算 max 的适合从 0 开始
            this.inc = inc;
            this.cycle = cycle;
            long d = max - min + 1;
            num = d % inc == 0 ? d / inc : d / inc + 1;
        }


        @Nullable
        @Override
        public Long apply(final Long c)
        {
            if (!more)
            {
                throw new SequenceException("No more value");
            }
            Long v = c * inc;
            if (cycle)
            {
                if (v > max)
                {
                    v = (c % num) * inc;
                }
            } else if (v + inc > max)
            {
                more = false;
            }

            return v + min;
        }
    }

    private static class LongDescSequence implements Function<Long, Long>
    {
        final long min;
        final long max;
        final long inc;
        final long num;
        final boolean cycle;
        volatile boolean more = true;

        private LongDescSequence(long min, long max, long inc, boolean cycle)
        {
            this.min = min - max;
            this.max = max;
            this.inc = inc;
            this.cycle = cycle;
            long d = Math.abs(max - min) + 1;
            num = d % inc == 0 ? d / Math.abs(inc) : d / Math.abs(inc) + 1;
        }


        @Nullable
        @Override
        public Long apply(final Long c)
        {
            if (!more)
            {
                throw new SequenceException("No more value");
            }
            Long v = c * inc;
            if (cycle)
            {
                if (v < min)
                {
                    v = (c % num) * inc;
                }
            } else if (v + inc < min)
            {
                more = false;
            }

            return v + max;
        }
    }

    private static class AtomicSupplier implements Supplier<Long>
    {
        private final AtomicLong v;

        private AtomicSupplier(AtomicLong v) {this.v = v;}

        @Override
        public Long get()
        {
            return v.getAndIncrement();
        }
    }

    private static class Multiplication implements Function<Long, Long>
    {
        final long val;

        private Multiplication(long val)
        {
            this.val = val;
        }

        @Override
        public Long apply(final Long v)
        {
            return v * val;
        }
    }

    private static class Factor implements Function<Long, Long>
    {
        final long a, b;

        private Factor(long a, long b)
        {
            this.a = a;
            this.b = b;
        }

        @Override
        public Long apply(final Long v)
        {
            return v * a + b;
        }
    }

    private static class Addition implements Function<Long, Long>
    {
        final long val;

        private Addition(long val)
        {
            this.val = val;
        }

        @Override
        public Long apply(final Long v)
        {
            return v + val;
        }
    }
}
