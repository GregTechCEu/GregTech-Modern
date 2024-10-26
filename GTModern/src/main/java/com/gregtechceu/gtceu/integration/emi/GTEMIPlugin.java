package com.gregtechceu.gtceu.integration.emi;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.emi.multipage.MultiblockInfoEmiCategory;
import com.gregtechceu.gtceu.integration.emi.oreprocessing.GTOreProcessingEmiCategory;
import com.gregtechceu.gtceu.integration.emi.orevein.GTBedrockFluidEmiCategory;
import com.gregtechceu.gtceu.integration.emi.orevein.GTBedrockOreEmiCategory;
import com.gregtechceu.gtceu.integration.emi.orevein.GTOreVeinEmiCategory;
import com.gregtechceu.gtceu.integration.emi.recipe.Ae2PatternTerminalHandler;
import com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipeHandler;
import com.gregtechceu.gtceu.integration.emi.recipe.GTRecipeTypeEmiCategory;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;

import appeng.menu.me.items.PatternEncodingTermMenu;
import de.mari_023.ae2wtlib.wet.WETMenu;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;

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
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            registry.addCategory(GTOreProcessingEmiCategory.CATEGORY);
        registry.addCategory(GTOreVeinEmiCategory.CATEGORY);
        registry.addCategory(GTBedrockFluidEmiCategory.CATEGORY);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            registry.addCategory(GTBedrockOreEmiCategory.CATEGORY);
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                if (Platform.isDevEnv() || gtRecipeType.getRecipeUI().isXEIVisible()) {
                    registry.addCategory(GTRecipeTypeEmiCategory.CATEGORIES.apply(gtRecipeType));
                }
            }
        }
        registry.addRecipeHandler(ModularUIContainer.MENUTYPE, new GTEmiRecipeHandler());
        if (GTCEu.isAE2Loaded()) {
            registry.addRecipeHandler(PatternEncodingTermMenu.TYPE, new Ae2PatternTerminalHandler<>());
        }
        if (LDLib.isModLoaded(GTValues.MODID_AE2WTLIB)) {
            registry.addRecipeHandler(WETMenu.TYPE, new Ae2PatternTerminalHandler<>());
        }
        // recipes
        try {
            MultiblockInfoEmiCategory.registerDisplays(registry);
        } catch (NullPointerException ignored) {}
        GTRecipeTypeEmiCategory.registerDisplays(registry);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingEmiCategory.registerDisplays(registry);
        GTOreVeinEmiCategory.registerDisplays(registry);
        GTBedrockFluidEmiCategory.registerDisplays(registry);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreEmiCategory.registerDisplays(registry);
        // workstations
        GTRecipeTypeEmiCategory.registerWorkStations(registry);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingEmiCategory.registerWorkStations(registry);
        GTOreVeinEmiCategory.registerWorkStations(registry);
        GTBedrockFluidEmiCategory.registerWorkStations(registry);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreEmiCategory.registerWorkStations(registry);
        for (MachineDefinition definition : GTMachines.ELECTRIC_FURNACE) {
            if (definition != null) {
                registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING, EmiStack.of(definition.asStack()));
            }
        }
        registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING,
                EmiStack.of(GTMachines.STEAM_FURNACE.left().asStack()));
        registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING,
                EmiStack.of(GTMachines.STEAM_FURNACE.right().asStack()));
        registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING, EmiStack.of(GTMachines.STEAM_OVEN.asStack()));
        registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING, EmiStack.of(GTMachines.MULTI_SMELTER.asStack()));
    }
}
