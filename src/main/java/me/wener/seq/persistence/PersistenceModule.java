package me.wener.seq.persistence;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.PrivateModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import com.typesafe.config.Config;

import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author wener
 * @since 15/11/18
 */
public class PersistenceModule extends PrivateModule {
    @Override
    protected void configure() {
        Multibinder<PersistenceFactory> setBinder = Multibinder.newSetBinder(binder(), PersistenceFactory.class);
        setBinder.addBinding().to(InMemoryPersistenceFactory.class);
        setBinder.addBinding().to(RedisPersistenceFactory.class);
        expose(PersistenceFactory.class);
        OptionalBinder.newOptionalBinder(binder(), PersistenceFactory.class).setDefault().to(MultiPersistenceFactory.class);
    }

    @Singleton
    private static class MultiPersistenceFactory implements PersistenceFactory {

        private final Set<PersistenceFactory> providers;
        private final String defaultProvider;
        private final ConcurrentMap<String, PersistenceProvider> managers = Maps.newConcurrentMap();

        @Inject
        private MultiPersistenceFactory(Set<PersistenceFactory> providers, Config config) {
            this.providers = providers;
            if (config.hasPath("persistence.default")) {
                defaultProvider = config.getString("persistence.default");
            } else {
                defaultProvider = null;
            }
            if (config.hasPath("persistence")) {

            }
        }

        @Override
        public Optional<PersistenceProvider> create(String type, String name, Config config) {
            if (Strings.isNullOrEmpty(name) && defaultProvider != null) {

            }
            for (PersistenceFactory provider : providers) {
                Optional<PersistenceProvider> o = provider.create(type, name, config);
                if (o.isPresent()) {
                    return o;
                }
            }
            return Optional.absent();
        }
    }
}
