package com.gregtechceu.gtceu.integration.emi.fabric;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.integration.emi.fabric.multipage.MultiblockInfoEmiCategory;
import com.gregtechceu.gtceu.integration.emi.fabric.recipe.GTRecipeTypeEmiCategory;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * @author KilaBash
 * @date 2023/4/4
 * @implNote EMIPlugin
 */
public class EMIPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(MultiblockInfoEmiCategory.CATEGORY);
        for (RecipeType<?> recipeType : Registry.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                registry.addCategory(GTRecipeTypeEmiCategory.CATEGORIES.apply(gtRecipeType));
            }
        }
        // recipes
        MultiblockInfoEmiCategory.registerDisplays(registry);
        GTRecipeTypeEmiCategory.registerDisplays(registry);
        // workstations
        MultiblockInfoEmiCategory.registerWorkStations(registry);
        GTRecipeTypeEmiCategory.registerWorkStations(registry);
        for (MachineDefinition definition : GTMachines.ELECTRIC_FURNACE) {
            registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING, EmiStack.of(definition.asStack()));
        }
    }
}
