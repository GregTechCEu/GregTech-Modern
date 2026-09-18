package brachy.modularui.integration.recipeviewer.entry.item;

import brachy.modularui.integration.recipeviewer.entry.EntryList;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public sealed interface ItemEntryList extends EntryList<ItemStack> permits ItemStackList, ItemTagList, ItemHolderSetList {

    List<ItemStack> getStacks();

    boolean isEmpty();

    @Override
    default Class<ItemStack> getType() {
        return ItemStack.class;
    }
}
