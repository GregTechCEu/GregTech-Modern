package com.gregtechceu.gtceu.integration.emi;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.integration.emi.multipage.MultiblockInfoEmiCategory;
import com.gregtechceu.gtceu.integration.emi.recipe.GTRecipeTypeEmiCategory;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * @author KilaBash
 * @date 2023/4/4
 * @implNote EMIPlugin
 */
@EmiEntrypoint
public class GTEMIPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(MultiblockInfoEmiCategory.CATEGORY);
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
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
            if (definition != null) {
                registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING, EmiStack.of(definition.asStack()));
            }
        }
        registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING, EmiStack.of(GTMachines.STEAM_FURNACE.left().asStack()));
        registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING, EmiStack.of(GTMachines.STEAM_FURNACE.right().asStack()));
        registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING, EmiStack.of(GTMachines.STEAM_OVEN.asStack()));
        registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING, EmiStack.of(GTMachines.MULTI_SMELTER.asStack()));
    }
}
