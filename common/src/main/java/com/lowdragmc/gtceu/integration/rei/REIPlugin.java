package com.lowdragmc.gtceu.integration.rei;

import com.lowdragmc.gtceu.api.machine.MachineDefinition;
import com.lowdragmc.gtceu.api.recipe.GTRecipeType;
import com.lowdragmc.gtceu.common.libs.GTMachines;
import com.lowdragmc.gtceu.integration.rei.multipage.MultiblockInfoDisplayCategory;
import com.lowdragmc.gtceu.integration.rei.recipe.GTRecipeTypeDisplayCategory;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeType;

import static me.shedaniel.rei.plugin.common.BuiltinPlugin.SMELTING;
import static me.shedaniel.rei.plugin.common.BuiltinPlugin.SMOKING;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote REIPlugin
 */
public class REIPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new MultiblockInfoDisplayCategory());
        for (RecipeType<?> recipeType : Registry.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                registry.add(new GTRecipeTypeDisplayCategory(gtRecipeType));
            }
        }
        // workstations
        MultiblockInfoDisplayCategory.registerWorkStations(registry);
        GTRecipeTypeDisplayCategory.registerWorkStations(registry);
        for (MachineDefinition definition : GTMachines.ELECTRIC_FURNACE) {
            registry.addWorkstations(SMELTING, EntryStacks.of(definition.asStack()));
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        GTRecipeTypeDisplayCategory.registerDisplays(registry);
        MultiblockInfoDisplayCategory.registerDisplays(registry);
    }
}
