package com.gregtechceu.gtceu.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import lombok.Getter;

import java.util.Objects;

public class DimensionFacingPos {

    @Getter
    private final BlockPos pos;
    @Getter
    private final Direction facing;
    @Getter
    private final ResourceKey<Level> dimension;
    private final int hashCode;

    public DimensionFacingPos(BlockPos pos, Direction facing, ResourceKey<Level> dimension) {
        this.pos = pos;
        this.facing = facing;
        this.dimension = dimension;
        this.hashCode = Objects.hash(pos, facing, dimension);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DimensionFacingPos that = (DimensionFacingPos) o;
        return dimension == that.dimension && Objects.equals(pos, that.pos) &&
                facing == that.facing;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
