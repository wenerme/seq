package me.wener.seq.persistence;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;

/**
 * A SequenceManager for persistence level, the sequence value of this is [0,+∞],
 * implementation of this interface should provide the basic sequence number to generate real sequence.
 *
 * @author wener
 * @since 15/11/24
 */
@ThreadSafe
public interface PersistenceProvider {
    String getType();

    String getName();

    void delete(String name);

    PersistenceSequence create(String name);

    PersistenceSequence get(String name);

    Set<String> sequences();
}
