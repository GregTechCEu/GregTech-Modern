package com.gregtechceu.gtceu.integration.recipeviewer.emi;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.core.mixins.mui.ModularUIEmiRecipeAccessor;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MultiblockInfoEmiCategory extends EmiRecipeCategory {

    public static final MultiblockInfoEmiCategory CATEGORY = new MultiblockInfoEmiCategory();

    private MultiblockInfoEmiCategory() {
        super(GTCEu.id("multiblock_info"), EmiStack.of(GTMultiMachines.ELECTRIC_BLAST_FURNACE.getItem()));
    }

    public static void registerDisplays(EmiRegistry registry) {
        GTRegistries.MACHINES.values().stream()
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

        public MultiblockInfoEmiWrapper(MultiblockMachineDefinition definition) {
            super(definition.getId(), () -> new MultiblockPreviewWidget(definition, null, 200, 180));
            this.definition = definition;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            super.addWidgets(widgets);
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
            var recipeUI = ((MultiblockPreviewWidget) ((ModularUIEmiRecipeAccessor) this).getRecipeUI().get());
            if (recipeUI.getMultiblockSchemaInfo() == null) return Collections.emptyList();
            var blockCounts = recipeUI.getMultiblockSchemaInfo().getBlockCounts();
            List<EmiIngredient> inputs = new ArrayList<>();
            blockCounts.forEach((block, count) -> inputs.add(EmiStack.of(block.asItem(), count)));
            return inputs;
        }

        @Override
        public List<EmiStack> getOutputs() {
            return List.of(EmiStack.of(definition.getBlock()));
        }
    }
}
