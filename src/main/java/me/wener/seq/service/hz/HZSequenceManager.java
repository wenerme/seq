package me.wener.seq.service.hz;

import me.wener.seq.SequenceDeclare;
import me.wener.seq.SequenceManager;

import javax.inject.Singleton;
import java.util.Set;

/**
 * @author wener
 * @since 15/11/24
 */
@Singleton
public class HZSequenceManager implements SequenceManager {
    @Override
    public void create(SequenceDeclare declare) {

    }

    @Override
    public void drop(String name) {

    }

    @Override
    public void update(SequenceDeclare declare) {

    }

    @Override
    public Set<String> sequences() {
        return null;
    }

    @Override
    public SequenceDeclare get(String name) {
        return null;
    }

    @Override
    public long next(String name) {
        return 0;
    }

    @Override
    public long reset(String name, long reset) {
        return 0;
    }

    @Override
    public long current(String name) {
        return 0;
    }
}
