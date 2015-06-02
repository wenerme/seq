package me.wener.seq.internal;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.util.concurrent.atomic.AtomicLong;
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
            // TODO
            return null;
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
            return null;
        }
    }

    public static Supplier<Long> create0(final Supplier<Long> src, final long minVal, final long maxVal, final long inc, final long cache, final boolean cycle)
    {
        check(minVal, maxVal, inc, cache, cycle);

        Supplier<Long> r = src;
        if (cache > 1)
        {
            r = new Scale(r, cache);
        }
        long min = minVal;
        long max = maxVal;
        if (inc != 1)
        {
            /*
            基本逻辑,将区间转换为正区间,将递减转换为递增逻辑
             */
            long step = inc > 0 ? inc : -inc;
            long offset = 0;
            if (inc < 0)
            {
                long t = max;
                if (min == Long.MIN_VALUE)
                {
                    max = Long.MAX_VALUE;
                } else
                {
                    max = -min;
                }
                min = -t;
            }
            if (min < 0)
            {
                offset = inc < 0 ? -min : min;
                min = 0;
                if (max != Long.MAX_VALUE)
                {
                    max += -offset;
                }
            }
            // 以下的 min 和 max 均为正数

            // 距离归约
            if (max != Long.MAX_VALUE && (max - min) % inc != 0)
            {
                long gap = (max - min) % inc;
                max -= gap;
            }

            if (min % step != 0)
            {
                // 对齐最小值
                long gap = min % step;
                offset += gap;
                min -= gap;
                min /= step;
                if (max != Long.MAX_VALUE)
                {
                    max -= gap;
                    max /= step;
                }
            } else
            {
                min /= step;
                if (max != Long.MAX_VALUE)
                {
                    // 对齐最大值时不需要 offset
                    max /= step;
                }
            }


            r = Suppliers.compose(new AscRange(min, max, cycle), r);
            r = Suppliers.compose(new Multiplication(inc), r);
            if (offset != 0)
            {
                r = Suppliers.compose(new Addition(offset), r);
            }
        } else
        {
            r = Suppliers.compose(new AscRange(min, max, cycle), r);
        }

        return r;
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

    /**
     * 范围归约
     */
    private static class LongAscSequence implements Supplier<Long>
    {
        final Supplier<Long> source;
        long min;
        long max;
        long inc;
        long distance;
        boolean cycle;
        Long c, n;
        long margin;

        /*
        boolean needsFlush = false;
        if ((increment > 0 && value >= valueWithMargin) ||
                (increment < 0 && value <= valueWithMargin)) {
            valueWithMargin += increment * cacheSize;
            needsFlush = true;
        }
        if ((increment > 0 && value > maxValue) ||
                (increment < 0 && value < minValue)) {
            if (cycle) {
                value = increment > 0 ? minValue : maxValue;
                valueWithMargin = value + (increment * cacheSize);
                needsFlush = true;
            } else {
                throw DbException.get(ErrorCode.SEQUENCE_EXHAUSTED, getName());
            }
        }
        if (needsFlush) {
            flush(session);
        }
        long v = value;
        value += increment;
        return v;
         */
        private LongAscSequence(Supplier<Long> source, long min, long max, long inc, boolean cycle)
        {
            this.source = source;
            this.min = min;
            this.max = max;
            this.inc = inc;
            this.cycle = cycle;
            this.distance = max - min + 1;
        }

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

        @Override
        public Long get()
        {
            if (c == null || c > margin)
            {
                c = source.get() * inc + min;
                margin = c + inc + min;
            }

            return null;
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
