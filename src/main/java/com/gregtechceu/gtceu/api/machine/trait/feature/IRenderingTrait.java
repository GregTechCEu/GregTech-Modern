package com.gregtechceu.gtceu.api.machine.trait.feature;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A machine trait that overrides some of the default machine rendering behaviour.
 */
@ParametersAreNonnullByDefault
public interface IRenderingTrait extends ITraitFeature {

    /**
     * Called when a player is looking at this machine, returns whether the grid overlay should be rendered.
     */
    default boolean shouldRenderGridOverlay(Player player, BlockPos pos, BlockState state, ItemStack held,
                                            Set<GTToolType> toolTypes) {
        return false;
    }

    /**
     * Called when the machine grid overlay is being rendered to determine the icon to be rendered within the grid
     * segment on a specifc side.
     */
    default @Nullable ResourceTexture getGridOverlayIcon(Player player, BlockPos pos, BlockState state,
                                                         Set<GTToolType> toolTypes,
                                                         Direction side) {
        return null;
    }

    default void updateModelData(ModelData.Builder builder) {}
}
