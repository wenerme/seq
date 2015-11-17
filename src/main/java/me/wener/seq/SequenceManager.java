package me.wener.seq;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
@ThreadSafe
public interface SequenceManager {
    void create(SequenceMeta meta);

    void create(SequenceMeta meta, long startValue);

    void update(SequenceMeta meta);

    void delete(String name);

    Sequence sequence(String name);

    SequenceMeta meta(String name);

    Set<String> sequences();
}
