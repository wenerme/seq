package me.wener.seq.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import javax.inject.Named;

/**
 * @author wener
 * @since 15/11/18
 */
@Named("persistence")
public class PersistenceModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<PersistenceProvider> setBinder = Multibinder.newSetBinder(binder(), PersistenceProvider.class);
        setBinder.addBinding().to(InMemoryPersistenceProvider.class);
        setBinder.addBinding().to(RedisPersistenceProvider.class);
    }
}
