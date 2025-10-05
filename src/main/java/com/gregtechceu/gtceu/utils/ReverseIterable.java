package com.gregtechceu.gtceu.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An iterable to iterate in reverse order over a list.
 * Shorter than using the list iterator of the list.
 */
public class ReverseIterable<T> implements Iterable<T> {

    private final List<T> list;

    public ReverseIterable(List<T> list) {
        this.list = list;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        ListIterator<T> iterator = this.list.listIterator(this.list.size());
        return new ListIterator<>() {

            @Override
            public boolean hasNext() {
                return iterator.hasPrevious();
            }

            @Override
            public T next() {
                return iterator.previous();
            }

            @Override
            public boolean hasPrevious() {
                return iterator.hasNext();
            }

            @Override
            public T previous() {
                return iterator.next();
            }

            @Override
            public int nextIndex() {
                return iterator.previousIndex();
            }

            @Override
            public int previousIndex() {
                return iterator.nextIndex();
            }

            @Override
            public void remove() {
                iterator.remove();
            }

            @Override
            public void set(T t) {
                iterator.set(t);
            }

            @Override
            public void add(T t) {
                iterator.add(t);
            }
        };
    }
}
