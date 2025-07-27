package com.gregtechceu.gtceu.core.mixins.forge;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = StrictNBTIngredient.class, remap = false)
public interface StrictNBTIngredientAccessor {

    @Accessor
    ItemStack getStack();
}
