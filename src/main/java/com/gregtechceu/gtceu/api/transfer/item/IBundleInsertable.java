package com.gregtechceu.gtceu.api.transfer.item;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;

/// Implemented by item handlers that can accept a stack of items as a single indivisible unit.
public interface IBundleInsertable extends IItemHandler {

    /// Similar to {@link IItemHandler#insertItem}, but the stack should never be split over multiple logical
    /// destinations upon insertion.
    @NotNull
    ItemStack insertItemBundle(@NotNull ItemStack stack, boolean simulate);
}
