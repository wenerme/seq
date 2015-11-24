package me.wener.seq.persistence;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import me.wener.seq.Exceptions;
import me.wener.seq.SequenceDeclare;
import me.wener.seq.SequenceException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Set;

/**
 * @author wener
 * @since 15/11/24
 */
public class RedisPersistenceProvider implements PersistenceProvider {
    @Override
    public Optional<PersistenceSequenceManager> create(String type, String name, Config config) {
        if (!"redis".equals(type)) {
            return Optional.absent();
        }
        return Optional.<PersistenceSequenceManager>of(new RedisSequenceManager(config, name));
    }

    /**
     * Sequence Manager for redis without lock
     *
     * @author wener
     * @since 15/11/24
     */
    public static class RedisSequenceManager implements PersistenceSequenceManager {
        private static final String DEFAULT_PREFIX = "SEQ:";
        private final String prefix;
        private final JedisPool pool;
        private String name;

        public RedisSequenceManager(Config c, String name) {
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

        @Override
        public void create(SequenceDeclare declare) {
            try (Jedis jedis = jedis()) {
                String seq = seq(declare.getName());
                if (jedis.exists(seq)) {
                    throw Exceptions.create(Exceptions.ALREADY_EXISTS, "Seq %s already exists", declare.getName());
                }
                jedis.hset(seq, "c", declare.toString());
            }
        }

        @Override
        public void drop(String name) {
            try (Jedis jedis = jedis()) {
                jedis.hdel(seq(name));
            }
        }

        @Override
        public void update(SequenceDeclare declare) {
            try (Jedis jedis = jedis()) {
                String seq = checkExists(declare.getName(), jedis);
                jedis.del(seq);
                jedis.hset(seq, "v", declare.toString());
            }
        }

        String seq(String name) {
            return prefix + name;
        }

        @Override
        public Set<String> sequences() {
            try (Jedis jedis = jedis()) {
                return jedis.keys(prefix + "*");
            }
        }

        @Override
        public SequenceDeclare get(String name) {
            try (Jedis jedis = jedis()) {
                String seq = checkExists(name, jedis);
                return SequenceDeclare.from(ConfigFactory.parseString(jedis.hget(seq, "c"))).build();
            }
        }

        @Override
        public long next(String name) {
            try (Jedis jedis = jedis()) {
                String seq = checkExists(name, jedis);
                return jedis.hincrBy(seq, "v", 1);
            }
        }

        @Override
        public long reset(String name, long reset) {
            try (Jedis jedis = jedis()) {
                String seq = checkExists(name, jedis);
                return jedis.hdel(seq, "v");
            }
        }

        private String checkExists(String name, Jedis jedis) {
            String seq = seq(name);
            if (!jedis.hexists(seq, "c")) {
                throw new SequenceException("Seq " + name + " not found", Exceptions.NOT_FOUND);
            }
            return seq;
        }

        @Override
        public long current(String name) {
            try (Jedis jedis = jedis()) {
                String seq = checkExists(name, jedis);
                String v = jedis.hget(seq, "v");
                return v == null ? 0 : Long.parseLong(v);
            }
        }

        @Override
        public String getType() {
            return "redis";
        }

        @Override
        public String getName() {
            return name;
        }

        public boolean isSafeDistributed() {
            // This implementation is not safe for distributed
            return false;
        }
    }
}
