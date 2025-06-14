package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ISubItemHandler extends IItemComponent {

    default void fillItemCategory(Item item, CreativeModeTab category, NonNullList<ItemStack> items) {
        items.add(new ItemStack(item));
    }
}
