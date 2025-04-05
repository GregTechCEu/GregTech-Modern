package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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

        var producingSteam = !capability.isHasNoWater() && capability.getCurrentTemperature() >= 100;
        if (capability.getRecipeLogic().isWorking()) {
            horizontalPane.text("gtceu.machine.boiler.info.heating.up",
                    producingSteam ? Component.translatable("gtceu.machine.boiler.info.producing.steam") : "");
        } else if (capability.getCurrentTemperature() > 0) {
            horizontalPane.text("gtceu.machine.boiler.info.cooling.down",
                    producingSteam ? Component.translatable("gtceu.machine.boiler.info.producing.steam") : "");
        }
    }

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("steam_boiler_info");
    }
}
