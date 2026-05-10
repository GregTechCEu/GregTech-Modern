package com.gregtechceu.gtceu.integration.recipeviewer.emi;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.ProgrammedCircuitRecipeWidget;

import net.minecraft.network.chat.Component;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;

public class ProgrammedCircuitEmiCategory extends EmiRecipeCategory {

    public static final ProgrammedCircuitEmiCategory CATEGORY = new ProgrammedCircuitEmiCategory();

    public ProgrammedCircuitEmiCategory() {
        super(GTCEu.id("programmed_circuit"), EmiStack.of(GTItems.PROGRAMMED_CIRCUIT.asItem()));
    }

    public static void registerDisplays(EmiRegistry registry) {
        registry.addRecipe(new ProgrammedCircuitEmiCategory.GTProgrammedCircuitWrapper());
    }

    @Override
    public Component getName() {
        return Component.translatable("gtceu.jei.programmed_circuit");
    }

    public static class GTProgrammedCircuitWrapper extends ModularUIEmiRecipe {

        public GTProgrammedCircuitWrapper() {
            super(GTCEu.id("/programmed_circuit"), ProgrammedCircuitRecipeWidget::new);
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return CATEGORY;
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return List.of();
        }

        @Override
        public @NotNull List<EmiStack> getOutputs() {
            return IntStream.range(0, 33)
                    .mapToObj(IntCircuitBehaviour::stack)
                    .map(EmiStack::of)
                    .toList();
        }

        @Override
        public boolean supportsRecipeTree() {
            return false;
        }

        @Override
        public boolean hideCraftable() {
            return true;
        }
    }
}
