package me.wener.seq.persistence;

import com.google.common.base.Optional;
import com.typesafe.config.Config;
import me.wener.seq.SequenceDeclare;

import java.util.Set;

/**
 * @author wener
 * @since 15/11/24
 */
public class ZookeeperPersistenceProvider implements PersistenceProvider {
    @Override
    public Optional<PersistenceSequenceManager> create(String type, String name, Config config) {
        if (!"zookeeper".equals(type)) {
            return Optional.absent();
        }
        return Optional.<PersistenceSequenceManager>of(new ZookeeperSequenceManager(name, config));
    }

    static class ZookeeperSequenceManager implements PersistenceSequenceManager {

        private final String name;
        private final Config config;

        public ZookeeperSequenceManager(String name, Config config) {
            this.name = name;
            this.config = config;
        }

        @Override
        public String getType() {
            return "zookeeper";
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void create(SequenceDeclare declare) {

        }

        @Override
        public void drop(String name) {

        }

        @Override
        public void update(SequenceDeclare declare) {

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
    }
}
