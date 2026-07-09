package com.gregtechceu.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;

/**
 * Allows for a recipe modifier or other machine configuration to be previewed when viewing a recipe in a recipe viewer.
 */
public interface RecipeModifierPreview {

    default boolean showPreviewUIPanel() {
        return true;
    }

    void buildPreviewUIPanel();

    ModifierFunction getModifier(GTRecipe baseRecipe, int minVoltageTier, int selectedVoltageTier);

}
