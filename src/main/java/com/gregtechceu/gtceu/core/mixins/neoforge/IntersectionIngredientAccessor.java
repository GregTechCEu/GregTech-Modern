package com.gregtechceu.gtceu.core.mixins.neoforge;

import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = IntersectionIngredient.class, remap = false)
public interface IntersectionIngredientAccessor {

    @Accessor
    List<Ingredient> getChildren();
}
