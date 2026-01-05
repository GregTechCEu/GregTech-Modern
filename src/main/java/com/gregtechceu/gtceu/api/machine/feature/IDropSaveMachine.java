package com.gregtechceu.gtceu.api.machine.feature;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.OverridingMethodsMustInvokeSuper;

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
     * Saves the contents of the block entity to a compound tag.
     *
     * @param tag The tag to save to.
     */
    @OverridingMethodsMustInvokeSuper
    void saveToItem(CompoundTag tag);

    /**
     * Loads the contents of the block entity from a compound tag.
     */
    @OverridingMethodsMustInvokeSuper
    void loadFromItem(CompoundTag tag);
}
