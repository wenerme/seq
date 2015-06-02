package me.wener.seq.internal;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Strings;
import javax.annotation.concurrent.Immutable;
import me.wener.seq.SequenceMeta;
import me.wener.seq.internal.util.Option;
import me.wener.seq.internal.util.Options;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class Metas
{
    private final SequenceMeta asc = new ImmutableSequenceMeta("", 1, Long.MAX_VALUE, 1, 1000, false, false);
    private final SequenceMeta desc = new ImmutableSequenceMeta("", -1, Long.MIN_VALUE, 1, 1000, false, false);

    private Metas() { }

    public static <T extends SequenceMeta> T check(T m)
    {
        checkNotNull(m, "Meta is null");
        checkArgument(!Strings.isNullOrEmpty(m.getName()), "No name");
        checkArgument(m.getMaxValue() > m.getMinValue(), "Max < Min");
        checkArgument(m.getCache() < 1, "Cache must >= 1");
        checkArgument(m.getIncrement() != 0, "Increment is zero");
        checkArgument(m.getCache() < Math.ceil(m.getMaxValue() - m.getMinValue()) / Math.abs(m.getIncrement()),
                "Cache must < (CEIL (MAXVALUE - MINVALUE)) / ABS (INCREMENT)");
        return m;
    }

    public static SequenceMeta fromOption(Option o)
    {
        Metas.MutableSequenceMeta meta = new Metas.MutableSequenceMeta();
        meta.setName(o.get("name"))
            .setCache(o.get("cache", 1000))
            .setCycle(o.get("cycle", false))
            .setIncrement(o.get("increment", 1))
            .setMaxValue(o.get("max", Long.MAX_VALUE))
            .setMinValue(o.get("min", 1))
            .setOrder(o.get("order", false))
        ;
        return meta;
    }

    public static SequenceMeta immutable(SequenceMeta meta)
    {
        if (meta instanceof ImmutableSequenceMeta)
        {
            return meta;
        }
        return new ImmutableSequenceMeta(meta);
    }

    public static Option toOption(SequenceMeta meta)
    {
        Option o = Options.create();
        o.set("name", meta.getName())
         .set("cache", meta.getCache())
         .set("cycle", meta.isCycle())
         .set("increment", meta.getIncrement())
         .set("max", meta.getMaxValue())
         .set("min", meta.getMinValue())
         .set("order", meta.isOrder())
        ;
        return o;
    }

    /**
     * @author <a href="http://github.com/wenerme">wener</a>
     */
    private static class MutableSequenceMeta extends AbstractSequenceMeta
    {
        private String name;
        private long minValue;
        private long maxValue;
        private long increment;
        private long cache;
        private boolean cycle;
        private boolean order;

        public String getName()
        {
            return name;
        }

        public MutableSequenceMeta setName(String name)
        {
            this.name = name;
            return this;
        }

        public long getMinValue()
        {
            return minValue;
        }

        public MutableSequenceMeta setMinValue(long minValue)
        {
            this.minValue = minValue;
            return this;
        }

        public long getMaxValue()
        {
            return maxValue;
        }

        public MutableSequenceMeta setMaxValue(long maxValue)
        {
            this.maxValue = maxValue;
            return this;
        }

        public long getIncrement()
        {
            return increment;
        }

        public MutableSequenceMeta setIncrement(long increment)
        {
            this.increment = increment;
            return this;
        }

        public long getCache()
        {
            return cache;
        }

        public MutableSequenceMeta setCache(long cache)
        {
            this.cache = cache;
            return this;
        }

        public boolean isCycle()
        {
            return cycle;
        }

        public MutableSequenceMeta setCycle(boolean cycle)
        {
            this.cycle = cycle;
            return this;
        }

        public boolean isOrder()
        {
            return order;
        }

        public MutableSequenceMeta setOrder(boolean order)
        {
            this.order = order;
            return this;
        }
    }

    /**
     * @author <a href="http://github.com/wenerme">wener</a>
     */
    @Immutable
    private static class ImmutableSequenceMeta extends AbstractSequenceMeta
    {
        private final String name;
        private final long minValue;
        private final long maxValue;
        private final long increment;
        private final long cache;
        private final boolean cycle;
        private final boolean order;

        public ImmutableSequenceMeta(String name, long minValue, long maxValue, long increment, long cache, boolean cycle, boolean order)
        {
            this.name = name;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.increment = increment;
            this.cache = cache;
            this.cycle = cycle;
            this.order = order;
        }

        public ImmutableSequenceMeta(SequenceMeta o)
        {
            this(o.getName(), o.getMinValue(), o.getMaxValue(), o.getIncrement(), o.getCache(), o.isCycle(), o
                    .isOrder());
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public long getMinValue()
        {
            return minValue;
        }

        @Override
        public long getMaxValue()
        {
            return maxValue;
        }

        @Override
        public long getIncrement()
        {
            return increment;
        }

        @Override
        public long getCache()
        {
            return cache;
        }

        @Override
        public boolean isCycle()
        {
            return cycle;
        }

        @Override
        public boolean isOrder()
        {
            return order;
        }
    }
}
