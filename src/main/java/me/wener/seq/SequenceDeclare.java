package me.wener.seq;

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import me.wener.seq.internal.Sequences;

import java.util.Map;

import static com.google.common.base.CharMatcher.inRange;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author wener
 * @since 15/11/21
 */
@SuppressWarnings("unused")
public class SequenceDeclare {
    public final static long NO_MAX = Long.MAX_VALUE;
    public final static long NO_MIN = Long.MIN_VALUE;
    public final static long NO_CACHE = 0;
    private static final CharMatcher NAME_MATCHER = inRange('a', 'z').or(inRange('A', 'Z')).or(inRange('0', '9')).or(CharMatcher.anyOf("_-"));
    private final static Config DEFAULT_CONFIG = asc().name("DEFAULT").build().toConfig();
    private final String name;
    private final long max;
    private final long min;
    private final long increment;
    private final boolean cycle;
    private final int cache;
    private final boolean order;
    private final Config attributes;


    private SequenceDeclare(Builder builder) {
        name = builder.name;
        max = builder.max;
        min = builder.min;
        increment = builder.increment;
        cycle = builder.cycle;
        cache = builder.cache;
        order = builder.order;
        attributes = builder.attributes;
        check();
    }

    /**
     * Default ascending sequence declare
     */
    public static Builder asc() {
        return new Builder().name("asc").cache(1).increment(1).max(NO_MAX).min(1).order(false);
    }

    /**
     * Default descending sequence declare
     */
    public static Builder desc() {
        return new Builder().name("desc").cache(1).increment(-1).max(-1).min(Long.MIN_VALUE).order(false);
    }

    /**
     * Get the builder form config which generated by {@link #toConfig()}
     */
    public static Builder from(final Config cfg) {
        Config c = cfg.withFallback(DEFAULT_CONFIG).getConfig("seq");
        return new Builder()
                .min(c.getLong("min"))
                .max(c.getLong("max"))
                .cycle(c.getBoolean("cycle"))
                .increment(c.getInt("increment"))
                .order(c.getBoolean("order"))
                .cache(c.getInt("cache"))
                .name(c.getString("name"));
    }

    /**
     * Get the builder form string which generated by {@link #toString()}
     */
    public static Builder from(String s) {
        return from(ConfigFactory.parseString(s));
    }

    public Config toConfig() {
        Map<String, Object> attrs = Maps.newHashMap();
        attrs.put("min", min);
        attrs.put("max", max);
        attrs.put("cycle", cycle);
        attrs.put("increment", increment);
        attrs.put("order", order);
        attrs.put("cache", cache);
        attrs.put("name", name);
        return ConfigFactory.parseMap(ImmutableMap.of("seq", attrs)).withFallback(attributes);
    }


    /**
     * Specify the interval between sequence numbers. This integer value
     * can be any positive or negative integer, but it cannot be 0.
     * This value can have 28 or fewer digits. The absolute of this value
     * must be less than the difference of MAXVALUE and MINVALUE. If this
     * value is negative, then the sequence descends. If the value is
     * positive, then the sequence ascends. If you omit this clause, then
     * the interval defaults to 1.
     */
    public long getIncrement() {
        return increment;
    }

    /**
     * Specify how many values of the sequence the database preallocates
     * and keeps in memory for faster access. This integer value can have
     * 28 or fewer digits. The minimum value for this parameter is 2. For
     * sequences that cycle, this value must be less than the number of
     * values in the cycle. You cannot cache more values than will fit in
     * a given cycle of sequence numbers. Therefore, the maximum value allowed
     * for CACHE must be less than the value determined by the following
     * formula:
     * <p>
     * <code>(CEIL (MAXVALUE - MINVALUE)) / ABS (INCREMENT)</code>
     * <p>
     * If a system failure occurs, then all cached sequence values that
     * have not been used in committed DML statements are lost. The potential
     * number of lost values is equal to the value of the CACHE parameter.
     * <p>
     * <b>注意</b>该值与具体实现相关
     */
    public long getCache() {
        return cache;
    }

    /**
     * Specify CYCLE to indicate that the sequence continues to generate values
     * after reaching either its maximum or minimum value. After an ascending
     * sequence reaches its maximum value, it generates its minimum value.
     * After a descending sequence reaches its minimum, it generates its maximum
     * value.
     * <p>
     * <b>注意</b>该值与具体实现相关
     */
    public boolean isCycle() {
        return cycle;
    }

    /**
     * @return 递增序列
     */
    public boolean isAscending() {
        return getIncrement() > 0;
    }

    /**
     * @return 递减序列
     */
    public boolean isDescending() {
        return getIncrement() < 0;
    }

    /**
     * @return 序列是否有限的
     */
    private boolean isUnBounded() {
        if (isAscending()) {
            return getMax() == Long.MAX_VALUE;
        } else if (isDescending()) {
            return getMin() == Long.MIN_VALUE;
        } else {
            throw new AssertionError("Can not determinate asc or desc");
        }
    }

    private void check() {
        checkArgument(!Strings.isNullOrEmpty(name), "name is null or empty");
        checkArgument(NAME_MATCHER.matchesAllOf(name), "Bad name format");
        Sequences.check(min, max, increment, cache, cycle);
    }

    public String getName() {
        return name;
    }

    /**
     * Specify the maximum value the sequence can generate. This integer
     * value can have 28 or fewer digits. MAXVALUE must be equal to or
     * greater than START WITH and must be greater than MINVALUE.
     */
    public long getMax() {
        return max;
    }

    /**
     * Specify the minimum value of the sequence. This integer value can have
     * 28 or fewer digits. MINVALUE must be less than or equal to START WITH
     * and must be less than MAXVALUE.
     */
    public long getMin() {
        return min;
    }

    /**
     * Specify ORDER to guarantee that sequence numbers are generated in order
     * of request. This clause is useful if you are using the sequence numbers
     * as timestamps. Guaranteeing order is usually not important for sequences
     * used to generate primary keys.
     * <p>
     * ORDER is necessary only to guarantee ordered generation if you are using
     * Oracle Real Application Clusters. If you are using exclusive mode, then
     * sequence numbers are always generated in order.
     * <p>
     * <b>注意</b>该值与具体实现相关
     */
    public boolean isOrder() {
        return order;
    }

    public Config getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SequenceDeclare that = (SequenceDeclare) o;
        return max == that.max &&
                min == that.min &&
                increment == that.increment &&
                cycle == that.cycle &&
                cache == that.cache &&
                order == that.order &&
                Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, max, min, increment, cycle, cache, order);
    }

    @Override
    public String toString() {
        return toConfig().root().render();
    }

    public static final class Builder {
        private String name;
        private long max;
        private long min;
        private long increment;
        private boolean cycle;
        private int cache;
        private boolean order;
        private Config attributes = ConfigFactory.empty();

        public Builder() {
        }

        public Builder(SequenceDeclare copy) {
            this.name = copy.name;
            this.max = copy.max;
            this.min = copy.min;
            this.increment = copy.increment;
            this.cycle = copy.cycle;
            this.cache = copy.cache;
            this.order = copy.order;
            this.attributes = copy.attributes;
        }


        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder max(long val) {
            max = val;
            return this;
        }

        public Builder min(long val) {
            min = val;
            return this;
        }

        public Builder increment(long val) {
            increment = val;
            return this;
        }

        public Builder cycle(boolean val) {
            cycle = val;
            return this;
        }

        public Builder cache(int val) {
            cache = val;
            return this;
        }

        public Builder order(boolean val) {
            order = val;
            return this;
        }

        public Builder attributes(Config val) {
            attributes = val;
            return this;
        }

        public String name() {
            return name;
        }

        public long max() {
            return max;
        }

        public long min() {
            return min;
        }

        public long increment() {
            return increment;
        }

        public boolean cycle() {
            return cycle;
        }

        public int cache() {
            return cache;
        }

        public boolean order() {
            return order;
        }

        public SequenceDeclare build() {
            return new SequenceDeclare(this);
        }
    }
}
