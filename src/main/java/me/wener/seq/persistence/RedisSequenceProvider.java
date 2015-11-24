package me.wener.seq.persistence;

import com.google.common.base.Optional;
import com.typesafe.config.Config;
import redis.clients.jedis.Jedis;

import javax.annotation.Nullable;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author wener
 * @since 15/11/21
 */
public class RedisSequenceProvider implements SequenceProvider {
    private final String key;
    private final String host;
    private final int port;
    private final String seqMap;

    public RedisSequenceProvider(Config c) {
        key = c.getString("key");
        host = c.getString("host");
        port = c.getInt("port");

        if (c.hasPath("seq-map")) {
            seqMap = c.getString("seq-map");
        } else {
            seqMap = key + "-SEQUENCE";
        }
        checkNotNull(key);
        checkNotNull(host);
        checkArgument(port > 0);
    }

    private Jedis jedis() {
        return new Jedis(host, port);
    }

    @Override
    public Set<String> getDeclared() {
        try (Jedis jedis = jedis()) {
            return jedis.hgetAll(key).keySet();
        }
    }

    @Nullable
    @Override
    public Optional<String> load(String name) {
        try (Jedis jedis = jedis()) {
            return Optional.fromNullable(jedis.hget(key, name));
        }
    }

    @Override
    public boolean storeIfAbsent(String name, String value) {
        try (Jedis jedis = jedis()) {
            return jedis.hsetnx(key, name, value) > 0;
        }
    }

    @Override
    public long next(String name) {
        return 0;
    }

    @Override
    public long reset(String name, long reset) {
        return 0;
    }

    @Override
    public long current(String name) {
        return 0;
    }
}
