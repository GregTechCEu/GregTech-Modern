package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote ISubItemHandler
 */
public interface ISubItemHandler extends IItemComponent {
    default void fillItemCategory(ComponentItem item, CreativeModeTab category, NonNullList<ItemStack> items) {
        items.add(new ItemStack(item));
    }
}
