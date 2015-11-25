package me.wener.seq.persistence;

import com.typesafe.config.Config;

/**
 * @author wener
 * @since 15/11/24
 */
public interface PersistenceManager extends PersistenceProvider {
    // Get from the under laying cache
    PersistenceSequence get(String name);

    /**
     * @return Default persistence
     */
    PersistenceSequence get();

    PersistenceSequence get(Config config);

}
