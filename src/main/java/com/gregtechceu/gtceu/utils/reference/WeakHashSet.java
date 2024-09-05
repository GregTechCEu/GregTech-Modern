package com.gregtechceu.gtceu.utils.reference;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Replica of {@link java.util.Collections.SetFromMap} for {@link WeakHashMap} to allow for greater type specificity
 * than the {@link java.util.Set} interface.
 */
public class WeakHashSet<E> extends AbstractSet<E> {

    private final WeakHashMap<E, Boolean> m = new WeakHashMap<>();

    private final transient Set<E> s = m.keySet();

    @Override
    public void clear() {
        m.clear();
    }

    @Override
    public int size() {
        return m.size();
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    // TODO access WeakHashMap#getEntry somehow
    // public E get(Object o) {
    // }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean contains(Object o) {
        return m.containsKey(o);
    }

    @Override
    public boolean remove(Object o) {
        return m.remove(o) != null;
    }

    @Override
    public boolean add(E e) {
        return m.put(e, null) == null;
    }

    @Override
    public Iterator<E> iterator() {
        return s.iterator();
    }

    @Override
    public Object[] toArray() {
        return s.toArray();
    }

    @Override
    public <T> T[] toArray(T @NotNull [] a) {
        return s.toArray(a);
    }

    @Override
    public String toString() {
        return s.toString();
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WeakHashSet<?> that = (WeakHashSet<?>) o;
        return Objects.equals(s, that.s);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return s.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return s.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return s.retainAll(c);
    }
    // addAll is the only inherited implementation

    @Override
    public void forEach(Consumer<? super E> action) {
        s.forEach(action);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return s.removeIf(filter);
    }

    @Override
    public Spliterator<E> spliterator() {
        return s.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return s.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return s.parallelStream();
    }
}
