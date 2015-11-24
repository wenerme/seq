package me.wener.seq.persistence;

import me.wener.seq.SequenceManager;

/**
 * A SequenceManager for persistence level, the sequence value of this is [0,+∞],
 * implementation of this interface should provide the basic sequence number to generate real sequence.
 *
 * @author wener
 * @since 15/11/24
 */
public interface PersistenceSequenceManager extends SequenceManager {
    String getType();

    String getName();

    // Is this implement safe for distributed version ?
    // If not, the wrapper should synchronize the method invocation
    // boolean isSafeDistributed()
}
