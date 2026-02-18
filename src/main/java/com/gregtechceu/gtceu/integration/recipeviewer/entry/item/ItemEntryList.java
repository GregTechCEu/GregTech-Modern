package com.gregtechceu.gtceu.integration.recipeviewer.entry.item;

import com.gregtechceu.gtceu.integration.recipeviewer.entry.EntryList;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public sealed interface ItemEntryList extends EntryList<ItemStack>
                                      permits ItemStackList, ItemTagList, ItemHolderSetList {

    List<ItemStack> getStacks();

    boolean isEmpty();
}
