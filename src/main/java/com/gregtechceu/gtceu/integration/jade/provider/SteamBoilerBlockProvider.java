package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class SteamBoilerBlockProvider extends BlockInfoProvider<SteamBoilerMachine> {

    public SteamBoilerBlockProvider() {
        super(GTCEu.id("steam_boiler_info"));
    }

    @Nullable
    @Override
    protected SteamBoilerMachine getCapability(Level level, BlockPos blockPos) {
        if (MetaMachine.getMachine(level, blockPos) instanceof SteamBoilerMachine steamBoilerMachine) {
            return steamBoilerMachine;
        }
        return null;
    }

    @Override
    protected void write(CompoundTag data, SteamBoilerMachine capability, BlockAccessor block) {
        data.putBoolean("heatingUp", capability.getRecipeLogic().isWorking());
        data.putBoolean("coolingDown", capability.getCurrentTemperature() > 0);
        data.putBoolean("producingSteam", !capability.isHasNoWater() && capability.getCurrentTemperature() >= 100);
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        var producing = capData.getBoolean("producingSteam");
        if (capData.getBoolean("heatingUp")) {
            tooltip.add(Component.translatable("gtceu.machine.boiler.info.heating.up",
                    producing ? Component.translatable("gtceu.machine.boiler.info.producing.steam") : ""));
        } else if (capData.getBoolean("coolingDown")) {
            tooltip.add(Component.translatable("gtceu.machine.boiler.info.cooling.down",
                    producing ? Component.translatable("gtceu.machine.boiler.info.producing.steam") : ""));
        }
    }
}
