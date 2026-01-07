package com.gregtechceu.gtceu.api.mui.widgets.slot;

import net.minecraft.world.inventory.Slot;

public class PlayerSlotGroup extends SlotGroup {

    public static final String NAME = "player_inventory";

    private Slot mainInvSlot;

    public PlayerSlotGroup(String name) {
        super(name, 9, PLAYER_INVENTORY_PRIO, true);
    }

    @Override
    public Slot getFirstSlotForSorting() {
        // we need to return the first non hotbar slot
        // in mui the main inv and the hotbar is a single slot group
        // in bogo they are separated
        // this method is also only used in the sort buttons, which are added to the main inv and not the hotbar
        if (mainInvSlot == null) {
            for (Slot slot : getSlots()) {
                if (slot.getSlotIndex() >= 9 && slot.getSlotIndex() < 36) {
                    mainInvSlot = slot;
                    break;
                }
            }
        }
        return mainInvSlot;
    }

    @Override
    void removeSlot(ModularSlot slot) {
        super.removeSlot(slot);
        if (mainInvSlot == slot) {
            mainInvSlot = null;
        }
    }
}
