package com.gregtechceu.gtceu.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@SuppressWarnings("all")
public final class FastEmptyStream<T> implements Stream<T> {

    private final T[] array;

    public FastEmptyStream(T[] emptyArray) {
        this.array = emptyArray;
    }

    private static final Stream EMPTY = Stream.empty();
    private static final IntStream EMPTY_INT = IntStream.empty();
    private static final LongStream EMPTY_LONG = LongStream.empty();
    private static final DoubleStream EMPTY_DOUBLE = DoubleStream.empty();

    @Override
    public Stream<T> filter(Predicate<? super T> predicate) {
        return this;
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return EMPTY;
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return EMPTY_INT;
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return EMPTY_LONG;
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return EMPTY_DOUBLE;
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return EMPTY;
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return EMPTY_INT;
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return EMPTY_LONG;
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return EMPTY_DOUBLE;
    }

    @Override
    public Stream<T> distinct() {
        return this;
    }

    @Override
    public Stream<T> sorted() {
        return this;
    }

    @Override
    public Stream<T> sorted(Comparator<? super T> comparator) {
        return this;
    }

    @Override
    public Stream<T> peek(Consumer<? super T> action) {
        return this;
    }

    @Override
    public Stream<T> limit(long maxSize) {
        return this;
    }

    @Override
    public Stream<T> skip(long n) {
        return this;
    }

    @Override
    public void forEach(Consumer<? super T> action) {}

    @Override
    public void forEachOrdered(Consumer<? super T> action) {}

    @Override
    public @NotNull T @NotNull [] toArray() {
        return array;
    }

    @Override
    public @NotNull <A> A @NotNull [] toArray(IntFunction<A[]> generator) {
        return (A[]) array;
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return null;
    }

    @Override
    public @NotNull Optional<T> reduce(BinaryOperator<T> accumulator) {
        return Optional.empty();
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return null;
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return null;
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return null;
    }

    @Override
    public @NotNull Optional<T> min(Comparator<? super T> comparator) {
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<T> max(Comparator<? super T> comparator) {
        return Optional.empty();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return false;
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return false;
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return false;
    }

    @Override
    public @NotNull Optional<T> findFirst() {
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<T> findAny() {
        return Optional.empty();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public @NotNull Spliterator<T> spliterator() {
        return Spliterators.emptySpliterator();
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public @NotNull Stream<T> sequential() {
        return this;
    }

    @Override
    public @NotNull Stream<T> parallel() {
        return this;
    }

    @Override
    public @NotNull Stream<T> unordered() {
        return this;
    }

    @Override
    public @NotNull Stream<T> onClose(@NotNull Runnable closeHandler) {
        return this;
    }

    @Override
    public void close() {}
}
