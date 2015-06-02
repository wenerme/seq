package me.wener.seq.internal.redis;

import java.util.Set;
import me.wener.seq.Sequence;
import me.wener.seq.SequenceManager;
import me.wener.seq.SequenceMeta;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class RedisSequenceManager implements SequenceManager
{
    @Override
    public void create(SequenceMeta meta)
    {

    }

    @Override
    public void create(SequenceMeta meta, long startValue)
    {

    }

    @Override
    public void update(SequenceMeta meta)
    {

    }

    @Override
    public void delete(String name)
    {

    }

    @Override
    public Sequence sequence(String name)
    {
        return null;
    }

    @Override
    public SequenceMeta meta(String name)
    {
        return null;
    }

    @Override
    public Set<String> sequences()
    {
        return null;
    }
}
