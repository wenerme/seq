package me.wener.seq;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
@ThreadSafe
public interface SequenceManager {
    void create(SequenceDeclare attrs);

    void drop(String name);

    Set<String> sequences();

    long next(String name);

    long reset(String name, long reset);
}
