package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.common.data.GTDataComponents;
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
        IElectricItem iElectricItem = GTCapabilityHelper.getElectricItem(itemStack);
        if (!(iElectricItem instanceof ElectricItem electricItem)) {
            throw new IllegalStateException("Not an electric item.");
        }
        electricItem.setMaxChargeOverride(maxCharge);
        return itemStack;
    }
}
