package com.gregtechceu.gtceu.utils.memoization;

import com.gregtechceu.gtceu.utils.memoization.function.MemoizedBiFunction;
import com.gregtechceu.gtceu.utils.memoization.function.MemoizedFunction;
import com.gregtechceu.gtceu.utils.memoization.function.MemoizedTriFunction;

import net.minecraft.world.level.block.Block;

import lombok.Getter;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class GTMemoizer {

    /**
     * This method doesn't return a thread-safe memoization of the delegate, if you need thread-safety you should use
     * {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoize}
     * 
     * @param delegate the supplier to memoize
     * @return memoized supplier for the delegate
     */
    public static <T> MemoizedSupplier<T> memoize(Supplier<T> delegate) {
        return new MemoizedSupplier<>(delegate);
    }

    public static <T extends Block> MemoizedBlockSupplier<T> memoizeBlockSupplier(Supplier<T> delegate) {
        return new MemoizedBlockSupplier<>(delegate);
    }

    public static <T, R> MemoizedFunction<T, R> memoizeFunctionWeakIdent(final Function<T, R> memoFunction) {
        return new MemoizedFunction<>() {

            @Getter
            private final Map<T, R> cache = new ConcurrentWeakIdentityHashMap<>();

            @Override
            public R apply(T key) {
                return this.cache.computeIfAbsent(key, memoFunction);
            }

            @Override
            public String toString() {
                return "memoizeFunctionWeakIdent/1[function=" + memoFunction + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, U,
            R> MemoizedBiFunction<T, U, R> memoizeFunctionWeakIdent(final BiFunction<T, U, R> memoBiFunction) {
        return new MemoizedBiFunction<>() {

            @Getter
            private final Map<Pair<T, U>, R> cache = new ConcurrentWeakIdentityHashMap<>();

            @Override
            public R apply(T key1, U key2) {
                return this.cache.computeIfAbsent(Pair.of(key1, key2), (key) -> {
                    return memoBiFunction.apply(key.getLeft(), key.getRight());
                });
            }

            @Override
            public String toString() {
                return "memoizeFunctionWeakIdent/2[function=" + memoBiFunction + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, U, V, R> MemoizedTriFunction<T, U, V, R> memoize(final TriFunction<T, U, V, R> memoTriFunction) {
        return new MemoizedTriFunction<>() {

            @Getter
            private final Map<Triple<T, U, V>, R> cache = new ConcurrentHashMap<>();

            @Override
            public R apply(T key1, U key2, V key3) {
                return this.cache.computeIfAbsent(Triple.of(key1, key2, key3), (cacheKey) -> {
                    return memoTriFunction.apply(cacheKey.getLeft(), cacheKey.getMiddle(), cacheKey.getRight());
                });
            }

            @Override
            public String toString() {
                return "memoize/3[function=" + memoTriFunction + ", size=" + this.cache.size() + "]";
            }
        };
    }
}
