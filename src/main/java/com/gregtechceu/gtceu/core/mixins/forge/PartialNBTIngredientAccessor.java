package com.gregtechceu.gtceu.core.mixins.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.PartialNBTIngredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(value = PartialNBTIngredient.class, remap = false)
public interface PartialNBTIngredientAccessor {

    @Accessor
    CompoundTag getNbt();

    @Accessor
    Set<Item> getItems();
}
