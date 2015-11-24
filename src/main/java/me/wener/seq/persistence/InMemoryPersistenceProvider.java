package me.wener.seq.persistence;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import me.wener.seq.Exceptions;
import me.wener.seq.SequenceDeclare;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wener
 * @since 15/11/24
 */
@Singleton
public class InMemoryPersistenceProvider implements PersistenceProvider {
    @Override
    public Optional<PersistenceSequenceManager> create(String type, String name, Config config) {
        if (!"in-memory".equals(type)) {
            return Optional.absent();
        }
        return Optional.<PersistenceSequenceManager>of(new InMemSequenceManager(name, config));
    }

    static class InMemSequenceManager implements PersistenceSequenceManager {
        private final ConcurrentMap<String, Seq> longs = Maps.newConcurrentMap();
        private final String name;
        private final Config config;

        public InMemSequenceManager(String name, Config config) {
            this.name = name;
            this.config = config;
        }

        @Override
        public String getType() {
            return "in-memory";
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void create(SequenceDeclare attrs) {
            Seq seq = new Seq(attrs);
            if (longs.putIfAbsent(attrs.getName(), seq) != null) {
                throw Exceptions.create(Exceptions.ALREADY_EXISTS, "Seq %s already exists", attrs.getName());
            }
        }

        @Override
        public void drop(String name) {
            longs.remove(name);
        }

        @Override
        public void update(SequenceDeclare declare) {
            Seq old = seq(declare.getName());
            Seq seq = new Seq(declare);
            if (!longs.replace(declare.getName(), old, seq)) {
                throw Exceptions.create(Exceptions.UNKNOWN, "Sequence %s has been update by others", declare.getName());
            }
        }

        @Override
        public Set<String> sequences() {
            return ImmutableSet.copyOf(longs.keySet());
        }

        @Override
        public SequenceDeclare get(String name) {
            return seq(name).declare;
        }

        @Override
        public long next(String name) {
            return seq(name).l.getAndIncrement();
        }

        @Override
        public long reset(String name, long reset) {
            Seq seq = seq(name);
            seq.l.set(reset);
            return seq.l.get();
        }

        private
        @Nonnull
        Seq seq(String name) {
            Seq seq = longs.get(name);
            if (seq == null) {
                throw Exceptions.create(Exceptions.NOT_FOUND, "Seq %s not found", name);
            }
            return seq;
        }

        @Override
        public long current(String name) {
            return seq(name).l.get();
        }

        static class Seq {
            private final AtomicLong l = new AtomicLong();
            private final SequenceDeclare declare;

            Seq(SequenceDeclare declare) {
                this.declare = declare;
            }
        }
    }
}
