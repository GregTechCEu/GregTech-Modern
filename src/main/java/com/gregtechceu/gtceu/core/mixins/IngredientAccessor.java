package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.item.crafting.Ingredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.stream.Stream;

@Mixin(Ingredient.class)
public interface IngredientAccessor {

    @Accessor
    Ingredient.Value[] getValues();

    @Invoker(value = "<init>")
    static Ingredient create(Stream<? extends Ingredient.Value> values) {
        return null;
    }
}
