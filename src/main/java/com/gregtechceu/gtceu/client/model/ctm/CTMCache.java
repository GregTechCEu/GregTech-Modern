/*
 * This file is part of ConnectedTexturesMod (https://github.com/Chisel-Team/ConnectedTexturesMod).
 * Copyright (c) 2023  Chisel Team.

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * ConnectedTexturesMod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along
 * with ConnectedTexturesMod; if not, If not, see <http://www.gnu.org/licenses/>.
 */

package com.gregtechceu.gtceu.client.model.ctm;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.client.model.ctm.OctagonalOrientation.*;

// @formatter:off
/**
 * The CTM renderer will draw the block's FACE by assembling 4 quadrants from the 5 available block textures.
 * The normal {@code texture.png} is the block's "unconnected" texture, and is used when CTM is disabled or the block has nothing to connect to.
 * This texture has all the outside corner quadrants, and {@code texture_ctm.png} contains the rest of the quadrants.
 * <pre>
 * ┌─────────────────┐ ┌────────────────────────────────┐
 * │ texture.png     │ │ texture_ctm.png                │
 * │ ╔══════╤══════╗ │ │  ──────┼────── ║ ─────┼───── ║ │
 * │ ║      │      ║ │ │ │      │      │║      │      ║ │
 * │ ║ 16   │ 17   ║ │ │ │ 0    │ 1    │║ 2    │ 3    ║ │
 * │ ╟──────┼──────╢ │ │ ┼──────┼──────┼╟──────┼──────╢ │
 * │ ║      │      ║ │ │ │      │      │║      │      ║ │
 * │ ║ 18   │ 19   ║ │ │ │ 4    │ 5    │║ 6    │ 7    ║ │
 * │ ╚══════╧══════╝ │ │  ──────┼────── ║ ─────┼───── ║ │
 * └─────────────────┘ │ ═══════╤═══════╝ ─────┼───── ╚ │
 *                     │ │      │      ││      │      │ │
 *                     │ │ 8    │ 9    ││ 10   │ 11   │ │
 *                     │ ┼──────┼──────┼┼──────┼──────┼ │
 *                     │ │      │      ││      │      │ │
 *                     │ │ 12   │ 13   ││ 14   │ 15   │ │
 *                     │ ═══════╧═══════╗ ─────┼───── ╔ │
 *                     └────────────────────────────────┘
 * </pre>
 * combining { 18, 13,  9, 16 }, we can generate a texture connected to the right!
 * <pre>
 * ╔══════╤═══════
 * ║      │      │
 * ║ 16   │ 9    │
 * ╟──────┼──────┼
 * ║      │      │
 * ║ 18   │ 13   │
 * ╚══════╧═══════
 * </pre>
 *
 * combining { 18, 13, 11,  2 }, we can generate a texture, in the shape of an L (connected to the right, and up
 * <pre>
 * ║ ─────┼───── ╚
 * ║      │      │
 * ║ 2    │ 11   │
 * ╟──────┼──────┼
 * ║      │      │
 * ║ 18   │ 13   │
 * ╚══════╧═══════
 * </pre>
 *
 * HAVE FUN!
 * -CptRageToaster-
 * <p>
 * Sourced from <a href="https://github.com/Chisel-Team/ConnectedTexturesMod/blob/19a58b080ff2d4fec4fd44ffdb426fc078ce853d/src/main/java/team/chisel/ctm/client/util/CTMLogic.java">ConnectedTexturesMod</a>.
 */
@Accessors(fluent = true, chain = true)
public class CTMCache {

    @FunctionalInterface
    public interface StateComparisonCallback {
        
        StateComparisonCallback DEFAULT = (connectionCheck, from, to, dir) -> connectionCheck.ignoreStates() ? from.getBlock() == to.getBlock() : from == to;
        
        boolean connects(ConnectionCheck instance, BlockState from, BlockState to, Direction dir);
    }
	
    /**
     * The UVs for the specific "magic number" value
     */
    public static final ISubmap[] uvs = {
            // CTM texture
            Submap.fromPixelScale(4, 4, 0, 0),   // 0
            Submap.fromPixelScale(4, 4, 4, 0),   // 1
            Submap.fromPixelScale(4, 4, 8, 0),   // 2
            Submap.fromPixelScale(4, 4, 12, 0),  // 3
            Submap.fromPixelScale(4, 4, 0, 4),   // 4
            Submap.fromPixelScale(4, 4, 4, 4),   // 5
            Submap.fromPixelScale(4, 4, 8, 4),   // 6
            Submap.fromPixelScale(4, 4, 12, 4),  // 7
            Submap.fromPixelScale(4, 4, 0, 8),   // 8
            Submap.fromPixelScale(4, 4, 4, 8),   // 9
            Submap.fromPixelScale(4, 4, 8, 8),   // 10
            Submap.fromPixelScale(4, 4, 12, 8),  // 11
            Submap.fromPixelScale(4, 4, 0, 12),  // 12
            Submap.fromPixelScale(4, 4, 4, 12),  // 13
            Submap.fromPixelScale(4, 4, 8, 12),  // 14
            Submap.fromPixelScale(4, 4, 12, 12), // 15
            // Default texture
            Submap.fromPixelScale(8, 8, 0, 0),   // 16
            Submap.fromPixelScale(8, 8, 8, 0),   // 17
            Submap.fromPixelScale(8, 8, 0, 8),   // 18
            Submap.fromPixelScale(8, 8, 8, 8)    // 19
    };
    
    public static final ISubmap FULL_TEXTURE = Submap.X1;

    // @formatter:on

    /** Some hardcoded offset values for the different corner indeces */
    protected static int[] submapOffsets = { 4, 5, 1, 0 };
    protected static int[] defaultSubmapCache = { 18, 19, 17, 16 };

    // TODO encapsulate
    public ConnectionCheck connectionCheck = new ConnectionCheck();

    // Mapping the different corner indeces to their respective dirs
    protected static final OctagonalOrientation[][] submapMap = {
            { BOTTOM, LEFT, BOTTOM_LEFT },
            { BOTTOM, RIGHT, BOTTOM_RIGHT },
            { TOP, RIGHT, TOP_RIGHT },
            { TOP, LEFT, TOP_LEFT }
    };

    protected byte connectionMap;
    protected int[] submapCache = defaultSubmapCache.clone();


    public static CTMCache getInstance() {
        return new CTMCache();
    }

    /**
     * Indeces are in counter-clockwise order starting at bottom left.
     *
     * @return The indeces of the typical 4x4 submap to use for the given face at the given location.
     */
    public int[] getSubmapIds(@Nullable BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side) {
        if (level == null) {
            return this.submapCache;
        }

        buildConnectionMap(level, pos, state, side);
        // Map connections to submap indices
        for (int i = 0; i < 4; i++) {
            fillSubmaps(i);
        }

        return this.submapCache;
    }

    public int[] getSubmapIndices() {
        return this.submapCache;
    }

    public static boolean isDefaultTexture(int id) {
        return (id == 16 || id == 17 || id == 18 || id == 19);
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
     * Builds the connection map and stores it in this CTMLogic instance.
     * The {@link #connected(OctagonalOrientation)}, {@link #connectedAnd(OctagonalOrientation...)},
     * and {@link #connectedOr(OctagonalOrientation...)} methods can be used to access it.
     */
    public void buildConnectionMap(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction side) {
        // TODO this naive check doesn't work for models that have unculled faces.
        // Perhaps a smarter optimization could be done eventually?
        for (OctagonalOrientation dir : OctagonalOrientation.VALUES) {
            //Note: We can't cache the state that we are checking about connection for as we want to ensure that
            // we can take into account the side of the block we want to know the "state" of as if the block is
            // a facade of some sort it might return different results based on where it is being queried from
            setConnectedState(dir, dir.isConnected(this.connectionCheck, world, pos, state, side));
        }
    }

    @SuppressWarnings("null")
    protected void fillSubmaps(int idx) {
        OctagonalOrientation[] dirs = submapMap[idx];
        if (connectedOr(dirs[0], dirs[1])) {
            if (connectedAnd(dirs)) {
                // If all dirs are connected, we use the fully connected face, the base offset value.
                this.submapCache[idx] = submapOffsets[idx];
            } else {
                // This is a bit magic-y, but basically the array is ordered so
                // the first dir requires an offset of 2, and the second dir requires an offset of 8
                // plus the initial offset for the corner.
                this.submapCache[idx] = submapOffsets[idx] + (connected(dirs[0]) ? 2 : 0) + (connected(dirs[1]) ? 8 : 0);
            }
        }
    }

    /**
     * @param dir
     *            The direction to check connection in.
     * @return True if the cached connectionMap holds a connection in this {@link OctagonalOrientation direction}.
     */
    public boolean connected(OctagonalOrientation dir) {
        return ((this.connectionMap >> dir.ordinal()) & 1) == 1;
    }

    /**
     * @param dirs
     *            The directions to check connection in.
     * @return True if the cached connectionMap holds a connection in <i><b>all</b></i> the given {@link OctagonalOrientation directions}.
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
     *            The directions to check connection in.
     * @return True if the cached connectionMap holds a connection in <i><b>one of</b></i> the given {@link OctagonalOrientation directions}.
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
}
