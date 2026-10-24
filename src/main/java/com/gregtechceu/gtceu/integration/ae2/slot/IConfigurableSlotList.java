package com.gregtechceu.gtceu.integration.ae2.slot;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;

public interface IConfigurableSlotList {

    IConfigurableSlot getConfigurableSlot(int index);

    int getConfigurableSlots();

    void onContentsChanged();

    default boolean isStocking() {
        return false;
    }

    default boolean isConfigStackAllowed(@Nullable GenericStack stack) {
        if (stack == null || stack.amount() <= 0) {
            return true;
        }
        if (!isStocking()) {
            return true;
        }
        return !hasStackInConfig(stack, true);
    }

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
