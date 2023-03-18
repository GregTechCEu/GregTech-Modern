package com.gregtechceu.gtceu.api.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote IItemUseFirst
 */
public interface IItemUseFirst {
    InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context);

}
