package com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;

public interface IMEStockingPart extends IAutoPullPart {

    @Override
    default void addedToController(IMultiController controller) {
        // ensure that no other stocking bus on this multiblock is configured to hold the same item.
        // that we have in our own bus.
        setAutoPullTest(stack -> !this.testConfiguredInOtherPart(stack));
        // also ensure that our current config is valid given other inputs
        if (self().getLevel() instanceof ServerLevel serverLevel) {
            // wait for 1 tick
            // we should not access the part list at this time
            serverLevel.getServer().tell(new TickTask(0, this::validateConfig));
        }
    }

    @Override
    default void removedFromController(IMultiController controller) {
        setAutoPullTest($ -> false);
        if (isAutoPull()) {
            getSlotList().clearInventory(0);
        }
    }

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

    int getMinStackSize();

    void setMinStackSize(int newSize);

    int getTicksPerCycle();

    void setTicksPerCycle(int newSize);
}
