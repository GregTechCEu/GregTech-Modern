package com.gregtechceu.gtceu.integration.emi.oreprocessing;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.emi.ModularUIEmiRecipeCategory;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import static com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey.ORE;

public class GTOreProcessingEmiCategory extends ModularUIEmiRecipeCategory {
    public static final GTOreProcessingEmiCategory CATEGORY = new GTOreProcessingEmiCategory();
    public GTOreProcessingEmiCategory() {
        super(GTCEu.id("ore_processing_diagram"), EmiStack.of(Items.IRON_ORE));
    }
    public static void registerDisplays(EmiRegistry registry) {
        for (Material mat : GTRegistries.MATERIALS) {
            if (mat.hasProperty(ORE)) {
                registry.addRecipe(new GTEmiOreProcessingV2(mat));
            }
        }
    }
    public static void registerWorkStations(EmiRegistry registry) {
        //use catalysts instead for recipe-aware workstations

        //for (EmiStack machine : GTEmiOreProcessingV2.getMachines(List.of(MACERATOR_RECIPES, ORE_WASHER_RECIPES, THERMAL_CENTRIFUGE_RECIPES, CENTRIFUGE_RECIPES, CHEMICAL_BATH_RECIPES, ELECTROMAGNETIC_SEPARATOR_RECIPES, SIFTER_RECIPES))) {
        //    registry.addWorkstation(CATEGORY, machine);
        //}
    }
    @Override
    public Component getName() {
        return Component.translatable("gtceu.jei.ore_processing_diagram");
    }
}
