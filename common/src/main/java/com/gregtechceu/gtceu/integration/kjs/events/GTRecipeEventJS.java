package com.gregtechceu.gtceu.integration.kjs.events;

import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.integration.kjs.GTCEuServerEvents;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GTRecipeEventJS
 */
public class GTRecipeEventJS extends EventJS {

    private final Map<ResourceLocation, JsonElement> recipes;
    public final Consumer<FinishedRecipe> provider = recipe -> {
        var id = recipe.getId();
        if (getRecipes().put(id, recipe.serializeRecipe()) != null) {
            GTCEu.LOGGER.error("duplicated recipe: {}", id);
        }
    };

    public GTRecipeEventJS(Map<ResourceLocation, JsonElement> recipes) {
        this.recipes = recipes;
    }

    public void remove(ResourceLocation id) {
        if (recipes.remove(id) == null) {
            GTCEu.LOGGER.error("failed to remove recipe {}, could not find matching recipe", id);
        }
    }

    public GTRecipeBuilder builder(GTRecipeType recipeType, ResourceLocation id) {
        return recipeType.recipeBuilder(id);
    }

    public Map<ResourceLocation, JsonElement> getRecipes() {
        return recipes;
    }

    public void post() {
        GTCEuServerEvents.RECIPE.post(this);
    }
}
