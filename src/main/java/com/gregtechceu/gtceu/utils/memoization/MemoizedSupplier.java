package com.gregtechceu.gtceu.utils.memoization;

import java.util.function.Supplier;

public class MemoizedSupplier<T> implements Supplier<T> {

    protected T value = null;
    protected boolean initialized = false;
    protected final Supplier<T> delegate;

    protected long timeToLiveNanos = -1;
    private volatile long lastAccessNanos;

    protected MemoizedSupplier(Supplier<T> delegate) {
        this.delegate = delegate;
    }

    protected MemoizedSupplier(Supplier<T> delegate, long timeToLiveNanos) {
        this.delegate = delegate;
        this.timeToLiveNanos = timeToLiveNanos;
    }

    @Override
    public T get() {
        if (timeToLiveNanos != -1) {
            long now = System.nanoTime();
            if (lastAccessNanos == 0 || (timeToLiveNanos >= 0 && now - lastAccessNanos >= timeToLiveNanos)) {
                value = delegate.get();
            }
            lastAccessNanos = now;
            return value;
        }
        if (!initialized) {
            value = delegate.get();
            initialized = true;
        }
        return value;
    }

    public void invalidate() {
        lastAccessNanos = 0;
        initialized = false;
        value = null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + ((initialized || lastAccessNanos != 0) ? value : "Uninitialized") +
                ")";
    }
}
