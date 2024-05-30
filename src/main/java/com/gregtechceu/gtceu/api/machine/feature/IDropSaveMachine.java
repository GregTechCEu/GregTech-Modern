package com.gregtechceu.gtceu.api.machine.feature;

import net.minecraft.nbt.CompoundTag;

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
    default void saveToItem(CompoundTag tag) {
        self().holder.saveManagedPersistentData(tag, true);
    }

    /**
     * Loads the contents of the block entity from a compound tag.
     */
    default void loadFromItem(CompoundTag tag) {
        self().holder.loadManagedPersistentData(tag);
    }
}
