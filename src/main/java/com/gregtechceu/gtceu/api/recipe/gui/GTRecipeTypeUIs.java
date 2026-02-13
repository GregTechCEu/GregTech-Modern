package com.gregtechceu.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import java.util.HashMap;
import java.util.Map;

public class GTRecipeTypeUIs {

    public static Map<GTRecipeType, GTRecipeTypeUILayout> recipeTypeUIs = new HashMap<>();

    public static void addRecipeTypeUI(GTRecipeType recipeType, GTRecipeTypeUILayout layout) {
        recipeTypeUIs.put(recipeType, layout);
    }
}
