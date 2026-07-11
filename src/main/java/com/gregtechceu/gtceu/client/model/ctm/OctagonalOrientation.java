/*
 * This file is part of ConnectedTexturesMod (https://github.com/Chisel-Team/ConnectedTexturesMod).
 * Copyright (c) 2023 Chisel Team.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * ConnectedTexturesMod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with ConnectedTexturesMod; if not, If not, see <http://www.gnu.org/licenses/>.
 */
package com.gregtechceu.gtceu.client.model.ctm;

import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

import static net.minecraft.core.Direction.*;

/**
 * Think of this class as an octagonal {@link net.minecraft.core.Direction}.
 * <p>
 * It represents the eight different directions a face of a block can connect with CTM,
 * and contains the logic for determining if a block is indeed connected in that direction.
 * <p>
 * Note that, for example, {@link #TOP_RIGHT} does not mean connected to the {@link #TOP} and {@link #RIGHT},
 * but connected in the diagonal direction represented by {@link #TOP_RIGHT}.
 * This is used for inner corner rendering.
 * <p>
 * Sourced from <a href=
 * "https://github.com/Chisel-Team/ConnectedTexturesMod/blob/19a58b080ff2d4fec4fd44ffdb426fc078ce853d/src/main/java/team/chisel/ctm/client/util/Dir.java">ConnectedTexturesMod</a>.
 */
public enum OctagonalOrientation implements StringRepresentable {

    // spotless:off
    TOP(UP), 
    TOP_RIGHT(UP, EAST),
    RIGHT(EAST), 
    BOTTOM_RIGHT(DOWN, EAST), 
    BOTTOM(DOWN), 
    BOTTOM_LEFT(DOWN, WEST), 
    LEFT(WEST), 
    TOP_LEFT(UP, WEST);
    // spotless:on

    public static final OctagonalOrientation[] VALUES = values();
    private static final Direction NORMAL = SOUTH;

    static {
        // Run after static init
        for (OctagonalOrientation dir : OctagonalOrientation.VALUES) {
            dir.buildCaches();
        }
    }

    private final Direction[] dirs;

    private final BlockPos[] offsets = new BlockPos[6];

    OctagonalOrientation(Direction... dirs) {
        this.dirs = dirs;
    }

    private void buildCaches() {
        // Fill normalized dirs
        for (Direction normal : GTUtil.DIRECTIONS) {
            Direction[] normalized;
            if (normal == NORMAL) {
                normalized = dirs;
            } else if (normal == NORMAL.getOpposite()) {
                // If this is the opposite direction of the default normal, we need to mirror the dirs
                // A mirror version does not affect y+ and y- so we ignore those
                Direction[] ret = new Direction[dirs.length];
                for (int i = 0; i < ret.length; i++) {
                    ret[i] = dirs[i].getStepY() != 0 ? dirs[i] : dirs[i].getOpposite();
                }
                normalized = ret;
            } else {
                Direction axis;
                // Next, we need different a different rotation axis depending on if this is up/down or not
                if (normal.getAxis() != Axis.Y) {
                    // If it is not up/down, pick either the left or right-hand rotation
                    axis = normal == NORMAL.getClockWise() ? UP : DOWN;
                } else {
                    // If it is up/down, pick either the up or down rotation.
                    axis = normal == UP ? NORMAL.getCounterClockWise() : NORMAL.getClockWise();
                }
                Direction[] ret = new Direction[dirs.length];
                // Finally apply all the rotations
                for (int i = 0; i < ret.length; i++) {
                    ret[i] = rotate(dirs[i], axis);
                }
                normalized = ret;
            }
            BlockPos ret = BlockPos.ZERO;
            for (Direction dir : normalized) {
                ret = ret.relative(dir);
            }
            offsets[normal.ordinal()] = ret;
        }
    }

    /**
     * Finds if this block is connected for the given side in this OctagonalOrientation.
     *
     * @param level The level the block is in.
     * @param pos   The position of your block.
     * @param state The state of your block.
     * @param side  The side of the current face.
     * @return True if the block is connected in the given OctagonalOrientation, false otherwise.
     */
    public boolean isConnected(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side) {
        return ConnectionCheck.isConnected(level, pos, state, applyConnection(pos, side), side);
    }

    /**
     * Finds if this block is connected for the given side in this OctagonalOrientation.
     *
     * @param level           The level the block is in.
     * @param pos             The position of your block.
     * @param state           The state of your block.
     * @param side            The side of the current face.
     * @param connectionState The state to check for connection with.
     * @return True if the block is connected in the given OctagonalOrientation, false otherwise.
     */
    public boolean isConnected(BlockAndTintGetter level, BlockPos pos, BlockState state,
                               Direction side, BlockState connectionState) {
        return ConnectionCheck.isConnected(level, pos, state, applyConnection(pos, side), side, connectionState);
    }

    /**
     * Apply this OctagonalOrientation to the given BlockPos for the given normal direction.
     * 
     * @return The offset BlockPos
     */
    public BlockPos applyConnection(BlockPos pos, Direction side) {
        return pos.offset(getOffset(side));
    }

    public BlockPos getOffset(Direction normal) {
        return offsets[normal.ordinal()];
    }

    private Direction rotate(Direction facing, Direction axisFacing) {
        Direction.Axis axis = axisFacing.getAxis();
        AxisDirection axisDir = axisFacing.getAxisDirection();

        if (axisDir == AxisDirection.POSITIVE) {
            return facing.getClockWise(axis);
        } else {
            return facing.getCounterClockWise(axis);
        }
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
