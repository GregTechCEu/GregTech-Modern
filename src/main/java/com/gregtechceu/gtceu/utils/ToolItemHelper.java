package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ToolItemHelper {

    public static final Map<GTToolType, ItemStack> TOOL_CACHE = new HashMap<>();

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
            throw new IllegalStateException("Only standard ElectricItem implementation supported, but this item uses " +
                    electricItem.getClass());
        }
        ((ElectricItem) electricItem).setMaxChargeOverride(maxCharge);
        return itemStack;
    }

    /**
     * get tool itemStack by GTToolType with default Material
     *
     * @param toolType GTToolType
     * @return the tool itemStack
     */
    public static ItemStack getToolItem(GTToolType toolType) {
        return TOOL_CACHE.computeIfAbsent(toolType, type -> {
            if (type == GTToolType.SOFT_MALLET) {
                return GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Rubber, type).asStack();
            }
            return GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Neutronium, type).asStack();
        });
    }
}
