package com.gregtechceu.gtceu.integration.recipeviewer.rei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.ProgrammedCircuitRecipeWidget;

import net.minecraft.network.chat.Component;

import brachy.modularui.integration.rei.recipe.ModularUIREIDisplay;
import brachy.modularui.integration.rei.recipe.ModularUIREIDisplayCategory;
import lombok.Getter;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;

public class ProgrammedCircuitReiCategory extends
                                          ModularUIREIDisplayCategory<ProgrammedCircuitReiCategory.GTProgrammedCircuitDisplay> {

    public static CategoryIdentifier<GTProgrammedCircuitDisplay> CATEGORY = CategoryIdentifier
            .of(GTCEu.id("programmed_circuit"));

    @Getter
    private final Renderer icon;

    public ProgrammedCircuitReiCategory() {
        this.icon = EntryStacks.of(GTItems.PROGRAMMED_CIRCUIT.asItem());
    }

    @Override
    public CategoryIdentifier<? extends GTProgrammedCircuitDisplay> getCategoryIdentifier() {
        return CATEGORY;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.programmed_circuit");
    }

    public static class GTProgrammedCircuitDisplay extends ModularUIREIDisplay {

        public GTProgrammedCircuitDisplay() {
            super(GTCEu.id("programmed_circuit"), ProgrammedCircuitRecipeWidget::new,
                    ProgrammedCircuitReiCategory.CATEGORY);
        }
    }
}
