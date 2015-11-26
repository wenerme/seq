package me.wener.seq.persistence;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import me.wener.seq.Exceptions;

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
public class InMemoryPersistenceFactory implements PersistenceFactory {
    @Override
    public Optional<PersistenceProvider> create(String type, String name, Config config) {
        if (!"in-memory".equals(type)) {
            return Optional.absent();
        }
        return Optional.<PersistenceProvider>of(new InMemProvider(name, config));
    }

    static class InMemProvider implements PersistenceProvider {
        private final ConcurrentMap<String, Seq> longs = Maps.newConcurrentMap();
        private final String name;
        private final Config config;

        public InMemProvider(String name, Config config) {
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
        public void delete(String name) {
            Seq seq = longs.remove(name);
            seq.deleted = true;
        }

        @Override
        public PersistenceSequence create(String name) {
            Seq seq = new Seq(name);
            if (longs.putIfAbsent(name, seq) == null) {
                return seq;
            }
            throw Exceptions.create(Exceptions.ALREADY_EXISTS, name);
        }

        @Override
        public PersistenceSequence get(String name) {
            return seq(name);
        }

        @Override
        public Set<String> sequences() {
            return ImmutableSet.copyOf(longs.keySet());
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

        class Seq implements PersistenceSequence {
            private final AtomicLong l = new AtomicLong();
            private final String name;
            private volatile boolean deleted = false;

            Seq(String name) {
                this.name = name;
            }

            @Override
            public long getAsLong() {
                checkDeleted();
                return l.getAndIncrement();
            }

            private void checkDeleted() {
                Preconditions.checkState(!deleted);
            }

            @Override
            public long current() {
                checkDeleted();
                return l.get() - 1;
            }

            @Override
            public boolean reset(long reset) {
                checkDeleted();
                l.set(reset);
                return true;
            }

            @Override
            public String getName() {
                checkDeleted();
                return name;
            }

            @Override
            public String getProviderName() {
                checkDeleted();
                return InMemProvider.this.getName();
            }

            @Override
            public String getType() {
                checkDeleted();
                return InMemProvider.this.getType();
            }
        }
    }
}
