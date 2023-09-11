package com.gregtechceu.gtceu.integration.rei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.integration.rei.multipage.MultiblockInfoDisplayCategory;
import com.gregtechceu.gtceu.integration.rei.recipe.GTRecipeTypeDisplayCategory;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;

import static me.shedaniel.rei.plugin.common.BuiltinPlugin.SMELTING;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote REIPlugin
 */
public class GTREIPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new MultiblockInfoDisplayCategory());
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                registry.add(new GTRecipeTypeDisplayCategory(gtRecipeType));
            }
        }
        // workstations
        MultiblockInfoDisplayCategory.registerWorkStations(registry);
        GTRecipeTypeDisplayCategory.registerWorkStations(registry);
        for (MachineDefinition definition : GTMachines.ELECTRIC_FURNACE) {
            if (definition != null) {
                registry.addWorkstations(SMELTING, EntryStacks.of(definition.asStack()));
            }
        }
        registry.addWorkstations(SMELTING, EntryStacks.of(GTMachines.STEAM_FURNACE.left().asStack()));
        registry.addWorkstations(SMELTING, EntryStacks.of(GTMachines.STEAM_FURNACE.right().asStack()));
        registry.addWorkstations(SMELTING, EntryStacks.of(GTMachines.STEAM_OVEN.asStack()));
        registry.addWorkstations(SMELTING, EntryStacks.of(GTMachines.MULTI_SMELTER.asStack()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        GTRecipeTypeDisplayCategory.registerDisplays(registry);
        MultiblockInfoDisplayCategory.registerDisplays(registry);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
        for (GTToolType toolType : GTToolType.values()) {
            registry.group(GTCEu.id("tool/" + toolType.name()), Component.translatable(toolType.getUnlocalizedName()), EntryIngredients.ofItemTag(toolType.itemTag));
        }
    }

}
