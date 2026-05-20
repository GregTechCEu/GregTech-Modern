package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;

import net.minecraftforge.fml.ModLoader;

import org.jetbrains.annotations.NotNull;

public class GTRecipeCategories {

    public static final GTRecipeCategory ORE_CRUSHING = register("ore_crushing", GTRecipeTypes.MACERATOR_RECIPES);
    public static final GTRecipeCategory ORE_FORGING = register("ore_forging", GTRecipeTypes.FORGE_HAMMER_RECIPES);
    public static final GTRecipeCategory ORE_BATHING = register("ore_bathing", GTRecipeTypes.CHEMICAL_BATH_RECIPES);
    public static final GTRecipeCategory CHEM_DYES = register("chem_dyes", GTRecipeTypes.CHEMICAL_BATH_RECIPES);
    public static final GTRecipeCategory INGOT_MOLDING = register("ingot_molding", GTRecipeTypes.ALLOY_SMELTER_RECIPES);

    public static final GTRecipeCategory ARC_FURNACE_RECYCLING = register("arc_furnace_recycling",
            GTRecipeTypes.ARC_FURNACE_RECIPES)
            .setIcon(GuiTextures.ARC_FURNACE_RECYCLING_CATEGORY);

    public static final GTRecipeCategory MACERATOR_RECYCLING = register("macerator_recycling",
            GTRecipeTypes.MACERATOR_RECIPES)
            .setIcon(GuiTextures.MACERATOR_RECYCLING_CATEGORY);

    public static final GTRecipeCategory EXTRACTOR_RECYCLING = register("extractor_recycling",
            GTRecipeTypes.EXTRACTOR_RECIPES)
            .setIcon(GuiTextures.EXTRACTOR_RECYCLING_CATEGORY);

    public static GTRecipeCategory register(String categoryName, @NotNull GTRecipeType recipeType) {
        GTRecipeCategory category = new GTRecipeCategory(categoryName, recipeType);
        GTRegistries.RECIPE_CATEGORIES.register(category.registryKey, category);
        return category;
    }

    public static void init() {
        if (GTCEu.Mods.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistries.RECIPE_CATEGORIES.getRegistryName());
        }
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.RECIPE_CATEGORIES, GTRecipeCategory.class));
        GTRegistries.RECIPE_CATEGORIES.freeze();
    }

    public static GTRecipeCategory get(String name) {
        return GTRegistries.RECIPE_CATEGORIES.get(GTCEu.id(name));
    }
}
