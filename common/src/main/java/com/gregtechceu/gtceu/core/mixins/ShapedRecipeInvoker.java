package com.gregtechceu.gtceu.core.mixins;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/7/24
 * @implNote ShapedRecipeAccessor
 */
@Mixin(ShapedRecipe.class)
public interface ShapedRecipeInvoker {
    @Invoker
    static Map<String, Ingredient> callKeyFromJson(JsonObject keyEntry) {
        return null;
    }

    @Invoker
    static String[] callPatternFromJson(JsonArray patternArray) {
        return null;
    }

    @Invoker
    static NonNullList<Ingredient> callDissolvePattern(String[] pattern, Map<String, Ingredient> keys, int patternWidth, int patternHeight) {
        return null;
    }

    @Invoker
    static String[] callShrink(String... toShrink) {
        return null;
    }
}
