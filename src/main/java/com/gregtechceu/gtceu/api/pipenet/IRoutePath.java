package com.gregtechceu.gtceu.api.pipenet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRoutePath<T> {

    @NotNull
    BlockPos getTargetPipePos();

    @NotNull
    Direction getTargetFacing();

    int getDistance();

    @Nullable
    T getHandler(Level world);

    @Nullable
    default BlockEntity getTargetTileEntity(Level level) {
        return level.getBlockEntity(getTargetPipePos().relative(getTargetFacing()));
    }

    @Nullable
    default <I> I getTargetCapability(Capability<I> capability, Level level) {
        BlockEntity tile = getTargetTileEntity(level);
        return tile == null ? null : tile.getCapability(capability, getTargetFacing().getOpposite()).resolve().orElse(null);
    }
}