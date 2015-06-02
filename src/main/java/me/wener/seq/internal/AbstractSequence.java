package me.wener.seq.internal;

import me.wener.seq.Sequence;
import me.wener.seq.SequenceMeta;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public abstract class AbstractSequence implements SequenceMeta, Sequence
{
    protected final SequenceMeta meta;

    public AbstractSequence(SequenceMeta meta)
    {
        this.meta = meta;
    }

    @Override
    public String getName() {return getMeta().getName();}

    @Override
    public long getMaxValue() {return getMeta().getMaxValue();}

    @Override
    public long getMinValue() {return getMeta().getMinValue();}

    @Override
    public long getIncrement() {return getMeta().getIncrement();}

    @Override
    public long getCache() {return getMeta().getCache();}

    @Override
    public boolean isCycle() {return getMeta().isCycle();}

    @Override
    public boolean isOrder() {return getMeta().isOrder();}

    @Override
    public boolean isAscending() {return getMeta().isAscending();}

    @Override
    public boolean isDescending() {return getMeta().isDescending();}

    @Override
    public boolean isUnBounded() {return getMeta().isUnBounded();}

    public SequenceMeta getMeta()
    {
        return meta;
    }

    @Override
    public long[] getNextValue(int count)
    {
        long[] sequences = new long[count];
        for (int i = 0; i < count; i++)
        {
            sequences[i] = getNextValue();
        }
        return sequences;
    }
}
