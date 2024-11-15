package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraftforge.fml.ModLoader;

public class GTRecipeCategories {

    // Used for recipes you don't want in a category
    public static final GTRecipeCategory DUMMY = GTRecipeCategory.of(GTRecipeTypes.DUMMY_RECIPES);

    public static final GTRecipeCategory ARC_FURNACE_RECYCLING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "arc_furnace_recycling", GTRecipeTypes.ARC_FURNACE_RECIPES)
            .setIcon(GuiTextures.ARC_FURNACE_RECYCLING_CATEGORY);

    public static final GTRecipeCategory MACERATOR_RECYCLING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "macerator_recycling", GTRecipeTypes.MACERATOR_RECIPES)
            .setIcon(GuiTextures.MACERATOR_RECYCLING_CATEGORY);

    public static final GTRecipeCategory EXTRACTOR_RECYCLING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "extractor_recycling", GTRecipeTypes.EXTRACTOR_RECIPES)
            .setIcon(GuiTextures.EXTRACTOR_RECYCLING_CATEGORY);

    public static final GTRecipeCategory ORE_CRUSHING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "ore_crushing", GTRecipeTypes.MACERATOR_RECIPES);

    public static final GTRecipeCategory ORE_FORGING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "ore_forging", GTRecipeTypes.FORGE_HAMMER_RECIPES);

    public static final GTRecipeCategory ORE_BATHING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "ore_bathing", GTRecipeTypes.CHEMICAL_BATH_RECIPES);

    public static final GTRecipeCategory CHEM_DYES = GTRecipeCategory
            .of(GTCEu.MOD_ID, "chem_dyes", GTRecipeTypes.CHEMICAL_BATH_RECIPES);

    public static final GTRecipeCategory INGOT_MOLDING = GTRecipeCategory
            .of(GTCEu.MOD_ID, "ingot_molding", GTRecipeTypes.ALLOY_SMELTER_RECIPES);

    public static void init() {
        GTRegistries.RECIPE_CATEGORIES.remove(DUMMY.getResourceLocation());
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.RECIPE_CATEGORIES, GTRecipeCategory.class));
        GTRegistries.RECIPE_CATEGORIES.freeze();
    }
}
