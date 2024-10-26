package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IEnvironmentalHazardCleaner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.apiimpl.elements.ElementText;
import org.jetbrains.annotations.Nullable;

public class HazardCleanerInfoProvider extends CapabilityInfoProvider<IEnvironmentalHazardCleaner> {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("hazard_cleaner_provider");
    }

    @Nullable
    @Override
    protected IEnvironmentalHazardCleaner getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getBlockEntity(pos) instanceof MetaMachineBlockEntity mte &&
                mte.getMetaMachine() instanceof IEnvironmentalHazardCleaner cleaner ? cleaner : null;
    }

    @Override
    protected void addProbeInfo(IEnvironmentalHazardCleaner capability, IProbeInfo probeInfo, Player player,
                                BlockEntity blockEntity, IProbeHitData data) {
        float cleaned = capability.getRemovedLastSecond();
        probeInfo.element(new ElementText(Component.translatable("gtceu.jade.cleaned_this_second", cleaned),
                probeInfo.defaultTextStyle()));
    }
}
