package brachy.modularui.widgets.slot;

import net.minecraft.world.item.ItemStack;

public interface IOnSlotChanged {

    /**
     * An empty listener.
     */
    IOnSlotChanged DEFAULT = (oldStack, newStack, client, init) -> {};

    /**
     * Called when an item stack in a {@link ModularSlot} changes.
     *
     * @param oldStack          the previous item in the slot
     * @param newStack          the item that is now in the slot
     * @param client            true if this function is currently called on client side
     * @param init              if this is the first sync call after opening the GUI. Doe not necessarily that this slot
     *                          changed
     */
    void onChange(ItemStack oldStack, ItemStack newStack, boolean client, boolean init);
}
