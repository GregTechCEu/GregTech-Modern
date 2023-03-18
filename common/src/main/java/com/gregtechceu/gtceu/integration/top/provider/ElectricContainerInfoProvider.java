package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ElectricContainerInfoProvider extends CapabilityInfoProvider<IEnergyContainer> {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("energy_container_provider");
    }

    @Nullable
    @Override
    protected IEnergyContainer getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapabilityHelper.getEnergyContainer(level, pos, side);
    }

    @Override
    protected void addProbeInfo(IEnergyContainer capability, IProbeInfo probeInfo, Player player, BlockEntity blockEntity, IProbeHitData data) {
        long maxStorage = capability.getEnergyCapacity();
        if (maxStorage == 0) return; // do not add empty max storage progress bar
        probeInfo.progress(capability.getEnergyStored(), maxStorage, probeInfo.defaultProgressStyle()
                .suffix(" / " + maxStorage + " EU")
                .filledColor(0xFFEEE600)
                .alternateFilledColor(0xFFEEE600)
                .borderColor(0xFF555555));
    }

    @Override
    protected boolean allowDisplaying(@Nonnull IEnergyContainer capability) {
        return !capability.isOneProbeHidden();
    }

}
