package com.gregtechceu.gtceu.api.mui.widgets.slot;

import net.minecraft.world.item.ItemStack;

public interface IOnSlotChanged {

    /**
     * An empty listener.
     */
    IOnSlotChanged DEFAULT = (newItem, onlyAmountChanged, client, init) -> {};

    /**
     * Called when an item stack in a {@link ModularSlot} changes.
     *
     * @param newItem           the item that is now in the slot
     * @param onlyAmountChanged true if the old item is the same as the new one and only the amount changed
     * @param client            true if this function is currently called on client side
     * @param init              if this is the first sync call after opening the GUI. Doe not necessarily that this slot
     *                          changed
     */
    void onChange(ItemStack newItem, boolean onlyAmountChanged, boolean client, boolean init);
}
