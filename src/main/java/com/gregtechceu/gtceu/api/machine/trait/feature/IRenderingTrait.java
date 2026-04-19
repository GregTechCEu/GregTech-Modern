package com.gregtechceu.gtceu.api.machine.trait.feature;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A machine trait that overrides some of the default machine rendering behaviour.
 */
public interface IRenderingTrait {

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
    default boolean shouldRenderGridOverlay(Player player, BlockPos pos, BlockState state, ItemStack held,
                                            Set<GTToolType> toolTypes) {
        return false;
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
    default @Nullable ResourceTexture getGridOverlayIcon(Player player, BlockPos pos, BlockState state,
                                                         Set<GTToolType> toolTypes,
                                                         Direction side) {
        return null;
    }
}
