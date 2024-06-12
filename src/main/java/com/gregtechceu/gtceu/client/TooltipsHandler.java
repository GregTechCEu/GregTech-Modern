package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.HazardProperty;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/11
 * @implNote TooltipsHandler
 */
@OnlyIn(Dist.CLIENT)
public class TooltipsHandler {

    private static final String ITEM_PREFIX = "item." + GTCEu.MOD_ID;
    private static final String BLOCK_PREFIX = "block." + GTCEu.MOD_ID;

    public static void appendTooltips(ItemStack stack, TooltipFlag flag, List<Component> tooltips) {
        // Formula
        var unificationEntry = ChemicalHelper.getUnificationEntry(stack.getItem());
        if (unificationEntry != null && unificationEntry.material != null) {
            if (unificationEntry.material.getChemicalFormula() != null &&
                    !unificationEntry.material.getChemicalFormula().isEmpty())
                tooltips.add(1, Component.literal(unificationEntry.material.getChemicalFormula())
                        .withStyle(ChatFormatting.YELLOW));
        }

        // Block/Item custom tooltips
        String translationKey = stack.getDescriptionId();
        if (translationKey.startsWith(ITEM_PREFIX) || translationKey.startsWith(BLOCK_PREFIX)) {
            String tooltipKey = translationKey + ".tooltip";
            if (I18n.exists(tooltipKey)) {
                tooltips.add(1, Component.translatable(tooltipKey));
            } else {
                List<MutableComponent> multiLang = LangHandler.getMultiLang(tooltipKey);
                if (multiLang != null && !multiLang.isEmpty()) {
                    tooltips.addAll(1, multiLang);
                }
            }
        }

        Material material = HazardProperty.getValidHazardMaterial(stack);
        if (material == null) {
            return;
        }
        GTUtil.appendHazardTooltips(material, tooltips);
    }

    public static void appendFluidTooltips(Fluid fluid, List<Component> tooltips, TooltipFlag flag) {
        var material = ChemicalHelper.getMaterial(fluid);
        if (material != null) {
            if (material.getChemicalFormula() != null && !material.getChemicalFormula().isEmpty())
                tooltips.add(1, Component.literal(material.getChemicalFormula()).withStyle(ChatFormatting.YELLOW));
        }
    }
}
