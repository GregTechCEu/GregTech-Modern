package com.gregtechceu.gtceu.integration.xei.entry.item;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public sealed interface ItemEntryList permits ItemStackList, ItemTagList, ItemHolderSetList {

    List<ItemStack> getStacks();

    boolean isEmpty();
}
