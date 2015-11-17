package me.wener.seq.persistence;

import com.google.common.collect.Range;
import com.typesafe.config.Config;

/**
 * Persistence storage
 *
 * @author wener
 * @since 15/11/17
 */
public interface Store {
    void store(Config config);

    Config load();

    long next();

    Range<Long> range();

    String getName();

    long getId();
}
