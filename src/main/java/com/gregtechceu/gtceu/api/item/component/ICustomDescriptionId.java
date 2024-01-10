package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.world.item.ItemStack;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote ICustomDescriptionId
 */
public interface ICustomDescriptionId extends IItemComponent{
    String getItemStackDisplayName(ItemStack itemStack);

}
