package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/11
 * @implNote TooltipsHandler
 */
@Environment(EnvType.CLIENT)
public class TooltipsHandler {
    public static void appendTooltips(ItemStack stack, TooltipFlag flag, List<Component> tooltips) {
        // Energy Item
        var energyItem = GTCapabilityHelper.getElectricItem(stack);
        if (energyItem != null) {
            tooltips.add(1, Component.translatable("metaitem.generic.electric_item.stored",
                    energyItem.getCharge(),
                    energyItem.getMaxCharge(),
                    Component.literal(String.format("%.2f%%", energyItem.getCharge() * 100f / energyItem.getMaxCharge())).withStyle(ChatFormatting.GREEN)));
        }

        // Formula
        var unificationEntry = ChemicalHelper.getUnificationEntry(stack.getItem());

        if (unificationEntry != null && unificationEntry.material != null) {
            if (unificationEntry.material.getChemicalFormula() != null && !unificationEntry.material.getChemicalFormula().isEmpty())
                tooltips.add(Component.literal(unificationEntry.material.getChemicalFormula()).withStyle(ChatFormatting.YELLOW));
        }
    }
}
