package me.wener.seq.internal.redis;

import com.google.common.base.Supplier;
import redis.clients.jedis.JedisCommands;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class JedisSupplier implements Supplier<Long>
{
    private final String key;
    private final JedisCommands jedis;

    public JedisSupplier(String key, JedisCommands jedis)
    {
        this.key = key;
        this.jedis = jedis;
    }

    @Override
    public Long get()
    {
        // Expected [1,+∞]
        return jedis.incr(key) - 1;
    }
}
