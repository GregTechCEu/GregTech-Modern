package com.gregtechceu.gtceu.integration.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.integration.jei.multipage.MultiblockInfoCategory;
import com.gregtechceu.gtceu.integration.jei.recipe.GTRecipeTypeCategory;
import com.lowdragmc.lowdraglib.LDLib;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote JEIPlugin
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@JeiPlugin
public class GTJEIPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return GTCEu.id("jei_plugin");
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        if (LDLib.isReiLoaded()) return;
        GTCEu.LOGGER.info("JEI register categories");
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        registry.addRecipeCategories(new MultiblockInfoCategory(jeiHelpers));
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                registry.addRecipeCategories(new GTRecipeTypeCategory(jeiHelpers, gtRecipeType));
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(@Nonnull IRecipeCatalystRegistration registration) {
        if (LDLib.isReiLoaded()) return;
        MultiblockInfoCategory.registerRecipeCatalysts(registration);
        GTRecipeTypeCategory.registerRecipeCatalysts(registration);
        for (MachineDefinition definition : GTMachines.ELECTRIC_FURNACE) {
            if (definition != null) {
                registration.addRecipeCatalyst(definition.asStack(), RecipeTypes.SMELTING);
            }
        }
        registration.addRecipeCatalyst(GTMachines.STEAM_FURNACE.left().asStack(), RecipeTypes.SMELTING);
        registration.addRecipeCatalyst(GTMachines.STEAM_FURNACE.right().asStack(), RecipeTypes.SMELTING);
        registration.addRecipeCatalyst(GTMachines.STEAM_OVEN.asStack(), RecipeTypes.SMELTING);
        registration.addRecipeCatalyst(GTMachines.MULTI_SMELTER.asStack(), RecipeTypes.SMELTING);
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        if (LDLib.isReiLoaded()) return;
        GTCEu.LOGGER.info("JEI register");
        MultiblockInfoCategory.registerRecipes(registration);
        GTRecipeTypeCategory.registerRecipes(registration);
    }

    @Override
    public void registerIngredients(@Nonnull IModIngredientRegistration registry) {
        if (LDLib.isReiLoaded()) return;
        GTCEu.LOGGER.info("JEI register ingredients");
    }
}

