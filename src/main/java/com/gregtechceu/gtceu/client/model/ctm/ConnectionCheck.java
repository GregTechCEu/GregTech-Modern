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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

/**
 * Sourced from <a href=
 * "https://github.com/Chisel-Team/ConnectedTexturesMod/blob/19a58b080ff2d4fec4fd44ffdb426fc078ce853d/src/main/java/team/chisel/ctm/client/newctm/ConnectionCheck.java">ConnectedTexturesMod</a>
 * with considerable simplification.
 */
@UtilityClass
public class ConnectionCheck {

    /**
     * A simple check for if the given block can connect to the given direction on the given side.
     * 
     * @param level        The level the positions are in.
     * @param current      The position of your block.
     * @param currentState The current state of your block.
     * @param connection   The position of the block to check against.
     * @param dir          The {@link Direction side} of the block to check for connection status.
     *                     This is <i>not</i> the direction to check in.
     * @return True if the given block can connect to the given location on the given side.
     */
    public static boolean isConnected(BlockAndTintGetter level, BlockPos current, BlockState currentState,
                                      BlockPos connection, Direction dir) {
        BlockState state = getConnectionState(level, current, currentState,
                dir, connection, level.getBlockState(connection));
        return isConnected(level, current, currentState, connection, dir, state);
    }

    /**
     * A simple check for if the given block can connect to the given direction on the given side.
     * 
     * @param level      The level the positions are in.
     * @param current    The position of your block.
     * @param connection The position of the block to check against.
     * @param dir        The {@link Direction side} of the block to check for connection status.
     *                   This is <i>not</i> the direction to check in.
     * @param state      The state to check against for connection.
     * @return True if the given block can connect to the given location on the given side.
     */
    public static boolean isConnected(BlockAndTintGetter level, BlockPos current, BlockState currentState,
                                      BlockPos connection,
                                      Direction dir, BlockState state) {
        BlockState connectionState = getConnectionState(level, connection, level.getBlockState(connection), dir,
                current, currentState);
        BlockPos obscuringPos = connection.relative(dir);
        BlockState obscuring = getConnectionState(level, obscuringPos, level.getBlockState(obscuringPos),
                dir, current, currentState);

        // check that we are connected AND aren't already connected outwards from this side
        return state == connectionState && state != obscuring;
    }

    public static BlockState getConnectionState(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                                @Nullable Direction side, BlockPos connection,
                                                BlockState connectionState) {
        if (side != null) {
            return state.getAppearance(level, pos, side, connectionState, connection);
        }
        return state;
    }
}
