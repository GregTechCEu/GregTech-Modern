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

import com.gregtechceu.gtceu.utils.ArrayHelpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import static com.gregtechceu.gtceu.client.model.ctm.OctagonalOrientation.*;

// spotless:off
/**
 * The CTM renderer will draw the block's FACE by assembling 4 quadrants from the 5 available block textures.
 * The normal {@code texture.png} is the block's "unconnected" texture, and is used when CTM is disabled or the block
 * has nothing to connect to.
 * This texture has all the outside corner quadrants, and {@code texture_ctm.png} contains the rest of the quadrants.
 * <pre>
 * ┌─────────────────┐ ┌────────────────────────────────┐
 * │ texture.png     │ │ texture_ctm.png                │
 * │ ╔══════╤══════╗ │ │  ──────┼────── ║ ─────┼───── ║ │
 * │ ║      │      ║ │ │ │      │      │║      │      ║ │
 * │ ║ 4/4  │ 4/5  ║ │ │ │ 0/0  │ 0/1  │║ 0/2  │ 0/3  ║ │
 * │ ╟──────┼──────╢ │ │ ┼──────┼──────┼╟──────┼──────╢ │
 * │ ║      │      ║ │ │ │      │      │║      │      ║ │
 * │ ║ 5/4  │ 5/5  ║ │ │ │ 1/0  │ 1/1  │║ 1/2  │ 1/3  ║ │
 * │ ╚══════╧══════╝ │ │  ──────┼────── ║ ─────┼───── ║ │
 * └─────────────────┘ │ ═══════╤═══════╝ ─────┼───── ╚ │
 *                     │ │      │      ││      │      │ │
 *                     │ │ 2/0  │ 2/1  ││ 2/2  │ 2/3  │ │
 *                     │ ┼──────┼──────┼┼──────┼──────┼ │
 *                     │ │      │      ││      │      │ │
 *                     │ │ 3/0  │ 3/1  ││ 3/2  │ 3/3  │ │
 *                     │ ═══════╧═══════╗ ─────┼───── ╔ │
 *                     └────────────────────────────────┘
 * </pre>
 * combining { { 5/4, 3/1 }, { 2/1, 4/4 } }, we can generate a texture connected to the right!
 * <pre>
 * ╔══════╤═══════
 * ║      │      │
 * ║ 4/4  │ 2/1  │
 * ╟──────┼──────┼
 * ║      │      │
 * ║ 5/4  │ 3/1  │
 * ╚══════╧═══════
 * </pre>
 *
 * combining { { 5/4, 3/1 }, { 0/2, 2/3 } }, we can generate a texture in the shape of an L
 * (connected to the right and up)
 * <pre>
 * ║ ─────┼───── ╚
 * ║      │      │
 * ║ 0/2  │ 2/3  │
 * ╟──────┼──────┼
 * ║      │      │
 * ║ 5/4  │ 3/1  │
 * ╚══════╧═══════
 * </pre>
 *
 * HAVE FUN!
 * -CptRageToaster-
 * <p>
 * Sourced from <a href="https://github.com/Chisel-Team/ConnectedTexturesMod/blob/19a58b080ff2d4fec4fd44ffdb426fc078ce853d/src/main/java/team/chisel/ctm/client/util/CTMLogic.java">ConnectedTexturesMod</a>.
 */
// spotless:on
@Accessors(fluent = true, chain = true)
public class CTMCache {

    @FunctionalInterface
    public interface StateComparisonCallback {

        StateComparisonCallback DEFAULT = (connectionCheck, from, to, dir) -> {
            return connectionCheck.ignoreStates() ? from.getBlock() == to.getBlock() : from == to;
        };

        boolean connects(ConnectionCheck instance, BlockState from, BlockState to, Direction dir);
    }

    /** Hardcoded offset values for the different submap indices */
    // store the full table(s) to reduce non-required allocations
    protected static final Vector2ic[][] submapOffsets = {
            { new Vector2i(0, 0), new Vector2i(0, 1), new Vector2i(0, 2), new Vector2i(0, 3), },
            { new Vector2i(1, 0), new Vector2i(1, 1), new Vector2i(1, 2), new Vector2i(1, 3), },
            { new Vector2i(2, 0), new Vector2i(2, 1), new Vector2i(2, 2), new Vector2i(2, 3), },
            { new Vector2i(3, 0), new Vector2i(3, 1), new Vector2i(3, 2), new Vector2i(3, 3), },
    };
    protected static final Vector2ic[][] defaultSubmapCache = {
            { new Vector2i(4, 4), new Vector2i(4, 5), },
            { new Vector2i(5, 4), new Vector2i(5, 5), },
    };

    public ConnectionCheck connectionCheck = new ConnectionCheck();

    // spotless:off
    // Mapping the different corner indices to their respective dirs
    protected static final OctagonalOrientation[][][] submapMap = {
            { { LEFT, TOP, TOP_LEFT },       { RIGHT, TOP, TOP_RIGHT },       },
            { { LEFT, BOTTOM, BOTTOM_LEFT }, { RIGHT, BOTTOM, BOTTOM_RIGHT }, },
    };
    // spotless:on

    protected byte connectionMap;
    protected Vector2ic[][] submapCache = ArrayHelpers.deepCopy(defaultSubmapCache);

    public static CTMCache getInstance() {
        return new CTMCache();
    }

    /**
     * Indeces are in counter-clockwise order starting at bottom left.
     *
     * @return The indeces of the typical 4x4 submap to use for the given face at the given location.
     */
    public Vector2ic[][] fillSubmapCache(@Nullable BlockAndTintGetter level, BlockPos pos,
                                         BlockState state, Direction side) {
        if (level == null) {
            return this.submapCache;
        }

        buildConnectionMap(level, pos, state, side);
        // Map connections to submap indices
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                fillSubmaps(x, y);
            }
        }

        return this.submapCache;
    }

    public Vector2ic[][] getCachedSubmapIndices() {
        return this.submapCache;
    }

    public static boolean isDefaultTexture(Vector2ic id) {
        return id.x() >= 4 && id.y() >= 4;
    }

    public static ISubmap getSubmapFor(Vector2ic coordinates) {
        if (isDefaultTexture(coordinates)) {
            return Submap.X2[coordinates.x() % 4][coordinates.y() % 4];
        } else {
            return Submap.X4[coordinates.x()][coordinates.y()];
        }
    }

    protected void setConnectedState(OctagonalOrientation dir, boolean connected) {
        this.connectionMap = setConnectedState(this.connectionMap, dir, connected);
    }

    private static byte setConnectedState(byte map, OctagonalOrientation dir, boolean connected) {
        if (connected) {
            return (byte) (map | (1 << dir.ordinal()));
        } else {
            return (byte) (map & ~(1 << dir.ordinal()));
        }
    }

    /**
     * Builds the connection map and stores it in this CTMLogic instance.<br>
     * The {@link #connected(OctagonalOrientation)}, {@link #connectedAnd(OctagonalOrientation...)},
     * and {@link #connectedOr(OctagonalOrientation...)} methods can be used to access it.
     */
    public void buildConnectionMap(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction side) {
        // TODO this naive check doesn't work for models that have unculled faces.
        // Perhaps a smarter optimization could be done eventually?
        for (OctagonalOrientation dir : OctagonalOrientation.VALUES) {
            // Note: We can't cache the state that we are checking about connection for as we want to ensure that
            // we can take into account the side of the block we want to know the "state" of as if the block is
            // a facade of some sort it might return different results based on where it is being queried from
            setConnectedState(dir, dir.isConnected(this.connectionCheck, world, pos, state, side));
        }
    }

    @SuppressWarnings("null")
    protected void fillSubmaps(int x, int y) {
        // [0] is horizontal, [1] is vertical, [2] is diagonal
        OctagonalOrientation[] dirs = submapMap[x][y];
        if (connectedOr(dirs[0], dirs[1])) {
            if (connectedAnd(dirs)) {
                // If all dirs are connected, we use the fully connected face, the base offset value.
                this.submapCache[x][y] = submapOffsets[x][y];
            } else {
                this.submapCache[x][y] = submapOffsets[x + (connected(dirs[0]) ? 2 : 0)][y +
                        (connected(dirs[1]) ? 2 : 0)];
            }
        }
    }

    /**
     * @param dir The direction to check connection in.
     * @return True if the cached connectionMap holds a connection in this {@link OctagonalOrientation direction}.
     */
    public boolean connected(OctagonalOrientation dir) {
        return ((this.connectionMap >> dir.ordinal()) & 1) == 1;
    }

    /**
     * @param dirs The directions to check connection in.
     * @return True if the cached connectionMap holds a connection in <i><b>all</b></i> the given
     *         {@link OctagonalOrientation directions}.
     */
    @SuppressWarnings("null")
    public boolean connectedAnd(OctagonalOrientation... dirs) {
        for (OctagonalOrientation dir : dirs) {
            if (!connected(dir)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param dirs
     *             The directions to check connection in.
     * @return True if the cached connectionMap holds a connection in <i><b>one of</b></i> the given
     *         {@link OctagonalOrientation directions}.
     */
    @SuppressWarnings("null")
    public boolean connectedOr(OctagonalOrientation... dirs) {
        for (OctagonalOrientation dir : dirs) {
            if (connected(dir)) {
                return true;
            }
        }
        return false;
    }

    public boolean connectedNone(OctagonalOrientation... dirs) {
        for (OctagonalOrientation dir : dirs) {
            if (connected(dir)) {
                return false;
            }
        }
        return true;
    }

    public boolean connectedOnly(OctagonalOrientation... dirs) {
        byte map = 0;
        for (OctagonalOrientation dir : dirs) {
            map = setConnectedState(map, dir, true);
        }
        return map == this.connectionMap;
    }

    @Override
    public int hashCode() {
        return this.connectionMap;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CTMCache other)) return false;
        return this.connectionMap == other.connectionMap;
    }
}
