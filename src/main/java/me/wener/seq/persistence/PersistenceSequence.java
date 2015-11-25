package me.wener.seq.persistence;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A SequenceManager for persistence level, the sequence value of this is [0,+∞],
 * implementation of this interface should provide the basic sequence number to generate real sequence.
 *
 * @author wener
 * @since 15/11/24
 */
@ThreadSafe
public interface PersistenceSequence {
    String getType();

    String getName();

    void delete(String name);

    void create(String name);

    long next(String name);

    long current(String name);

    /**
     * @return isResettable
     */
    boolean reset(String name, long reset);

//    Supplier<Long> supplier(String name);
    // Is this implement safe for distributed version ?
    // If not, the wrapper should synchronize the method invocation
    // boolean isSafeDistributed()
}
