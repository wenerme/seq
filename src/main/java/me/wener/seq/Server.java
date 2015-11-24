package me.wener.seq;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import me.wener.seq.internal.Modularize;
import me.wener.seq.internal.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author wener
 * @since 15/11/18
 */
public class Server extends AbstractService {
    private final static Logger log = LoggerFactory.getLogger(Server.class);
    private final Config config;
    private ServerLauncher launcher;


    public Server(Config config) {
        this.config = config;
    }

    public static void main(String[] args) {
        Config config = ConfigFactory.load(System.getProperty("seq.config", "seq"));
        new Server(config).start();
    }

    @Override
    protected void doStart() {
        launcher = new ServerLauncher();
        launcher.asyncStart();
    }

    @Override
    protected void doStop() {
        launcher.asyncStop();
    }

    public Server start() {
        startAsync();
        awaitRunning();
        return this;
    }

    private class ServerModule extends AbstractModule {

        @Override
        protected void configure() {
            try {
                install(Modularize.installScanNamed(this.getClass().getClassLoader(), this.getClass().getPackage().getName(), new Predicate<Map.Entry<String, Class<? extends Module>>>() {
                    @Override
                    public boolean apply(@Nullable Map.Entry<String, Class<? extends Module>> input) {
                        if (input == null) {
                            return false;
                        }
                        String path = "services." + input.getKey() + ".enable";
                        // TODO Should use missing is enabled
                        boolean enabled = !config.hasPath(path) || config.getBoolean(path);
                        log.debug("Service {} is {}", input.getKey(), enabled ? "enabled" : "disabled");
                        return enabled;
                    }
                }));
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }

        @Provides
        @Singleton
        private Server server() {
            return Server.this;
        }

        @Provides
        @Singleton
        private Config config() {
            return config;
        }
    }

    private class ServerLauncher {
        private ServiceManager serviceManager;

        public void launcherStop() {
            Thread t = Thread.currentThread();
            String name = t.getName();
            t.setName("server-stop");
            try {
                stop();
                notifyStopped();
            } catch (Exception e) {
                notifyFailed(e);
            } finally {
                t.setName(name);
            }
        }

        private void stop() throws TimeoutException {
            Preconditions.checkState(serviceManager != null, "Not start yet");
            serviceManager.stopAsync();

            if (!tryStop()) {
                throw new TimeoutException("Stop timeout");
            }
        }

        private boolean tryStop() {
            boolean stopped = false;
            int retry = 3;
            while (retry-- > 0) {
                try {
                    serviceManager.awaitStopped(30, TimeUnit.SECONDS);
                    stopped = true;
                    break;
                } catch (TimeoutException e) {
                    log.warn("Stop wait tool long : {} - {}", serviceManager.servicesByState(), serviceManager.startupTimes());
                }
            }
            return stopped;
        }

        public void launcherStart() {
            Thread t = Thread.currentThread();
            String name = t.getName();
            t.setName("server-start");
            try {
                start();
                notifyStarted();
            } catch (Exception e) {
                notifyFailed(e);
            } finally {
                t.setName(name);
            }
        }

        void asyncStart() {
            new Thread() {
                @Override
                public void run() {
                    launcherStart();
                }
            }.start();
        }

        private void start() throws Exception {
            if (serviceManager == null) {
                Injector injector = Guice.createInjector(new ServerModule());
                injector.injectMembers(Server.this);
                Set<Service> services = injector.getInstance(Key.get(new TypeLiteral<Set<Service>>() {
                }));
                serviceManager = new ServiceManager(services);
            }
            serviceManager.startAsync();
            int retry = 3;
            while (retry-- > 0) {
                try {
                    serviceManager.awaitHealthy(30, TimeUnit.SECONDS);
                    break;
                } catch (TimeoutException e) {
                    log.warn("Start service manager wait tool long : {} - {}", serviceManager.servicesByState(), serviceManager.startupTimes());
                }
            }

            if (!serviceManager.isHealthy()) {
                log.error("Server start failed due to time out");
                serviceManager.stopAsync();

                if (!tryStop()) {
                    throw new TimeoutException("Start and stop service with timeout");
                } else {
                    throw new TimeoutException("Start service timeout");
                }
            }
            log.debug("Startup time {}", serviceManager.startupTimes());
        }

        public void asyncStop() {
            new Thread() {
                @Override
                public void run() {
                    launcherStop();
                }
            }.start();
        }
    }
}
