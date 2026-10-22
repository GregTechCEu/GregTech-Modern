package com.gregtechceu.gtceu.api.transfer.item;

import net.minecraft.world.item.ItemStack;

/**
 * Defines an interface needed for tracking item transfer and limiting the amount
 */
public interface ITransferAmountLimiter {

    /** @return the maximum amount of items that can be transferred for the given item type */
    int getRemainingTransferAmount(ItemStack stack);

    /**
     * Called to notify when item transfer has occurred. Stack is the stack that has been transferred,
     * with the amount matching the amount of items transferred.
     */
    void notifyItemTransferred(ItemStack stack);

    /**
     * @return true if more items can be transferred. this is used to short-circuit when the transfer limit has been
     *         reached, but is optional to implement
     */
    default boolean canTransferMoreItems() {
        return true;
    }
}
