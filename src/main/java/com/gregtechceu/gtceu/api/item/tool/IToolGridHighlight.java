package com.gregtechceu.gtceu.api.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.drawable.UITexture;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface IToolGridHighlight {

    /**
     * Called to determine if the grid overlay should be rendered on this machine.
     *
     * @param player    Player looking at this machine
     * @param pos       Block pos
     * @param state     Block state
     * @param held      Item that player is holding
     * @param toolTypes The GT tool types of the held item, if any
     * @return If the grid overlay should be drawn on the machine.
     */
    default boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held,
                                     Set<GTToolType> toolTypes) {
        return true;
    }

    /**
     * Called when the machine grid overlay is being rendered to determine the icon to be rendered within the grid
     * segment on a specifc side.
     *
     * @param player    Player looking at this machine
     * @param pos       Block pos
     * @param state     Block state
     * @param toolTypes The GT tool types of the held item, if any
     * @param side      The machine side which this grid segment correspond to
     * @return The icon to be rendered, or null
     */
    default @Nullable UITexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                         Direction side) {
        return null;
    }
}
