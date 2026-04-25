package com.gregtechceu.gtceu.core.util;

import lombok.Getter;

public class ContextualObjectHelper<T> implements AutoCloseable {

    @Getter
    private T current;

    public ContextualObjectHelper<T> with(T object) {
        this.current = object;
        return this;
    }

    @Override
    public void close() {
        this.current = null;
    }
}
