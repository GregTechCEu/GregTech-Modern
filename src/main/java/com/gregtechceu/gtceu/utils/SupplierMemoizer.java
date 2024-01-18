package com.gregtechceu.gtceu.utils;

import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * Supplier memoizer. stores the value after the first {@link Supplier#get()} call.
 */
public class SupplierMemoizer {

    public static <T> Supplier<T> memoize(Supplier<T> delegate) {
        return new MemoizedSupplier<>(delegate);
    }

    public static <T extends Block> Supplier<T> memoizeBlockSupplier(Supplier<T> delegate) {
        return new MemoizedBlockSupplier<>(delegate);
    }

    public static class MemoizedSupplier<T> implements Supplier<T> {
        transient T value;
        transient volatile boolean initialized;
        final Supplier<T> delegate;

        MemoizedSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            // A 2-field variant of Double Checked Locking.
            if (!initialized) {
                synchronized (this) {
                    if (!initialized) {
                        T t = delegate.get();
                        value = t;
                        initialized = true;
                        return t;
                    }
                }
            }
            return value;
        }

        @Override
        public String toString() {
            return "SupplierMemoizer.memoize("
                + (initialized ? "<supplier that returned " + value + ">" : delegate)
                + ")";
        }
    }

    /**
     * A variant of the memoized supplier that stores a block explicitly.
     * Use this to save blocks to {@link com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper#registerUnificationItems(com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry, Supplier[])}
     */
    public static class MemoizedBlockSupplier<T extends Block> extends MemoizedSupplier<T> {

        MemoizedBlockSupplier(Supplier<T> delegate) {
            super(delegate);
        }

        @Override
        public String toString() {
            return "SupplierMemoizer.memoizeBlockSupplier("
                + (initialized ? "<supplier that returned " + value + ">" : delegate)
                + ")";
        }
    }
}
