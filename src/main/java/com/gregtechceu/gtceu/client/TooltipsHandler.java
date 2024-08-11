package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.FluidConstants;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidType;

import java.util.List;
import java.util.function.Consumer;

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

    public static void appendFluidTooltips(Fluid fluid, long amount, Consumer<Component> tooltips, TooltipFlag flag) {
        FluidType fluidType = fluid.getFluidType();
        var material = ChemicalHelper.getMaterial(fluid);
        if (material != null) {
            if (material.getChemicalFormula() != null && !material.getChemicalFormula().isEmpty())
                tooltips.accept(Component.literal(material.getChemicalFormula()).withStyle(ChatFormatting.YELLOW));

            if (material.hasProperty(PropertyKey.INGOT)) {
                if (GTUtil.isShiftDown() && amount >= GTValues.L) {
                    long ingots = amount / GTValues.L;
                    long remainder = amount % GTValues.L;
                    String fluidAmount = String.format(" %,d mB = %,d * %d mB", amount, ingots, GTValues.L);
                    if (remainder != 0) {
                        fluidAmount += String.format(" + %d mB", remainder);
                    }
                    tooltips.accept(Component.translatable("gtceu.gui.fluid_amount").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(fluidAmount)));
                }
            }

            if (fluid instanceof GTFluid attributedFluid) {
                FluidState state = attributedFluid.getState();
                switch (state) {
                    case LIQUID -> tooltips.accept(Component.translatable("gtceu.fluid.state_liquid"));
                    case GAS -> tooltips.accept(Component.translatable("gtceu.fluid.state_gas"));
                    case PLASMA -> tooltips.accept(Component.translatable("gtceu.fluid.state_plasma"));
                }

                attributedFluid.getAttributes().forEach(a -> a.appendFluidTooltips(tooltips));
            }
            tooltips.accept(Component.translatable("gtceu.fluid.temperature", fluidType.getTemperature()));
            if (fluidType.getTemperature() < FluidConstants.CRYOGENIC_FLUID_THRESHOLD) {
                tooltips.accept(Component.translatable("gtceu.fluid.temperature.cryogenic"));
            }
        }
    }
}
