package com.gregtechceu.gtceu.integration.top.provider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import org.jetbrains.annotations.Nullable;

public abstract class CapabilityInfoProvider<T> implements IProbeInfoProvider {

    @Nullable
    protected abstract T getCapability(Level level, BlockPos pos, @Nullable Direction side);

    protected abstract void addProbeInfo(T capability, IProbeInfo probeInfo, Player player, BlockEntity blockEntity,
                                         IProbeHitData data);

    protected boolean allowDisplaying(T capability) {
        return true;
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState,
                             IProbeHitData data) {
        if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(data.getPos());
            if (blockEntity == null) return;
            T resultCapability = getCapability(world, data.getPos(), data.getSideHit());
            if (resultCapability != null && allowDisplaying(resultCapability)) {
                addProbeInfo(resultCapability, probeInfo, player, blockEntity, data);
            }
        }
    }
}
