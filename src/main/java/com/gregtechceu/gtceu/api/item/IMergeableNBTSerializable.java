package com.gregtechceu.gtceu.api.item;

import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * An interface for capability providers to implement if they need to store NBT data
 * and have custom comparison logic in {@link ItemStack#areCapsCompatible(CapabilityDispatcher)}.
 */
public interface IMergeableNBTSerializable extends INBTSerializable<Tag> {

    /**
     * Called right before this capability provider is compared to a different one in
     * {@link ItemStack#areCapsCompatible(CapabilityDispatcher)}.
     * The other capability provider is guaranteed to have the same id as this one, but may be {@code null} if the other
     * item does not have this
     * capability.
     * 
     * @param other the other capability provider
     */
    void prepareForComparisonWith(INBTSerializable<Tag> other);
}
