package me.wener.seq.internal;


import me.wener.seq.SequenceMeta;

public abstract class AbstractSequenceMeta implements SequenceMeta {

    @Override
    public boolean isAscending() {
        return getIncrement() > 0;
    }

    @Override
    public boolean isDescending() {
        return getIncrement() < 0;
    }

    @Override
    public boolean isUnBounded() {
        if (isAscending()) {
            return getMaxValue() == Long.MAX_VALUE;
        } else if (isDescending()) {
            return getMinValue() == Long.MIN_VALUE;
        } else {
            throw new IllegalStateException("Can not determinate asc or desc");
        }
    }

    @Override
    public String toString() {
        return Metas.toOption(this).toString();
    }
}
