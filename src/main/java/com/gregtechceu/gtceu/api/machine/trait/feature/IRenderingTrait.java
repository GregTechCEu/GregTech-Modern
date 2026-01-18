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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

/**
 * A machine trait that overrides some of the default machine rendering behaviour.
 */
@ParametersAreNonnullByDefault
public interface IRenderingTrait extends ITraitFeature {

    default boolean shouldRenderGridOverlay(Player player, BlockPos pos, BlockState state, ItemStack held,
                                            Set<GTToolType> toolTypes) {
        return false;
    }

    default @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                               Direction side) {
        return null;
    }

    default void updateModelData(ModelData.Builder builder) {}
}
