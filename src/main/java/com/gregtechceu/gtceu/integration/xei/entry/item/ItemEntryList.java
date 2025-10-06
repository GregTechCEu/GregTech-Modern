package com.gregtechceu.gtceu.integration.xei.entry.item;

import com.gregtechceu.gtceu.integration.xei.entry.EntryList;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public sealed interface ItemEntryList extends EntryList<ItemStack> permits ItemStackList, ItemTagList {

    List<ItemStack> getStacks();

    boolean isEmpty();
}
