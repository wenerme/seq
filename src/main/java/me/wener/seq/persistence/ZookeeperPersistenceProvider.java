package me.wener.seq.persistence;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;

/**
 * @author wener
 * @since 15/11/24
 */
public class ZookeeperPersistenceProvider implements PersistenceProvider {
    @Override
    public Optional<PersistenceSequence> create(String type, String name, Config config) {
        if (!"zookeeper".equals(type)) {
            return Optional.absent();
        }
        return Optional.<PersistenceSequence>of(new ZookeeperSequence(name, config));
    }

    static class ZookeeperSequence implements PersistenceSequence {
        public static final Config DEFAULT_CONFIG = ConfigFactory.parseMap(ImmutableMap.of("root", "seq"));
        private final String name;
        private final Config config;
        private final CuratorFramework client;
        private final String root;

        public ZookeeperSequence(String name, Config config) {
            this.name = name;
            this.config = config.withFallback(DEFAULT_CONFIG);
            root = config.getString("root");
            client = null;
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
        public void delete(String name) {

        }

        @Override
        public void create(String name) {

        }

        @Override
        public long next(String name) {
            return 0;
        }

        @Override
        public long current(String name) {
            return 0;
        }

        @Override
        public boolean reset(String name, long reset) {
            return false;
        }

        private String path(String name) {
            return ZKPaths.makePath(root, name);
        }

        private static class Seq extends ZookeeperSupplier {

            public Seq(CuratorFramework client, String path) {
                super(client, path);
            }
        }
    }
}
