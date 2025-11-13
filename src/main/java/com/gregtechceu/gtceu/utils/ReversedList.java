package com.gregtechceu.gtceu.utils;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

public class ReversedList<T> extends AbstractList<T> {

    private final List<T> delegate;

    public ReversedList(List<T> delegate) {
        this.delegate = delegate;
    }

    public int inverseIndex(int i) {
        return size() - 1 - i;
    }

    @Override
    public T get(int index) {
        return this.delegate.get(inverseIndex(index));
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public T set(int index, T element) {
        return this.delegate.set(inverseIndex(index), element);
    }

    @Override
    public T remove(int index) {
        return this.delegate.remove(inverseIndex(index));
    }

    @Override
    public void add(int index, T element) {
        this.delegate.add(inverseIndex(index), element);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return this.delegate.addAll(inverseIndex(index), c);
    }
}
