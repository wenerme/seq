package me.wener.seq.persistence;

import com.google.common.base.Optional;
import com.typesafe.config.Config;

/**
 * @author wener
 * @since 15/11/21
 */
public interface PersistenceProvider {
    /**
     * @return absent if type not match
     */
    Optional<PersistenceSequence> create(String type, String name, Config config);
}
