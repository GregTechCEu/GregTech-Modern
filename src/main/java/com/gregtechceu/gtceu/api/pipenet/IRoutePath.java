package com.gregtechceu.gtceu.api.pipenet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
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
}