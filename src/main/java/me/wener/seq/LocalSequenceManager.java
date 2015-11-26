package me.wener.seq;

import com.google.common.base.Supplier;
import me.wener.seq.persistence.PersistenceManager;
import me.wener.seq.persistence.PersistenceProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wener
 * @since 15/11/24
 */
@Singleton
public class LocalSequenceManager implements SequenceManager {

    private final PersistenceManager persistenceManager;

    @Inject
    public LocalSequenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public void create(SequenceDeclare declare) {
    }

    private PersistenceProvider persistence(SequenceDeclare declare) {
        if (declare.getAttributes().hasPath("persistence")) {
            return persistenceManager.get(declare.getAttributes().getConfig("persistence"));
        } else {
            return persistenceManager.get();
        }
    }

    @Override
    public void drop(String name) {
        // How to get the persistence ?
    }

    @Override
    public void update(SequenceDeclare declare) {
        // TODO invalidate all cache
    }

    @Override
    public Set<String> sequences() {
        return null;
    }

    @Override
    public SequenceDeclare get(String name) {
        return null;
    }

    @Override
    public long next(String name) {
        return 0;
    }

    @Override
    public long reset(String name, long reset) {
        return 0;
    }

    @Override
    public long current(String name) {
        return 0;
    }

    private static class Seq {
        private final AtomicLong current = new AtomicLong();
        private SequenceDeclare declare;
        private PersistenceProvider persistenceProvider;
        private Supplier<Long> supplier;

        long next() {
            Long c = supplier.get();
            current.set(c);
            return c;
        }

        long current() {
            return current.get();
        }

        void reset(long reset) {
            throw new AssertionError();
        }
    }
}
