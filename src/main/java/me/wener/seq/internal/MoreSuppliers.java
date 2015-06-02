package me.wener.seq.internal;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class MoreSuppliers
{
    public static <T, F> Supplier<T> compose(Supplier<F> supplier, Function<? super F, T> function)
    {
        return Suppliers.compose(function, supplier);
    }

    public static <A, B, C> Supplier<C> compose(Supplier<A> supplier, Function<A, ? extends B> a, Function<B, C> b)
    {
        return Suppliers.compose(MoreFunctions.compose(a, b), supplier);
    }

    /**
     * If {@code nThreads > 1}, require {@code origin} threadSafe
     */
    public static <T> Supplier<T> prefetch(Supplier<T> origin, int n, int nThreads)
    {
        return new PreFetcher<>(origin, n, nThreads);
    }

    public static <T> Supplier<T> prefetch(Supplier<T> origin, int n)
    {
        return new PreFetcher<>(origin, n, 1);
    }

    public static <T> Supplier<T> prefetch(Supplier<T> origin)
    {
        return new PreFetcher<>(origin, 1, 1);
    }

    private static class PreFetcher<T> implements Supplier<T>, Runnable
    {
        private final BlockingQueue<T> queue;
        private final Supplier<T> origin;
        private final int nThreads;
        private ExecutorService svc;
        private volatile boolean init;
        private boolean running;

        private PreFetcher(Supplier<T> origin, int n, int nThreads)
        {
            this.origin = origin;
            Preconditions.checkArgument(n > 0);
            Preconditions.checkArgument(nThreads > 0);
            queue = new ArrayBlockingQueue<T>(n);
            this.nThreads = nThreads;
        }

        @Override
        public T get()
        {
            try
            {
                if (!init)
                {
                    init();
                    init = true;
                }
                return queue.take();
            } catch (InterruptedException e)
            {
                throw Throwables.propagate(e);
            }
        }

        void init()
        {
            running = true;
            if (nThreads == 1)
            {
                Thread thread = new Thread(this);
                thread.setName("pre-fetch-supplier");
                thread.start();
                svc = null;
            } else
            {
                ThreadFactory factory = new ThreadFactoryBuilder()
                        .setNameFormat("pre-fetch-supplier-%d")
                        .setThreadFactory(Executors.defaultThreadFactory())
                        .build();
                svc = Executors.newFixedThreadPool(nThreads, factory);
                for (int i = 0; i < nThreads; i++)
                {
                    svc.execute(this);
                }
            }
        }

        @Override
        public void run()
        {
            while (running)
            {
                try
                {
                    queue.put(origin.get());
                } catch (InterruptedException e)
                {
                    Thread.interrupted();
                }
            }
        }
    }
}
