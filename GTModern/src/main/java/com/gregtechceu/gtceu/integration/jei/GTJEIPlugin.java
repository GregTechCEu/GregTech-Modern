package com.gregtechceu.gtceu.integration.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.jei.multipage.MultiblockInfoCategory;
import com.gregtechceu.gtceu.integration.jei.oreprocessing.GTOreProcessingInfoCategory;
import com.gregtechceu.gtceu.integration.jei.orevein.GTBedrockFluidInfoCategory;
import com.gregtechceu.gtceu.integration.jei.orevein.GTBedrockOreInfoCategory;
import com.gregtechceu.gtceu.integration.jei.orevein.GTOreVeinInfoCategory;
import com.gregtechceu.gtceu.integration.jei.recipe.GTRecipeTypeCategory;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.*;
import org.jetbrains.annotations.NotNull;

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
    public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
        if (LDLib.isReiLoaded() || LDLib.isEmiLoaded()) return;
        GTCEu.LOGGER.info("JEI register categories");
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        registry.addRecipeCategories(new MultiblockInfoCategory(jeiHelpers));
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            registry.addRecipeCategories(new GTOreProcessingInfoCategory(jeiHelpers));
        registry.addRecipeCategories(new GTOreVeinInfoCategory(jeiHelpers));
        registry.addRecipeCategories(new GTBedrockFluidInfoCategory(jeiHelpers));
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            registry.addRecipeCategories(new GTBedrockOreInfoCategory(jeiHelpers));
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                if (Platform.isDevEnv() || gtRecipeType.getRecipeUI().isXEIVisible()) {
                    registry.addRecipeCategories(new GTRecipeTypeCategory(jeiHelpers, gtRecipeType));
                }
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (LDLib.isReiLoaded() || LDLib.isEmiLoaded()) return;
        GTRecipeTypeCategory.registerRecipeCatalysts(registration);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingInfoCategory.registerRecipeCatalysts(registration);
        GTOreVeinInfoCategory.registerRecipeCatalysts(registration);
        GTBedrockFluidInfoCategory.registerRecipeCatalysts(registration);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreInfoCategory.registerRecipeCatalysts(registration);
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
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (LDLib.isReiLoaded() || LDLib.isEmiLoaded()) return;
        GTCEu.LOGGER.info("JEI register");
        MultiblockInfoCategory.registerRecipes(registration);
        GTRecipeTypeCategory.registerRecipes(registration);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingInfoCategory.registerRecipes(registration);
        GTOreVeinInfoCategory.registerRecipes(registration);
        GTBedrockFluidInfoCategory.registerRecipes(registration);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreInfoCategory.registerRecipes(registration);
    }

    @Override
    public void registerIngredients(@NotNull IModIngredientRegistration registry) {
        if (LDLib.isReiLoaded() || LDLib.isEmiLoaded()) return;
        GTCEu.LOGGER.info("JEI register ingredients");
    }
}
