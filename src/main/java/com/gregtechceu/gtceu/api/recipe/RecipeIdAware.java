package com.gregtechceu.gtceu.api.recipe;

import net.minecraft.resources.Identifier;

/** Recipes that retain their holder's ID for GT lookup, research and synchronization. */
public interface RecipeIdAware {
    void setRecipeId(Identifier id);
}
