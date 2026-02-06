package com.gregtechceu.gtceu.api.mui.utils;

import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;

public interface ObjectList<V> extends it.unimi.dsi.fastutil.objects.ObjectList<V> {

    static <V> ObjectArrayList<V> create() {
        return new ObjectArrayList<>();
    }

    static <V> ObjectArrayList<V> create(int size) {
        return new ObjectArrayList<>(size);
    }

    static <V> ObjectArrayList<V> of(Collection<? extends V> c) {
        return new ObjectArrayList<>(c);
    }

    static <V> ObjectArrayList<V> of(ObjectCollection<? extends V> c) {
        return new ObjectArrayList<>(c);
    }

    static <V> ObjectArrayList<V> of(it.unimi.dsi.fastutil.objects.ObjectList<? extends V> l) {
        return new ObjectArrayList<>(l);
    }

    static <V> ObjectArrayList<V> of(V[] a) {
        return new ObjectArrayList<>(a);
    }

    static <V> ObjectArrayList<V> of(V[] a, int offset, int length) {
        return new ObjectArrayList<>(a, offset, length);
    }

    static <V> ObjectArrayList<V> of(Iterator<? extends V> i) {
        return new ObjectArrayList<>(i);
    }

    static <V> ObjectArrayList<V> of(ObjectIterator<? extends V> i) {
        return new ObjectArrayList<>(i);
    }

    void addFirst(V v);

    void addLast(V v);

    @NotNull
    V getFirst();

    @NotNull
    V getLast();

    @NotNull
    V removeFirst();

    @NotNull
    V removeLast();

    @Nullable
    V peekFirst();

    @Nullable
    V pollFirst();

    @Nullable
    V peekLast();

    @Nullable
    V pollLast();

    void trim();

    @NotNull
    V[] elements();

    void ensureCapacity(int minCapacity);

    class ObjectArrayList<V> extends it.unimi.dsi.fastutil.objects.ObjectArrayList<V> implements ObjectList<V> {

        public ObjectArrayList(int capacity) {
            super(capacity);
        }

        public ObjectArrayList() {}

        public ObjectArrayList(Collection<? extends V> c) {
            super(c);
        }

        public ObjectArrayList(ObjectCollection<? extends V> c) {
            super(c);
        }

        public ObjectArrayList(it.unimi.dsi.fastutil.objects.ObjectList<? extends V> l) {
            super(l);
        }

        public ObjectArrayList(V[] a) {
            super(a);
        }

        public ObjectArrayList(V[] a, int offset, int length) {
            super(a, offset, length);
        }

        public ObjectArrayList(Iterator<? extends V> i) {
            super(i);
        }

        public ObjectArrayList(ObjectIterator<? extends V> i) {
            super(i);
        }

        @Override
        public void addFirst(V v) {
            add(0, v);
        }

        @Override
        public void addLast(V v) {
            add(v);
        }

        @Override
        public @NotNull V getFirst() {
            return get(0);
        }

        @Override
        public @NotNull V getLast() {
            return get(size() - 1);
        }

        @Override
        public @NotNull V removeFirst() {
            return remove(0);
        }

        @Override
        public @NotNull V removeLast() {
            return remove(size() - 1);
        }

        @Override
        public V peekFirst() {
            return isEmpty() ? null : getFirst();
        }

        @Override
        public V pollFirst() {
            return isEmpty() ? null : removeFirst();
        }

        @Override
        public V peekLast() {
            return isEmpty() ? null : getLast();
        }

        @Override
        public V pollLast() {
            return isEmpty() ? null : removeLast();
        }
    }
}
