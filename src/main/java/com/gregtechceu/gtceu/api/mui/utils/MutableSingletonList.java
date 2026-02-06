package com.gregtechceu.gtceu.api.mui.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MutableSingletonList<T> implements List<T> {

    private boolean hasValue;
    private T value;

    public MutableSingletonList() {
        remove();
    }

    public MutableSingletonList(T value) {
        set(value);
    }

    public T get() {
        if (!this.hasValue) throw new IndexOutOfBoundsException("List is empty but tried to access index 0!");
        return value;
    }

    public T getOrNull() {
        return hasValue ? value : null;
    }

    public void set(T t) {
        this.value = t;
        this.hasValue = true;
    }

    public void remove() {
        this.value = null;
        this.hasValue = false;
    }

    @Override
    public int size() {
        return hasValue ? 1 : 0;
    }

    @Override
    public boolean isEmpty() {
        return !hasValue;
    }

    public boolean hasValue() {
        return hasValue;
    }

    @Override
    public boolean contains(Object o) {
        return this.hasValue && Objects.equals(this.value, o);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new Iterator<>() {

            private byte cursor = 0;

            @Override
            public boolean hasNext() {
                return MutableSingletonList.this.hasValue && this.cursor == 0;
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                this.cursor++;
                return MutableSingletonList.this.value;
            }

            @Override
            public void remove() {
                if (this.cursor < 1) throw new IllegalStateException();
                MutableSingletonList.this.remove();
                this.cursor--;
            }
        };
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        if (!this.hasValue) return new Object[0];
        Object[] o = new Object[1];
        o[0] = this.value;
        return o;
    }

    @Override
    public @NotNull <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull [] a) {
        if (!this.hasValue) return a;
        if (a.length == 0) a = Arrays.copyOf(a, 1);
        a[0] = (T1) this.value;
        return a;
    }

    @Override
    public boolean add(T t) {
        if (this.hasValue) throw new IllegalStateException(
                "MutableSingletonList can only have one value, but it already has a value!");
        set(t);
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (contains(o)) {
            remove();
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        int s = c.size();
        if (s > 1 || (s == 1 != this.hasValue)) return false;
        if (!this.hasValue) return true;
        if (c instanceof List<?> l) return Objects.equals(this.value, l.get(0));
        return Objects.equals(this.value, c.iterator().next());
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        if (this.hasValue || c.isEmpty()) return false;
        if (c instanceof List<?> l) {
            add((T) l.get(0));
        } else {
            add(c.iterator().next());
        }
        return true;
    }

    private void verifyIndex(int i, boolean checkEmpty) {
        if (i != 0) throw new IndexOutOfBoundsException("MutableSingletonList only accepts index 0!");
        if (checkEmpty && !this.hasValue)
            throw new IndexOutOfBoundsException("Tried to access index 0, but MutableSingletonList has no element!");
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        verifyIndex(index, false);
        return addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        if (!this.hasValue || c.isEmpty()) return false;
        if (c instanceof List<?> l) {
            return remove(l.get(0));
        }
        return remove(c.iterator().next());
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        if (!this.hasValue || c.contains(this.value)) return false;
        remove();
        return true;
    }

    @Override
    public void clear() {
        remove();
    }

    @Override
    public T get(int index) {
        verifyIndex(index, true);
        return null;
    }

    @Override
    public T set(int index, T element) {
        verifyIndex(index, true);
        T t = this.value;
        this.value = element;
        return t;
    }

    @Override
    public void add(int index, T element) {
        verifyIndex(index, false);
        add(element);
    }

    @Override
    public T remove(int index) {
        verifyIndex(index, true);
        T t = this.value;
        remove();
        return t;
    }

    @Override
    public int indexOf(Object o) {
        return contains(o) ? 0 : -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @Override
    public @NotNull ListIterator<T> listIterator() {
        return new ListIterator<T>() {

            private byte cursor = 0;

            @Override
            public boolean hasNext() {
                return MutableSingletonList.this.hasValue && (this.cursor == 0 || this.cursor == -1);
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                this.cursor = 1;
                return MutableSingletonList.this.value;
            }

            @Override
            public boolean hasPrevious() {
                return MutableSingletonList.this.hasValue && (this.cursor == 0 || this.cursor == 1);
            }

            @Override
            public T previous() {
                if (!hasNext()) throw new NoSuchElementException();
                this.cursor = -1;
                return MutableSingletonList.this.value;
            }

            @Override
            public int nextIndex() {
                return cursor == 0 ? 0 : 1;
            }

            @Override
            public int previousIndex() {
                return cursor == 0 ? 0 : -1;
            }

            @Override
            public void remove() {
                if (this.cursor == 0) throw new IllegalStateException();
                MutableSingletonList.this.remove();
                this.cursor = 0;
            }

            @Override
            public void set(T t) {
                if (this.cursor == 0) throw new IllegalStateException();
                MutableSingletonList.this.set(t);
            }

            @Override
            public void add(T t) {
                MutableSingletonList.this.add(t);
            }
        };
    }

    @Override
    public @NotNull ListIterator<T> listIterator(int index) {
        verifyIndex(index, false);
        return listIterator();
    }

    @Override
    public @NotNull List<T> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > 1 || toIndex < fromIndex) throw new IndexOutOfBoundsException();
        return new MutableSingletonList<>(this.value);
    }
}
