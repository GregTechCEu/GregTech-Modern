package com.gregtechceu.gtceu.api.pipenet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;

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
    default BlockEntity getTargetBlockEntity(Level level) {
        return level.getBlockEntity(getTargetPipePos().relative(getTargetFacing()));
    }

    @Nullable
    default <I> I getTargetCapability(BlockCapability<I, Direction> capability, Level level) {
        return level.getCapability(capability, getTargetPipePos().relative(getTargetFacing()),
                getTargetFacing().getOpposite());
    }
}
