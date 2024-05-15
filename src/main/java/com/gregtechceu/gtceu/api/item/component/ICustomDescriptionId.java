package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote ICustomDescriptionId
 */
public interface ICustomDescriptionId extends IItemComponent {

    String getItemStackDisplayName(ItemStack itemStack);
}
