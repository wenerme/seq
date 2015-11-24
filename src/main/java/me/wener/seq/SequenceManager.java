package me.wener.seq;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
@ThreadSafe
public interface SequenceManager {
    void create(SequenceDeclare declare);

    void drop(String name);

    /**
     * Update sequence declare.Will reset this sequence to initial value.
     */
    void update(SequenceDeclare declare);

    Set<String> sequences();

    SequenceDeclare get(String name);

    /**
     * Get the next value of this  sequence
     */
    long next(String name);

    /**
     * Reset this sequence to this value.
     */
    long reset(String name, long reset);

    /**
     * @return Max or min for unordered sequence ?
     */
    long current(String name);
}
