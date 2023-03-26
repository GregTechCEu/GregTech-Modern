package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import dev.latvian.mods.kubejs.event.EventJS;

import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote RecipeTypeEventJS
 */
public class RecipeTypeEventJS extends EventJS {

    public GTRecipeType create(String name) {
        return GTRecipeTypes.register(name);
    }

    public boolean remove(String name) {
        return GTRegistries.RECIPE_TYPES.remove(GTCEu.id(name));
    }

    @Nullable
    public GTRecipeType get(String name) {
        return GTRegistries.RECIPE_TYPES.get(GTCEu.id(name));
    }
}
