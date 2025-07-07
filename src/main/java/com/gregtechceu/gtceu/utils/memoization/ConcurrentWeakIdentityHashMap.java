package com.gregtechceu.gtceu.utils.memoization;

import net.minecraft.MethodsReturnNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConcurrentWeakIdentityHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {

    private final ConcurrentMap<Key<K>, V> map;
    private final ReferenceQueue<K> queue = new ReferenceQueue<>();
    private transient @Nullable Set<Map.Entry<K, V>> entrySet;

    public ConcurrentWeakIdentityHashMap(int initialCapacity) {
        this.map = new ConcurrentHashMap<>(initialCapacity);
    }

    public ConcurrentWeakIdentityHashMap() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public @Nullable V get(Object key) {
        purgeKeys();
        return map.get(new Key<>(key, null));
    }

    @Override
    public @Nullable V put(K key, V value) {
        purgeKeys();
        return map.put(new Key<>(key, queue), value);
    }

    @Override
    public int size() {
        purgeKeys();
        return map.size();
    }

    @SuppressWarnings({ "ReassignedVariable", "SuspiciousMethodCalls" })
    private void purgeKeys() {
        Reference<? extends K> reference;
        while ((reference = queue.poll()) != null) {
            map.remove(reference);
        }
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> entrySet = this.entrySet;
        return entrySet == null ? this.entrySet = new EntrySet() : entrySet;
    }

    @Override
    public @Nullable V putIfAbsent(K key, V value) {
        purgeKeys();
        return map.putIfAbsent(new Key<>(key, queue), value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(new Key<>(key, null));
    }

    @Override
    public boolean remove(Object key, Object value) {
        purgeKeys();
        return map.remove(new Key<>(key, null), value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        purgeKeys();
        return map.replace(new Key<>(key, null), oldValue, newValue);
    }

    @Override
    public @Nullable V replace(K key, V value) {
        purgeKeys();
        return map.replace(new Key<>(key, null), value);
    }

    @Override
    public boolean containsKey(Object key) {
        purgeKeys();
        return map.containsKey(new Key<>(key, null));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void clear() {
        while (queue.poll() != null);
        map.clear();
    }

    @Override
    public boolean containsValue(Object value) {
        purgeKeys();
        return map.containsValue(value);
    }

    private static class Key<T> extends WeakReference<T> {

        private final int hash;

        Key(T t, @Nullable ReferenceQueue<T> queue) {
            super(t, queue);
            hash = System.identityHashCode(Objects.requireNonNull(t));
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof Key<?> key && key.get() == this.get();
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    private class Iter implements Iterator<Map.Entry<K, V>> {

        private final Iterator<Map.Entry<Key<K>, V>> it;
        private @Nullable Map.Entry<K, V> nextValue;

        Iter(Iterator<Map.Entry<Key<K>, V>> it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            if (nextValue != null) {
                return true;
            }
            while (it.hasNext()) {
                Map.Entry<Key<K>, V> entry = it.next();
                K key = entry.getKey().get();
                if (key != null) {
                    nextValue = new Entry(key, entry.getValue());
                    return true;
                } else {
                    it.remove();
                }
            }
            return false;
        }

        @Override
        public @Nullable Map.Entry<K, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Map.Entry<K, V> entry = nextValue;
            nextValue = null;
            return entry;
        }

        @Override
        public void remove() {
            it.remove();
            nextValue = null;
        }
    }

    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new Iter(map.entrySet().iterator());
        }

        @Override
        public int size() {
            return ConcurrentWeakIdentityHashMap.this.size();
        }

        @Override
        public void clear() {
            ConcurrentWeakIdentityHashMap.this.clear();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry<?, ?> e)) {
                return false;
            }
            return ConcurrentWeakIdentityHashMap.this.get(e.getKey()) == e.getValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry<?, ?> e)) {
                return false;
            }
            return ConcurrentWeakIdentityHashMap.this.remove(e.getKey(), e.getValue());
        }
    }

    private class Entry extends AbstractMap.SimpleEntry<K, V> {

        @Serial
        private static final long serialVersionUID = 1L;

        Entry(K key, V value) {
            super(key, value);
        }

        @Override
        public V setValue(V value) {
            ConcurrentWeakIdentityHashMap.this.put(getKey(), value);
            return super.setValue(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Map.Entry<?, ?> e) {
                return getKey() == e.getKey() && getValue() == e.getValue();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(getKey()) ^ System.identityHashCode(getValue());
        }
    }
}
