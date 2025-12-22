package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class GTMultiblockTextUtil {

    public static IKey addEnergyUsageLine(boolean formed, IEnergyContainer energyContainer) {
        if (formed && energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
            long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());

            String energyFormatted = FormattingUtil.formatNumbers(maxVoltage);
            // wrap in text component to keep it from being formatted
            byte voltageTier = GTUtil.getFloorTierByVoltage(maxVoltage);
            Component voltageName = Component.literal(
                    GTValues.VNF[voltageTier]);

            MutableComponent bodyText = Component.translatable("gtceu.multiblock.max_energy_per_tick",
                    energyFormatted, voltageName).withStyle(ChatFormatting.GRAY);
            Component hoverText = Component.translatable("gtceu.multiblock.max_energy_per_tick_hover")
                    .withStyle(ChatFormatting.GRAY);
            return IKey.dynamic(() -> bodyText.withStyle(
                    style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))));
        }
        return IKey.EMPTY;
    }

    public static IKey addEnergyTierLine(boolean formed, int tier) {

        if(!formed || tier < GTValues.ULV || tier > GTValues.MAX)
            return IKey.EMPTY;

        Component voltageName = Component.literal(GTValues.VNF[tier]);
        MutableComponent bodyText = Component.translatable(
                "gtceu.multiblock.max_recipe_tier",
                voltageName).withStyle(ChatFormatting.GRAY);
        Component hoverText = Component.translatable("gtceu.multiblock.max_recipe_tier_hover")
                .withStyle(ChatFormatting.GRAY);
        return IKey.dynamic(() -> bodyText
                .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))));
    }

    public static Component addProgressLine(boolean formed, boolean active, double currentDuration, double maxDuration, double progressPercent) {
        if (!formed || !active)
            return CommonComponents.EMPTY;

        int currentProgress = (int) (progressPercent * 100);
        double currentInSec = currentDuration / 20.0;
        double maxInSec = maxDuration / 20.0;
        return Component.translatable("gtceu.multiblock.progress",
                String.format("%.2f", (float) currentInSec),
                String.format("%.2f", (float) maxInSec), currentProgress);
    }


}
