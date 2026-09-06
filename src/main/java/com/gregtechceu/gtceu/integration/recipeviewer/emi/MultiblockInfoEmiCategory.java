package com.gregtechceu.gtceu.integration.recipeviewer.emi;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiblockInfoEmiCategory extends EmiRecipeCategory {

    public static final MultiblockInfoEmiCategory CATEGORY = new MultiblockInfoEmiCategory();

    private MultiblockInfoEmiCategory() {
        super(GTCEu.id("multiblock_info"), EmiStack.of(GTMultiMachines.ELECTRIC_BLAST_FURNACE.getItem()));
    }

    public static void registerDisplays(EmiRegistry registry) {
        GTRegistries.MACHINES.stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .filter(MultiblockMachineDefinition::isRenderXEIPreview)
                .map(MultiblockInfoEmiWrapper::new)
                .forEach(registry::addRecipe);
    }

    @Override
    public Component getName() {
        return Component.translatable("gtceu.jei.multiblock_info");
    }

    public static class MultiblockInfoEmiWrapper extends ModularUIEmiRecipe {

        private final MultiblockMachineDefinition definition;
        private final List<EmiIngredient> containedBlocks;

        public MultiblockInfoEmiWrapper(MultiblockMachineDefinition definition) {
            super(definition.getId(), () -> new MultiblockPreviewWidget(definition, null, 200, 180));
            this.definition = definition;
            containedBlocks = MultiblockPreviewWidget.initializeContainedBlocks(definition).stream()
                    .map(v -> (EmiIngredient) EmiStack.of(v)).toList();
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return CATEGORY;
        }

        @Override
        public @Nullable ResourceLocation getId() {
            return definition.getId().withPrefix("/multi_info/");
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return containedBlocks;
        }

        @Override
        public List<EmiStack> getOutputs() {
            return List.of(EmiStack.of(definition.getBlock()));
        }
    }
}
