package com.gregtechceu.gtceu.core.mixins.emi;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map;

@Mixin(EmiApi.class)
public interface EmiApiAccessor {

    @Invoker("setPages")
    static void gtceu$setPages(Map<EmiRecipeCategory, List<EmiRecipe>> recipes, EmiIngredient stack) {
        throw new AssertionError();
    }
}
