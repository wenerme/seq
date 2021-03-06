package me.wener.seq.internal;

import com.google.common.util.concurrent.AbstractService;

/**
 * @author wener
 * @since 15/11/18
 */
public abstract class AbstractServiceAdapter extends AbstractService implements Service {
    private final String name;

    protected AbstractServiceAdapter(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected final void doStart() {
        Thread t = Thread.currentThread();
        String name = t.getName();
        t.setName(getName() + "-start");
        try {
            adapterStart();
            notifyStarted();
        } catch (Exception e) {
            notifyFailed(e);
        } finally {
            t.setName(name);
        }
    }

    protected abstract void adapterStart() throws Exception;

    protected abstract void adapterStop() throws Exception;

    @Override
    protected final void doStop() {
        Thread t = Thread.currentThread();
        String name = t.getName();
        t.setName(getName() + "-stop");
        try {
            adapterStop();
            notifyStopped();
        } catch (Exception e) {
            notifyFailed(e);
        } finally {
            t.setName(name);
        }
    }
}
