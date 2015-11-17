package me.wener.seq.persistence;

import com.google.common.collect.Range;
import com.typesafe.config.Config;

/**
 * @author wener
 * @since 15/11/17
 */
public class RedisStore implements Store {
    @Override
    public void store(Config config) {

    }

    @Override
    public Config load() {
        return null;
    }

    @Override
    public long next() {
        return 0;
    }

    @Override
    public Range<Long> range() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public long getId() {
        return 0;
    }
}
