package me.wener.seq.persistence;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import me.wener.seq.Exceptions;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author wener
 * @since 15/11/24
 */
public class RedisPersistenceProvider implements PersistenceProvider {
    @Override
    public Optional<PersistenceSequence> create(String type, String name, Config config) {
        if (!"redis".equals(type)) {
            return Optional.absent();
        }
        return Optional.<PersistenceSequence>of(new RedisSequence(config, name));
    }

    /**
     * Sequence Manager for redis without lock
     *
     * @author wener
     * @since 15/11/24
     */
    public static class RedisSequence implements PersistenceSequence {
        private static final String DEFAULT_PREFIX = "SEQ:";
        private final String prefix;
        private final JedisPool pool;
        private String name;

        public RedisSequence(Config c, String name) {
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
        public long next(String name) {
            try (Jedis jedis = jedis()) {
                // TODO check exists ?
                return jedis.incr(seq(name));
            }
        }

        @Override
        public long current(String name) {
            try (Jedis jedis = jedis()) {
                String v = jedis.get(seq(name));
                return v == null ? 0 : Long.parseLong(v);
            }
        }

        @Override
        public boolean reset(String name, long reset) {
            try (Jedis jedis = jedis()) {
                jedis.set(seq(name), "0");
            }
            return true;
        }

        @Override
        public String getType() {
            return "redis";
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
        public void create(String name) {
            try (Jedis jedis = jedis()) {
                if (jedis.setnx(seq(name), "0") == 1) {
                    throw Exceptions.create(Exceptions.ALREADY_EXISTS, "Seq %s already exists", name);
                }
            }
        }

        public boolean isSafeDistributed() {
            // This implementation is not safe for distributed
            return false;
        }
    }
}
