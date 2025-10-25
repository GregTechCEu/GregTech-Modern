package com.gregtechceu.gtceu.utils.memoization;

import net.minecraft.world.level.block.Block;

import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    public static <T, R> Function<T, R> memoizeFunctionWeakIdent(final Function<T, R> memoFunction) {
        return new Function<>() {

            private final Map<T, R> cache = new ConcurrentWeakIdentityHashMap<>();

            public R apply(T key) {
                return this.cache.computeIfAbsent(key, memoFunction);
            }

            public String toString() {
                return "memoizeFunctionWeakIdent/1[function=" + memoFunction + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, U, V, R> TriFunction<T, U, V, R> memoize(final TriFunction<T, U, V, R> memoTriFunction) {
        return new TriFunction<>() {

            private final Map<Triple<T, U, V>, R> cache = new ConcurrentHashMap<>();

            public R apply(T key1, U key2, V key3) {
                return this.cache.computeIfAbsent(Triple.of(key1, key2, key3), (cacheKey) -> {
                    return memoTriFunction.apply(cacheKey.getLeft(), cacheKey.getMiddle(), cacheKey.getRight());
                });
            }

            public String toString() {
                return "memoize/3[function=" + memoTriFunction + ", size=" + this.cache.size() + "]";
            }
        };
    }
}
