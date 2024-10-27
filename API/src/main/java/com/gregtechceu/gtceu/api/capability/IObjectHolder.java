package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public interface IObjectHolder {

    /**
     * Get the item held in the object holder.
     * 
     * @param remove Whether to also remove the item from its slot.
     */
    @NotNull
    ItemStack getHeldItem(boolean remove);

    /**
     * Set the item held in the object holder. Overwrites the currently held item.
     */
    void setHeldItem(@NotNull ItemStack heldItem);

    /**
     * Get the data item held in the object holder.
     * 
     * @param remove Whether to also remove the item from its slot.
     */
    @NotNull
    ItemStack getDataItem(boolean remove);

    /**
     * Set the data item held in the object holder. Overwrites the currently held data item.
     */
    void setDataItem(@NotNull ItemStack dataItem);

    /**
     * Lock or unlock the object holder, meaning if the items can be removed or not.
     */
    void setLocked(boolean locked);

    Direction getFrontFacing();

    /**
     * @return the object holder's contents represented as an IItemHandler
     */
    @NotNull
    NotifiableItemStackHandler getAsHandler();
}
