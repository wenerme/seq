package me.wener.seq.persistence;

import me.wener.seq.internal.LongSupplier;

/**
 * @author wener
 * @since 15/11/26
 */
public interface PersistenceSequence extends LongSupplier {

    /**
     * @return Next sequence number
     */
    @Override
    long getAsLong();

    long current();

    boolean reset(long reset);

    String getName();

    String getProviderName();

    String getType();
}
