package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.trait.hazard.EnvironmentalHazardCleanerTrait;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class HazardCleanerBlockProvider extends MachineTraitProvider<EnvironmentalHazardCleanerTrait> {

    public HazardCleanerBlockProvider() {
        super(GTCEu.id("hazard_cleaner_provider"), EnvironmentalHazardCleanerTrait.TYPE);
    }

    @Override
    protected void write(CompoundTag data, BlockAccessor block, EnvironmentalHazardCleanerTrait capability) {
        data.putFloat("Cleaned", capability.getRemovedLastSecond());
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        float cleaned = capData.getFloat("Cleaned");
        if (cleaned > 0) {
            tooltip.add(Component.translatable("gtceu.jade.cleaned_this_second", cleaned));
        }
    }
}
