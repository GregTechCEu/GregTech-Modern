package com.gregtechceu.gtceu.api.item.tool;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/3/2
 */
public interface IToolGridHighLight {

    default boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held,
                                     Set<GTToolType> toolTypes) {
        return true;
    }

    @Nullable
    default ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                     Direction side) {
        return null;
    }
}
