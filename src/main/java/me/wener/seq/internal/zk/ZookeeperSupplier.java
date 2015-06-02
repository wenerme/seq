package me.wener.seq.internal.zk;

import com.google.common.base.Supplier;
import me.wener.seq.Exceptions;
import me.wener.seq.SequenceException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class ZookeeperSupplier implements Supplier<Long>
{
    private final CuratorFramework client;
    private final String path;

    public ZookeeperSupplier(CuratorFramework client, String path)
    {
        this.client = client;
        this.path = path;
    }

    @Override
    public Long get()
    {
        try
        {
            String node = client.create()
                                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                                .forPath(path);
            // Expected [1,+∞]
            return Long.parseLong(node.substring(node.length() - 10)) - 1;
        } catch (KeeperException.NoNodeException e)
        {
            throw new SequenceException("Sequence not exists", e, Exceptions.NOT_FOUND);
        } catch (Exception e)
        {
            throw new SequenceException(e, Exceptions.UNKNOWN);
        }
    }
}
