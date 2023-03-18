package com.lowdragmc.gtceu.integration.jei;

import com.lowdragmc.gtceu.GTCEu;
import com.lowdragmc.gtceu.api.machine.MachineDefinition;
import com.lowdragmc.gtceu.api.recipe.GTRecipeType;
import com.lowdragmc.gtceu.common.libs.GTMachines;
import com.lowdragmc.gtceu.integration.jei.multipage.MultiblockInfoCategory;
import com.lowdragmc.gtceu.integration.jei.recipe.GTRecipeTypeCategory;
import com.lowdragmc.lowdraglib.LDLib;
import me.shedaniel.rei.api.common.util.EntryStacks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static me.shedaniel.rei.plugin.common.BuiltinPlugin.SMELTING;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote JEIPlugin
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class JEIPlugin implements IModPlugin {
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
        for (RecipeType<?> recipeType : Registry.RECIPE_TYPE) {
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
            registration.addRecipeCatalyst(definition.asStack(), RecipeTypes.SMELTING);
        }
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
