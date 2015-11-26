package me.wener.seq.internal;

import me.wener.seq.SequenceDeclare;
import me.wener.seq.persistence.ZookeeperSupplier;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.io.IOException;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
@State(Scope.Benchmark)
public class ZKPSupplierPerformance {
    private static String connectString;
    private static TestingServer server;

    public static void main(String[] args) throws RunnerException {
        ChainedOptionsBuilder builder = new OptionsBuilder()
                .include(ZKPSupplierPerformance.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .threads(4)
                .verbosity(VerboseMode.SILENT)
                .forks(1);

        Benchmarks.output(null);
        Benchmarks.threads(builder, 1, 2, 4, 6);
    }

    @Setup
    public static void setup() throws Exception {
        server = new TestingServer(8788);
        connectString = server.getConnectString();
        System.out.println("Zookeeper ConnectString:" + connectString);
    }

    @TearDown
    public static void stop() throws IOException {
        server.close();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void get(ClientState c, Blackhole bh) {
        bh.consume(c.supplier.getAsLong());
    }

    @State(Scope.Thread)
    public static class ClientState {
        CuratorFramework client;
        LongSupplier supplier;

        @Setup(Level.Trial)
        public void up() throws Exception {
            client = CuratorFrameworkFactory.newClient(connectString, new RetryOneTime(8000));
            client.start();
            client.create().creatingParentsIfNeeded().withProtection().forPath("/seq/test/node");
            supplier = new ZookeeperSupplier(client, "/seq/test/node");
//            supplier = MoreSuppliers.prefetch(supplier, 10);
            supplier = Sequences.create(supplier, SequenceDeclare.asc().name("seq").cache(1000).build());
        }

        @TearDown(Level.Trial)
        public void down() {
            client.close();
        }
    }
}
