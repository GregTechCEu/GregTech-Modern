package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import org.jetbrains.annotations.Nullable;

public class SteamBoilerInfoProvider extends CapabilityInfoProvider<SteamBoilerMachine> {

    @Override
    protected @Nullable SteamBoilerMachine getCapability(Level level, BlockPos blockPos, @Nullable Direction side) {
        if (MetaMachine.getMachine(level, blockPos) instanceof SteamBoilerMachine steamBoilerMachine) {
            return steamBoilerMachine;
        }
        return null;
    }

    @Override
    protected void addProbeInfo(SteamBoilerMachine capability, IProbeInfo probeInfo, Player player,
                                BlockEntity blockEntity, IProbeHitData data) {
        IProbeInfo horizontalPane = probeInfo
                .horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));

        boolean isBurning = capability.getRecipeLogic().isWorking();
        boolean hasWater = !capability.isHasNoWater();
        long production = capability.getTotalSteamOutput();
        int temperature = capability.getCurrentTemperature();
        int maxTemperature = capability.getMaxTemperature();

        boolean makingSteam = hasWater && temperature >= 100;

        // Determine the first section
        MutableComponent root;
        if (isBurning && temperature < maxTemperature) {
            // Heating up
            root = Component.translatable("gtceu.machine.boiler.info.heating.up");
        } else if (!isBurning && temperature > 0) {
            // Cooling down
            root = Component.translatable("gtceu.machine.boiler.info.cooling.down");
        } else {
            root = null; // neither heating nor cooling, is either max temperature or temperature of zero
        }

        // Determine the second section
        MutableComponent extra;
        if (makingSteam) {
            // Producing some amount of steam
            extra = Component.translatable("gtceu.machine.boiler.info.production.data",
                    Component.literal(FormattingUtil.formatNumbers(production / 10))
                            .withStyle(ChatFormatting.GREEN));
            if (root != null) {
                // append some nice separation here to the root
                extra = Component.literal(" | ").append(extra);
            }
        } else if (temperature > 0 && temperature < 100) {
            // Still warming up (or cooling down)
            extra = Component.literal(String.format(" %s(%s%%)",
                    // Either heating up or cooling down
                    isBurning ? ChatFormatting.RED : ChatFormatting.BLUE,
                    temperature));
        } else {
            // Nothing to add
            extra = null;
        }

        if (root != null && extra != null) {
            horizontalPane.text(root.append(extra));
        } else if (root != null) {
            horizontalPane.text(root);
        } else if (extra != null) {
            horizontalPane.text(extra);
        }
    }

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("steam_boiler_info");
    }
}
