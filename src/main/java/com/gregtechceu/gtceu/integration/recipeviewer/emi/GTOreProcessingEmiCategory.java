package com.gregtechceu.gtceu.integration.recipeviewer.emi;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.GTOreByProduct;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreProcessingRecipeWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import brachy.modularui.integration.emi.EmiStackConverter;
import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey.ORE;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe.GTRecipeEMICategory.sortDefinition;

public class GTOreProcessingEmiCategory extends EmiRecipeCategory {

    public static final GTOreProcessingEmiCategory CATEGORY = new GTOreProcessingEmiCategory();

    public GTOreProcessingEmiCategory() {
        super(GTCEu.id("ore_processing_diagram"), EmiStack.of(Items.RAW_IRON));
    }

    public static void registerDisplays(EmiRegistry registry) {
        for (Material mat : GTRegistries.MATERIALS.values()) {
            if (mat.hasProperty(ORE) && !mat.hasFlag(MaterialFlags.NO_ORE_PROCESSING_TAB)) {
                registry.addRecipe(new GTEmiOreProcessingWrapper(mat));
            }
        }
    }

    public static void registerWorkStations(EmiRegistry registry) {
        List<MachineDefinition> registeredMachines = new ArrayList<>();
        GTRecipeType[] validTypes = new GTRecipeType[] {
                MACERATOR_RECIPES, ORE_WASHER_RECIPES, THERMAL_CENTRIFUGE_RECIPES, CENTRIFUGE_RECIPES,
                CHEMICAL_BATH_RECIPES, ELECTROMAGNETIC_SEPARATOR_RECIPES, SIFTER_RECIPES
        };
        for (MachineDefinition machine : GTRegistries.MACHINES.values()
                .stream()
                .sorted(sortDefinition)
                .toList()) {
            for (GTRecipeType type : machine.getRecipeTypes()) {
                for (GTRecipeType validType : validTypes) {
                    if (type == validType && !registeredMachines.contains(machine)) {
                        registry.addWorkstation(CATEGORY, EmiStack.of(machine.asStack()));
                        registeredMachines.add(machine);
                    }
                }
            }
        }
    }

    @Override
    public Component getName() {
        return Component.translatable("gtceu.jei.ore_processing_diagram");
    }

    public static class GTEmiOreProcessingWrapper extends ModularUIEmiRecipe {

        final Material material;
        final GTOreByProduct byProduct;

        public GTEmiOreProcessingWrapper(Material material) {
            super(material.getResourceLocation().withPrefix("/ore_proc/"),
                    () -> new OreProcessingRecipeWidget(material));
            this.material = material;
            byProduct = new GTOreByProduct(material);
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return CATEGORY;
        }

        @Override
        public List<EmiIngredient> getInputs() {
            var items = byProduct.getItemInputs();
            var fluids = byProduct.getFluidInputs();
            List<EmiIngredient> ingredients = new ArrayList<>();
            ingredients.addAll(items.stream()
                    .map(v -> EmiStackConverter.ITEM.convertTo(v, 1)).toList());
            ingredients.addAll(
                    fluids.stream().map(v -> EmiStackConverter.FLUID.convertTo(v, 1)).toList());
            return ingredients;
        }

        @Override
        public List<EmiStack> getOutputs() {
            return byProduct.getItemOutputs().stream().map(EmiStack::of).toList();
        }

        @Override
        public boolean supportsRecipeTree() {
            return false;
        }
    }
}
