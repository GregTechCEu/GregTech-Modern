package com.gregtechceu.gtceu.api.item.tool;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * @author KilaBash
 * @date 2023/3/2
 * @implNote IBlockGridHighLight
 */
public interface IToolGridHighLight {
    boolean shouldRenderGrid(Player player, ItemStack held, GTToolType toolType);

    boolean isSideUsed(Player player, GTToolType toolType, Direction side);

}
