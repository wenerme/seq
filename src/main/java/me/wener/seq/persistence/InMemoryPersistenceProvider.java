package me.wener.seq.persistence;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import me.wener.seq.Exceptions;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wener
 * @since 15/11/24
 */
@Singleton
public class InMemoryPersistenceProvider implements PersistenceProvider {
    @Override
    public Optional<PersistenceSequence> create(String type, String name, Config config) {
        if (!"in-memory".equals(type)) {
            return Optional.absent();
        }
        return Optional.<PersistenceSequence>of(new InMemSequence(name, config));
    }

    static class InMemSequence implements PersistenceSequence {
        private final ConcurrentMap<String, Seq> longs = Maps.newConcurrentMap();
        private final String name;
        private final Config config;

        public InMemSequence(String name, Config config) {
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
            longs.remove(name);
        }

        @Override
        public void create(String name) {
            if (longs.putIfAbsent(name, new Seq()) != null) {
                throw Exceptions.create(Exceptions.ALREADY_EXISTS, "Seq %s already exists", name);
            }
        }


        @Override
        public long next(String name) {
            return seq(name).l.getAndIncrement();
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

        @Override
        public boolean reset(String name, long reset) {
            return false;
        }

        static class Seq {
            private final AtomicLong l = new AtomicLong();
        }
    }
}
