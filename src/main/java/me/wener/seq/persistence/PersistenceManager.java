package me.wener.seq.persistence;

import com.typesafe.config.Config;

/**
 * @author wener
 * @since 15/11/24
 */
public interface PersistenceManager extends PersistenceFactory {
    // Get from the under laying cache
    PersistenceProvider get(String name);

    /**
     * @return Default persistence
     */
    PersistenceProvider get();

    PersistenceProvider get(Config config);

}
