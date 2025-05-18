package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccessor {

    @Invoker
    static Map<String, Ingredient> callKeyFromJson(JsonObject keyEntry) {
        throw new AssertionError();
    }

    @Invoker
    static String[] callPatternFromJson(JsonArray patternArray) {
        throw new AssertionError();
    }

    @Invoker
    static NonNullList<Ingredient> callDissolvePattern(String[] pattern, Map<String, Ingredient> keys, int patternWidth,
                                                       int patternHeight) {
        throw new AssertionError();
    }

    @Invoker
    static String[] callShrink(String... toShrink) {
        throw new AssertionError();
    }

    @Accessor
    ItemStack getResult();
}
