package com.gregtechceu.gtceu.utils.reference;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.function.Supplier;

public class RegeneratingSoftReference<T> implements Supplier<T> {

    private final @Nullable ReferenceQueue<? super T> q;
    private final @NotNull Supplier<T> regenerator;
    private @NotNull SoftReference<T> reference;

    public RegeneratingSoftReference(@NotNull T initialReference, @NotNull Supplier<T> regenerator,
                                     @Nullable ReferenceQueue<? super T> q) {
        this.q = q;
        this.reference = new SoftReference<>(initialReference, q);
        this.regenerator = regenerator;
    }

    public RegeneratingSoftReference(@NotNull Supplier<T> regenerator, @Nullable ReferenceQueue<? super T> q) {
        this(regenerator.get(), regenerator, q);
    }

    public RegeneratingSoftReference(@NotNull T initialReference, @NotNull Supplier<T> regenerator) {
        this(initialReference, regenerator, null);
    }

    public RegeneratingSoftReference(@NotNull Supplier<T> regenerator) {
        this(regenerator.get(), regenerator);
    }

    @Override
    public T get() {
        T fetch = reference.get();
        if (fetch == null) {
            fetch = regenerator.get();
            reference = new SoftReference<>(fetch, q);
        }
        return fetch;
    }
}
