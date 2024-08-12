package com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;

public interface IMEStockingPart extends IMultiPart {

    IConfigurableSlotList getSlotList();

    /**
     * @return True if the passed stack is found as a configuration in any other stocking buses on the multiblock.
     */
    boolean testConfiguredInOtherPart(@Nullable GenericStack config);

    /**
     * Test for if any of our configured items are in another stocking bus on the multi
     * we are attached to. Prevents dupes in certain situations.
     */
    default void validateConfig() {
        var slots = getSlotList();
        for (int i = 0; i < slots.getConfigurableSlots(); i++) {
            var slot = slots.getConfigurableSlot(i);
            if (slot.getConfig() != null) {
                GenericStack configuredStack = slot.getConfig();
                if (testConfiguredInOtherPart(configuredStack)) {
                    slot.setConfig(null);
                    slot.setStock(null);
                }
            }
        }
    }
}
