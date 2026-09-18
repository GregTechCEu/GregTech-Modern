package brachy.modularui.utils.memoization;

import java.util.function.Supplier;

public class MemoizedSupplier<T> implements Supplier<T> {

    protected final Supplier<T> delegate;
    protected long timeToLiveNanos;

    protected volatile T value = null;
    private volatile long lastAccessNanos;

    protected MemoizedSupplier(Supplier<T> delegate, long timeToLiveNanos) {
        this.delegate = delegate;
        this.timeToLiveNanos = timeToLiveNanos;
    }

    @Override
    public T get() {
        long now = System.nanoTime();
        if (lastAccessNanos == 0 || (timeToLiveNanos >= 0 && now - lastAccessNanos >= timeToLiveNanos)) {
            value = delegate.get();
        }
        lastAccessNanos = now;
        return value;
    }

    public void invalidate() {
        lastAccessNanos = 0;
        value = null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + (lastAccessNanos != 0 ? value : "Uninitialized") + ")";
    }
}
