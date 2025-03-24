package com.gregtechceu.gtceu.utils.memoization;

import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * A variant of the memoized supplier that stores a block explicitly.
 * Use this to save blocks to
 * {@link com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper#registerUnificationItems(com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry, Supplier[])}
 */
public class MemoizedBlockSupplier<T extends Block> extends MemoizedSupplier<T> {

    protected MemoizedBlockSupplier(Supplier<T> delegate) {
        super(delegate);
    }

    @Override
    public String toString() {
        return "SupplierMemoizer.memoizeBlockSupplier(" +
                (initialized ? "<supplier that returned " + value + ">" : delegate) + ")";
    }
}
