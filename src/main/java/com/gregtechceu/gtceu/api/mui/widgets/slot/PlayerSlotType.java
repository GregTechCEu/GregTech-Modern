package com.gregtechceu.gtceu.api.mui.widgets.slot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.*;

public enum PlayerSlotType {

    HOTBAR,
    MAIN_INVENTORY,
    OFFHAND,
    ARMOR;

    public static PlayerSlotType getPlayerSlotType(Slot slot) {
        int index = slot.getSlotIndex();
        if (index < 0 || index > 40) return null;
        if (slot instanceof SlotItemHandler slotItemHandler) {
            if (slotItemHandler.getItemHandler() instanceof PlayerMainInvWrapper) {
                return index < 9 ? HOTBAR : MAIN_INVENTORY;
            }
            if (slotItemHandler.getItemHandler() instanceof PlayerArmorInvWrapper) {
                return ARMOR;
            }
            if (slotItemHandler.getItemHandler() instanceof PlayerOffhandInvWrapper) {
                return OFFHAND;
            }
            if (!(slotItemHandler.getItemHandler() instanceof PlayerInvWrapper) &&
                    !(slotItemHandler.getItemHandler() instanceof InvWrapper invWrapper &&
                            invWrapper.getInv() instanceof Inventory)) {
                return null;
            }
        } else if (!(slot.container instanceof Inventory)) {
            return null;
        }
        if (index < 9) return HOTBAR;
        if (index < 36) return MAIN_INVENTORY;
        if (index < 40) return ARMOR;
        return OFFHAND;
    }
}
