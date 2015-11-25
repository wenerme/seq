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
        Multibinder<PersistenceProvider> setBinder = Multibinder.newSetBinder(binder(), PersistenceProvider.class);
        setBinder.addBinding().to(InMemoryPersistenceProvider.class);
        setBinder.addBinding().to(RedisPersistenceProvider.class);
        expose(PersistenceProvider.class);
        OptionalBinder.newOptionalBinder(binder(), PersistenceProvider.class).setDefault().to(MultiPersistenceProvider.class);
    }

    @Singleton
    private static class MultiPersistenceProvider implements PersistenceProvider {

        private final Set<PersistenceProvider> providers;
        private final String defaultProvider;
        private final ConcurrentMap<String, PersistenceSequence> managers = Maps.newConcurrentMap();

        @Inject
        private MultiPersistenceProvider(Set<PersistenceProvider> providers, Config config) {
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
        public Optional<PersistenceSequence> create(String type, String name, Config config) {
            if (Strings.isNullOrEmpty(name) && defaultProvider != null) {

            }
            for (PersistenceProvider provider : providers) {
                Optional<PersistenceSequence> o = provider.create(type, name, config);
                if (o.isPresent()) {
                    return o;
                }
            }
            return Optional.absent();
        }
    }
}
