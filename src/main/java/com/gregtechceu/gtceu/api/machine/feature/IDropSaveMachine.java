package com.gregtechceu.gtceu.api.machine.feature;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

/**
 * A machine that can save its contents when dropped.
 */
public interface IDropSaveMachine extends IMachineFeature {

    /**
     * Whether save for breaking.
     */
    default boolean saveBreak() {
        return true;
    }

    /**
     * Whether save for cloning.
     */
    default boolean savePickClone() {
        return true;
    }

    /**
     * Saves the contents of the block entity to an item stack.
     *
     * @param stack The stack to save to.
     */
    default void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        self().holder.self().saveToItem(stack, registries);
    }
}
