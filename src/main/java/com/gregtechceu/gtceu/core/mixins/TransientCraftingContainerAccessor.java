package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TransientCraftingContainer.class)
public interface TransientCraftingContainerAccessor {

    @Accessor("items")
    NonNullList<ItemStack> gtceu$getActualItems();

    @Accessor
    AbstractContainerMenu getMenu();
}
