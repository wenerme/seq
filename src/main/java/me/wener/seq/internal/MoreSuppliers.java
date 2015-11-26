package me.wener.seq.internal;

import com.google.common.base.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class MoreSuppliers {
    public static LongSupplier compose(LongSupplier supplier, LongFunction function) {
        return new LongSupplierComposition(function, supplier);
    }

    public static LongSupplier compose(LongSupplier supplier, LongFunction a, LongFunction b) {
        return compose(supplier, compose(a, b));
    }

    private static LongFunction compose(LongFunction a, LongFunction b) {
        return new LongFunctionComposition(a, b);
    }

    public static <T, F> Supplier<T> compose(Supplier<F> supplier, Function<? super F, T> function) {
        return Suppliers.compose(function, supplier);
    }

    public static <A, B, C> Supplier<C> compose(Supplier<A> supplier, Function<A, ? extends B> a, Function<B, C> b) {
        return Suppliers.compose(MoreFunctions.compose(a, b), supplier);
    }

    /**
     * If {@code nThreads > 1}, require {@code origin} threadSafe
     */
    public static <T> Supplier<T> prefetch(Supplier<T> origin, int n, int nThreads) {
        return new PreFetcher<>(origin, n, nThreads);
    }

    public static <T> Supplier<T> prefetch(Supplier<T> origin, int n) {
        return new PreFetcher<>(origin, n, 1);
    }

    public static <T> Supplier<T> prefetch(Supplier<T> origin) {
        return new PreFetcher<>(origin, 1, 1);
    }

    private static class PreFetcher<T> implements Supplier<T>, Runnable {
        private final BlockingQueue<T> queue;
        private final Supplier<T> origin;
        private final int nThreads;
        private ExecutorService svc;
        private volatile boolean init;
        private boolean running;

        private PreFetcher(Supplier<T> origin, int n, int nThreads) {
            this.origin = origin;
            Preconditions.checkArgument(n > 0);
            Preconditions.checkArgument(nThreads > 0);
            queue = new ArrayBlockingQueue<T>(n);
            this.nThreads = nThreads;
        }

        @Override
        public T get() {
            try {
                if (!init) {
                    init();
                    init = true;
                }
                return queue.take();
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }

        void init() {
            running = true;
            if (nThreads == 1) {
                Thread thread = new Thread(this);
                thread.setName("pre-fetch-supplier");
                thread.start();
                svc = null;
            } else {
                ThreadFactory factory = new ThreadFactoryBuilder()
                        .setNameFormat("pre-fetch-supplier-%d")
                        .setThreadFactory(Executors.defaultThreadFactory())
                        .build();
                svc = Executors.newFixedThreadPool(nThreads, factory);
                for (int i = 0; i < nThreads; i++) {
                    svc.execute(this);
                }
            }
        }

        @Override
        public void run() {
            while (running) {
                try {
                    queue.put(origin.get());
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        }
    }


    private static class LongSupplierComposition
            implements LongSupplier, Serializable {
        private static final long serialVersionUID = 0;
        final LongFunction function;
        final LongSupplier supplier;

        LongSupplierComposition(LongFunction function, LongSupplier supplier) {
            this.function = function;
            this.supplier = supplier;
        }

        @Override
        public long getAsLong() {
            return function.apply(supplier.getAsLong());
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof LongSupplierComposition) {
                LongSupplierComposition that = (LongSupplierComposition) obj;
                return function.equals(that.function) && supplier.equals(that.supplier);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(function, supplier);
        }

        @Override
        public String toString() {
            return "LongSupplierComposition.compose(" + function + ", " + supplier + ")";
        }
    }


    private static class LongFunctionComposition implements LongFunction, Serializable {
        private static final long serialVersionUID = 0;
        private final LongFunction g;
        private final LongFunction f;

        public LongFunctionComposition(LongFunction g, LongFunction f) {
            this.g = checkNotNull(g);
            this.f = checkNotNull(f);
        }

        @Override
        public long apply(long a) {
            return g.apply(f.apply(a));
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof LongFunctionComposition) {
                LongFunctionComposition that = (LongFunctionComposition) obj;
                return f.equals(that.f) && g.equals(that.g);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return f.hashCode() ^ g.hashCode();
        }

        @Override
        public String toString() {
            return g + "(" + f + ")";
        }
    }
}
