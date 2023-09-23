package com.gregtechceu.gtceu.common.pipelike.laser;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Direction;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class LaserPipeProperties {
    public final Direction.Axis axis;
    public LaserPipeProperties(@Nonnull LaserPipeProperties other) {
        this.axis = other.axis;
    }
}
