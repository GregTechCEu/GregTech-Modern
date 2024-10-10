package com.gregtechceu.gtceu.utils;

@FunctionalInterface
public interface TriConsumer<T, U, S> {

    void accept(T t, U u, S s);
}
