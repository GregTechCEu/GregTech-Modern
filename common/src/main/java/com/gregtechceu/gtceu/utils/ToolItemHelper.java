package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ToolItemHelper {

    /**
     * Attempts to get an electric item variant with override of max charge
     *
     * @param maxCharge new max charge of this electric item
     * @return item stack with given max charge
     * @throws java.lang.IllegalStateException if this item is not electric item or uses custom implementation
     */
    public static ItemStack getMaxChargeOverrideStack(Item item, long maxCharge) {
        ItemStack itemStack = item.getDefaultInstance();
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(itemStack);
        if (electricItem == null) {
            throw new IllegalStateException("Not an electric item.");
        }
        if (!(electricItem instanceof ElectricItem)) {
            throw new IllegalStateException("Only standard ElectricItem implementation supported, but this item uses " + electricItem.getClass());
        }
        ((ElectricItem) electricItem).setMaxChargeOverride(maxCharge);
        return itemStack;
    }
}
