package com.gregtechceu.gtceu.utils.memoization;

import net.minecraft.world.level.block.Block;

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
}
