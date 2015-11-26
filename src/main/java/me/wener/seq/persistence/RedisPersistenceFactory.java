package me.wener.seq.persistence;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import me.wener.seq.Exceptions;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Set;

/**
 * @author wener
 * @since 15/11/24
 */
public class RedisPersistenceFactory implements PersistenceFactory {
    private static final String REDIS_TYPE = "redis";

    @Override
    public Optional<PersistenceProvider> create(String type, String name, Config config) {
        if (!REDIS_TYPE.equals(type)) {
            return Optional.absent();
        }
        return Optional.<PersistenceProvider>of(new RedisProvider(config, name));
    }

    /**
     * Sequence Manager for redis without lock
     *
     * @author wener
     * @since 15/11/24
     */
    public static class RedisProvider implements PersistenceProvider {
        private static final String DEFAULT_PREFIX = "SEQ:";
        private final String prefix;
        private final JedisPool pool;
        private String name;

        public RedisProvider(Config c, String name) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "name");
            String host = c.getString("host");
            this.name = name;
            int port = c.getInt("port");
            if (c.hasPath("prefix")) {
                prefix = c.getString("prefix");
            } else {
                prefix = DEFAULT_PREFIX;
            }
            JedisPoolConfig config = new JedisPoolConfig();
            pool = new JedisPool(config, host, port);
            try (Jedis jedis = jedis()) {
                jedis.ping();
            }
        }

        private Jedis jedis() {
            return pool.getResource();
        }

        String seq(String name) {
            return prefix + name;
        }


        @Override
        public String getType() {
            return REDIS_TYPE;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void delete(String name) {
            try (Jedis jedis = jedis()) {
                jedis.del(seq(name));
            }
        }

        @Override
        public PersistenceSequence create(String name) {
            try (Jedis jedis = jedis()) {
                String seq = seq(name);
                if (jedis.setnx(seq, "0") == 1) {
                    throw Exceptions.create(Exceptions.ALREADY_EXISTS, "Seq %s already exists", name);
                }
                return new Seq(pool, seq, name, this.name);
            }
        }

        @Override
        public PersistenceSequence get(String name) {
            try (Jedis jedis = jedis()) {
                String seq = seq(name);
                if (!jedis.exists(seq)) {
                    throw Exceptions.create(Exceptions.NOT_FOUND, "Seq %s not found", name);
                }
                return new Seq(pool, seq, name, this.name);
            }
        }

        @Override
        public Set<String> sequences() {
            try (Jedis jedis = jedis()) {
                Set<String> keys = jedis.keys(prefix + "*");
                ImmutableSet.Builder<String> builder = ImmutableSet.builder();
                for (String key : keys) {
                    String k = key.substring(prefix.length());
                    if (k.length() > 0) {
                        builder.add(k);
                    }
                }
                return builder.build();
            }
        }

        static class Seq implements PersistenceSequence {
            private final JedisPool pool;
            private final String key;
            private final String providerName;
            private final String name;

            Seq(JedisPool pool, String key, String name, String providerName) {
                this.pool = pool;
                this.key = key;
                this.name = name;
                this.providerName = providerName;
            }

            private Jedis jedis() {
                return pool.getResource();
            }

            @Override
            public long getAsLong() {
                try (Jedis jedis = jedis()) {
                    // TODO check exists ?
                    return jedis.incr(key);
                }
            }

            @Override
            public long current() {
                try (Jedis jedis = jedis()) {
                    String v = jedis.get(key);
                    return v == null ? 0 : Long.parseLong(v);
                }
            }

            @Override
            public boolean reset(long reset) {
                try (Jedis jedis = jedis()) {
                    jedis.set(key, "0");
                }
                return true;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getProviderName() {
                return providerName;
            }

            @Override
            public String getType() {
                return REDIS_TYPE;
            }
        }
    }
}
