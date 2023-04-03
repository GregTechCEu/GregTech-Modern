package com.gregtechceu.gtceu.integration.emi.fabric.multipage;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.lowdragmc.lowdraglib.emi.ModularUIEmiRecipeCategory;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.resources.ResourceLocation;

public class MultiblockInfoEmiCategory extends ModularUIEmiRecipeCategory {
    public static final MultiblockInfoEmiCategory CATEGORY = new MultiblockInfoEmiCategory();

    private MultiblockInfoEmiCategory() {
        super(new ResourceLocation(GTCEu.MOD_ID + ":multiblock_info"), EmiStack.of(GTMachines.ELECTRIC_BLAST_FURNACE.getItem()));
    }

    public static void registerDisplays(EmiRegistry registry) {
        GTRegistries.MACHINES.values().stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .map(MultiblockInfoEmiRecipe::new)
                .forEach(registry::addRecipe);
    }

    public static void registerWorkStations(EmiRegistry registry) {
        for (var definition : GTRegistries.MACHINES.values()) {
            if (definition instanceof MultiblockMachineDefinition multiblockDefinition) {
                registry.addWorkstation(CATEGORY, EmiStack.of(multiblockDefinition.asStack()));
            }
        }
    }

}
