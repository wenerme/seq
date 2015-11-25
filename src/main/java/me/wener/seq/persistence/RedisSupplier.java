package me.wener.seq.persistence;

import com.google.common.base.Supplier;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class RedisSupplier implements Supplier<Long> {
    private final String key;
    private final JedisPool pool;

    public RedisSupplier(String key, JedisPool pool) {
        this.key = key;
        this.pool = pool;
    }

    @Override
    public Long get() {
        // Expected [1,+∞], need [0,+∞]
        try (Jedis jedis = pool.getResource()) {
            return jedis.incr(key) - 1;
        }
    }
}
