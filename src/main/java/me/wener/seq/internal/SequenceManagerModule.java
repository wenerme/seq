package me.wener.seq.internal;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.OptionalBinder;
import me.wener.seq.LocalSequenceManager;
import me.wener.seq.SequenceManager;

/**
 * @author wener
 * @since 15/11/24
 */
public class SequenceManagerModule extends AbstractModule {
    @Override
    protected void configure() {
        OptionalBinder.newOptionalBinder(binder(), SequenceManager.class).setDefault().to(LocalSequenceManager.class);
    }
}
