package me.wener.seq.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.reflect.ClassPath;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import javax.inject.Named;
import java.io.IOException;
import java.util.Map;

/**
 * Decouple implementation internal
 *
 * @author wener
 * @since 15/11/18
 */
public class Modularize {
    @SuppressWarnings("unchecked")
    public static Map<String, Class<? extends Module>> scanNamed(ClassLoader cl, String pkg) throws IOException {
        UnmodifiableIterator<ClassPath.ClassInfo> iterator = ClassPath.from(cl).getTopLevelClassesRecursive(pkg).iterator();
        Map<String, Class<? extends Module>> scanned = Maps.newHashMap();
        while (iterator.hasNext()) {
            Class<?> cls = iterator.next().load();
            if (Module.class.isAssignableFrom(cls)) {
                {
                    Named named = cls.getAnnotation(Named.class);
                    if (named != null) {
                        scanned.put(named.value(), (Class<? extends Module>) cls);
                    }
                }
                {
                    com.google.inject.name.Named named = cls.getAnnotation(com.google.inject.name.Named.class);
                    if (named != null) {
                        scanned.put(named.value(), (Class<? extends Module>) cls);
                    }
                }
            }
        }
        return scanned;
    }

    public static Module installScanNamed(ClassLoader cl, String pkg, Predicate<Map.Entry<String, Class<? extends Module>>> isEnabled) throws IOException {
        return installMatched(scanNamed(cl, pkg), isEnabled);
    }

    public static Module installMatched(final Map<String, Class<? extends Module>> modules, final Predicate<Map.Entry<String, Class<? extends Module>>> enabled) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                for (Map.Entry<String, Class<? extends Module>> entry : modules.entrySet()) {
                    if (enabled.apply(entry)) {
                        try {
                            install(entry.getValue().newInstance());
                        } catch (Exception e) {
                            Throwables.propagate(e);
                        }
                    }
                }
            }
        };
    }

    public static Multibinder<Service> serviceBinder(Binder binder) {
        return Multibinder.newSetBinder(binder, Service.class);
    }

    public static Config moduleConfig(Config c, String svc) {
        String path = "module." + Preconditions.checkNotNull(svc);
        return c.hasPath(path) ? c.getConfig(path) : ConfigFactory.empty();
    }
}
