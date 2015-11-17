package me.wener.seq.internal;

import com.google.common.base.Supplier;
import me.wener.seq.persistence.ZookeeperSupplier;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
@State(Scope.Benchmark)
public class ZKPSupplierPerformance
{
    private static String connectString;
    private static TestingServer server;

    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(ZKPSupplierPerformance.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup
    public static void setup() throws Exception
    {
        server = new TestingServer(8788);
        connectString = server.getConnectString();
        System.out.println("Zookeeper ConnectString:" + connectString);
    }

    @TearDown
    public static void stop() throws IOException
    {
        server.close();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void get(ClientState c)
    {
        c.supplier.get();
    }

    @State(Scope.Thread)
    public static class ClientState
    {
        CuratorFramework client;
        Supplier<Long> supplier;

        @Setup(Level.Trial)
        public void up() throws Exception
        {
            client = CuratorFrameworkFactory.newClient(connectString, new RetryOneTime(8000));
            client.start();
            client.create().creatingParentsIfNeeded().withProtection().forPath("/seq/test/node");
            supplier = new ZookeeperSupplier(client, "/seq/test/node");
//            supplier = MoreSuppliers.prefetch(supplier, 10);
            supplier = Sequences.asc(supplier, 1000);
        }

        @TearDown(Level.Trial)
        public void down()
        {
            client.close();
        }
    }
}
