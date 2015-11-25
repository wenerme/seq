package me.wener.seq.service.hz;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.multibindings.OptionalBinder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import me.wener.seq.SequenceManager;
import me.wener.seq.internal.AbstractServiceAdapter;
import me.wener.seq.internal.Modularize;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;

/**
 * @author wener
 * @since 15/11/18
 */

@Named("hazelcast")
public class HazelcastModule extends AbstractModule {
    @Override
    protected void configure() {
        Modularize.serviceBinder(binder()).addBinding().to(HZService.class);
        OptionalBinder.newOptionalBinder(binder(), SequenceManager.class).setBinding().to(HZSequenceManager.class);
    }

    @Provides
    @Singleton
    private HazelcastInstance hz(Config root) {
        com.hazelcast.config.Config c = new com.hazelcast.config.Config();
        Config config = Modularize.moduleConfig(root, "hazelcast");
        if (config.hasPath("properties")) {
            for (Map.Entry<String, ConfigValue> entry : config.getConfig("properties").entrySet()) {
                c.setProperty(entry.getKey(), entry.getValue().unwrapped().toString());
            }
        }
        return Hazelcast.newHazelcastInstance(c);
    }

    private static class HZService extends AbstractServiceAdapter {
        @Inject
        private Provider<HazelcastInstance> hz;

        protected HZService() {
            super("hazelcast");
        }

        @Override
        protected void adapterStart() {
            hz.get();
        }

        @Override
        protected void adapterStop() {
            hz.get().shutdown();
        }
    }
}
