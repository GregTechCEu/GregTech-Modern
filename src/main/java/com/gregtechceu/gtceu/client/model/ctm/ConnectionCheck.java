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

import com.gregtechceu.gtceu.client.model.ctm.CTMCache.StateComparisonCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

/**
 * Sourced from <a href=
 * "https://github.com/Chisel-Team/ConnectedTexturesMod/blob/19a58b080ff2d4fec4fd44ffdb426fc078ce853d/src/main/java/team/chisel/ctm/client/newctm/ConnectionCheck.java">ConnectedTexturesMod</a>.
 */
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
public class ConnectionCheck {

    public static final MapCodec<ConnectionCheck> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("ignore_states", false).forGetter(ConnectionCheck::ignoreStates))
            .apply(instance, ignoredStates -> new ConnectionCheck().ignoreStates(ignoredStates)));

    @Getter
    @Setter
    protected boolean ignoreStates;

    @Getter
    @Setter
    protected StateComparisonCallback stateComparator = StateComparisonCallback.DEFAULT;

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
    public final boolean isConnected(BlockAndTintGetter level, BlockPos current, BlockState currentState,
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
    @SuppressWarnings({ "unused", "null" })
    public boolean isConnected(BlockAndTintGetter level, BlockPos current, BlockState currentState, BlockPos connection,
                               Direction dir, BlockState state) {
        BlockState connectionState = getConnectionState(level, connection, level.getBlockState(connection), dir,
                current, currentState);
        BlockPos obscuringPos = connection.relative(dir);
        BlockState obscuring = getConnectionState(level, obscuringPos, level.getBlockState(obscuringPos),
                dir, current, currentState);

        // check that we aren't already connected to / from this side
        return stateComparator(state, connectionState, dir) && !stateComparator(state, obscuring, dir);
    }

    public boolean stateComparator(BlockState from, BlockState to, Direction dir) {
        return stateComparator.connects(this, from, to, dir);
    }

    public BlockState getConnectionState(BlockAndTintGetter level, BlockPos pos, @Nullable Direction side,
                                         BlockPos connection, BlockState connectionState) {
        return getConnectionState(level, pos, level.getBlockState(pos), side, connection, connectionState);
    }

    public BlockState getConnectionState(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                         @Nullable Direction side, BlockPos connection, BlockState connectionState) {
        if (side != null) {
            return state.getAppearance(level, pos, side, connectionState, connection);
        }
        return state;
    }
}
