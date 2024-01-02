package com.gregtechceu.gtceu.api.item.tool;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/3/2
 * @implNote IBlockGridHighLight
 */
public interface IToolGridHighLight {
    default boolean shouldRenderGrid(Player player, ItemStack held, Set<GTToolType> toolTypes) {
        return true;
    }

    @Nullable
    default ResourceTexture sideTips(Player player, Set<GTToolType> toolTypes, Direction side) {
        return null;
    }

}
