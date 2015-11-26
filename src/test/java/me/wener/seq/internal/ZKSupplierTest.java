package me.wener.seq.internal;

import me.wener.seq.persistence.ZookeeperSupplier;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class ZKSupplierTest {
    private static CuratorFramework client;
    private static TestingServer server;

    @BeforeClass
    public static void setup() throws Exception {
        server = new TestingServer(8788);
        String connectString = server.getConnectString();
        System.out.println("Zookeeper 链接字符串:" + connectString);
        client = CuratorFrameworkFactory.newClient(connectString, new RetryOneTime(8000));
        client.start();
    }

    @AfterClass
    public static void stop() throws IOException {
        client.close();
        server.close();
    }

    @Test
    public void test() throws Exception {
        client.create().creatingParentsIfNeeded().withProtection().forPath("/seq/test/node");
        ZookeeperSupplier supplier = new ZookeeperSupplier(client, "/seq/test/node");
        for (long i = 0; i < 100; i++) {
            assertEquals(i, supplier.getAsLong());
        }
    }


}
