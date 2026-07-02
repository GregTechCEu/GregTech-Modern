package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.ProgrammedCircuitRecipeWidget;

import net.minecraft.network.chat.Component;

import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProgrammedCircuitJeiCategory extends
                                          ModularUIRecipeCategory<ProgrammedCircuitJeiCategory.GTProgrammedCircuitWrapper> {

    public final static RecipeType<GTProgrammedCircuitWrapper> RECIPE_TYPE = new RecipeType<>(
            GTCEu.id("programmed_circuit"), GTProgrammedCircuitWrapper.class);

    private final IDrawable icon;

    public ProgrammedCircuitJeiCategory(IJeiHelpers helpers) {
        super($ -> new ProgrammedCircuitRecipeWidget(), $ -> GTCEu.id("programmed_circuit"));
        icon = helpers.getGuiHelper().createDrawableItemStack(GTItems.PROGRAMMED_CIRCUIT.asStack());
    }

    @Override
    public @NotNull RecipeType<GTProgrammedCircuitWrapper> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("gtceu.jei.programmed_circuit");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    public static class GTProgrammedCircuitWrapper {}
}
