package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public class GTRecipeCategories {

    // disable spotless on registration blocks so they can be formatted manually
    // spotless:off
    public static final GTRecipeCategory ORE_CRUSHING = register("ore_crushing", GTRecipeTypes.MACERATOR_RECIPES);
    public static final GTRecipeCategory ORE_FORGING = register("ore_forging", GTRecipeTypes.FORGE_HAMMER_RECIPES);
    public static final GTRecipeCategory ORE_BATHING = register("ore_bathing", GTRecipeTypes.CHEMICAL_BATH_RECIPES);
    public static final GTRecipeCategory CHEM_DYES = register("chem_dyes", GTRecipeTypes.CHEMICAL_BATH_RECIPES);
    public static final GTRecipeCategory INGOT_MOLDING = register("ingot_molding", GTRecipeTypes.ALLOY_SMELTER_RECIPES);

    public static final GTRecipeCategory ARC_FURNACE_RECYCLING = register("arc_furnace_recycling", GTRecipeTypes.ARC_FURNACE_RECIPES)
            .setIcon(GuiTextures.ARC_FURNACE_RECYCLING_CATEGORY);

    public static final GTRecipeCategory MACERATOR_RECYCLING = register("macerator_recycling", GTRecipeTypes.MACERATOR_RECIPES)
            .setIcon(GuiTextures.MACERATOR_RECYCLING_CATEGORY);

    public static final GTRecipeCategory EXTRACTOR_RECYCLING = register("extractor_recycling", GTRecipeTypes.EXTRACTOR_RECIPES)
            .setIcon(GuiTextures.EXTRACTOR_RECYCLING_CATEGORY);
    // spotless:on

    public static GTRecipeCategory register(ResourceLocation id, @NotNull GTRecipeType recipeType) {
        GTRecipeCategory category = new GTRecipeCategory(id, recipeType);
        GTRegistries.register(GTRegistries.RECIPE_CATEGORIES, category.registryKey, category);
        return category;
    }

    private static GTRecipeCategory register(String categoryName, @NotNull GTRecipeType recipeType) {
        return register(GTCEu.id(categoryName), recipeType);
    }

    public static void init() {}

    public static GTRecipeCategory get(String name) {
        return GTRegistries.RECIPE_CATEGORIES.get(GTCEu.id(name));
    }
}
