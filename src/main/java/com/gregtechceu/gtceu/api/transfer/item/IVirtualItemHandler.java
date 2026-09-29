package com.gregtechceu.gtceu.api.transfer.item;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.function.ToIntFunction;

/**
 * Interface to be implemented by item handlers that do not represent real inventories but rather act as distribution
 * network for a number of underlying inventories.
 * Allows correct implementation of functionality that requires underlying inventory topology knowledge, such
 * as keeping exact number of items in each inventory, or network-wide item retrieval.
 */
public interface IVirtualItemHandler extends IItemHandler, IBundleInsertable {

    /// Stocks each inventory represented by this virtual item handler with the given item amounts from source inventory
    int stockInventoryItems(IItemHandler sourceInventory, int maxTransferAmount,
                            ToIntFunction<ItemStack> itemKeepAmountProvider);
}
