package me.wener.seq.service.hz;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import me.wener.seq.internal.AbstractServiceAdapter;

import javax.inject.Named;
import javax.inject.Provider;
import java.util.Map;

/**
 * @author wener
 * @since 15/11/18
 */

@Named("hazelcast")
public class HazelcastModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), me.wener.seq.internal.Service.class).addBinding().to(HZService.class);
    }

    @Provides
    private HazelcastInstance hz(Config config) {
        com.hazelcast.config.Config c = new com.hazelcast.config.Config();
        if (config.hasPath("services.hazelcast.properties")) {
            for (Map.Entry<String, ConfigValue> entry : config.getConfig("services.hazelcast.properties").entrySet()) {
                c.setProperty(entry.getKey(), entry.getValue().render());
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
