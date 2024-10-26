package com.gregtechceu.gtceu.integration.ae2.slot;

import appeng.api.stacks.GenericStack;

public interface IConfigurableSlotList {

    IConfigurableSlot getConfigurableSlot(int index);

    int getConfigurableSlots();

    default boolean hasStackInConfig(GenericStack stack, boolean checkExternal) {
        if (stack == null || stack.amount() <= 0) return false;
        for (int i = 0; i < getConfigurableSlots(); i++) {
            var slot = getConfigurableSlot(i);
            GenericStack config = slot.getConfig();
            if (config != null && config.what().equals(stack.what())) {
                return true;
            }
        }
        return false;
    }

    default void clearInventory(int startIndex) {
        for (int i = startIndex; i < getConfigurableSlots(); i++) {
            var slot = getConfigurableSlot(i);
            slot.setConfig(null);
            slot.setStock(null);
        }
    }
}
