package com.gregtechceu.gtceu.common.pipelike.laser;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.api.pipenet.IRoutePath;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaserRoutePath implements IRoutePath<ILaserContainer> {

    @Getter
    private final BlockPos targetPipePos;
    /**
     * the current face to handler
     */
    @NotNull
    @Getter
    private final Direction targetFacing;
    /**
     * the manhattan distance traveled during walking
     */
    @Getter
    private final int distance;
    @Getter
    byte connections;

    public LaserRoutePath(BlockPos targetPipePos, Direction targetFacing, int distance) {
        this.targetPipePos = targetPipePos;
        this.targetFacing = targetFacing;
        this.distance = distance;
    }

    /**
     * Gets the handler if it exists
     *
     * @return the handler
     */
    @Nullable
    public ILaserContainer getHandler(Level level) {
        return GTCapabilityHelper.getLaser(level, getTargetPipePos().relative(targetFacing),
                targetFacing.getOpposite());
    }
}
