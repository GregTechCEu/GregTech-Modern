package com.gregtechceu.gtceu.api.item;

import net.minecraft.world.item.ItemStack;

/**
 * An interface for data components to implement if they need to
 * have custom comparison logic in {@link ItemStack#isSameItemSameComponents(ItemStack, ItemStack)}.
 */
public interface IMergeableDataComponent {

    /**
     * Called right before data component is compared to a different one in
     * {@link ItemStack#isSameItemSameComponents(ItemStack, ItemStack)}.
     *
     * @param stack the stack that has this data component
     * @param other the stack that this stack is being compared to
     */
    void prepareForComparisonWith(ItemStack stack, ItemStack other);
}
