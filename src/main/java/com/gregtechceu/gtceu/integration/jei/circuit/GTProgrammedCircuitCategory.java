package com.gregtechceu.gtceu.integration.jei.circuit;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.integration.xei.widgets.GTProgrammedCircuitWidget;

import com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;

import net.minecraft.network.chat.Component;

import lombok.Getter;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import org.jetbrains.annotations.NotNull;

public class GTProgrammedCircuitCategory extends ModularUIRecipeCategory<GTProgrammedCircuitWidget> {

    public final static RecipeType<GTProgrammedCircuitWidget> RECIPE_TYPE = new RecipeType<>(
            GTCEu.id("programmed_circuit"), GTProgrammedCircuitWidget.class);
    @Getter
    private final IDrawable background;
    @Getter
    private final IDrawable icon;

    public GTProgrammedCircuitCategory(IJeiHelpers helpers) {
        super(GTProgrammedCircuitWrapper::new);
        background = helpers.getGuiHelper().createBlankDrawable(150, 80);
        icon = helpers.getGuiHelper().createDrawableItemStack(GTItems.PROGRAMMED_CIRCUIT.asStack());
    }

    @Override
    public @NotNull RecipeType<GTProgrammedCircuitWidget> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("gtceu.jei.programmed_circuit");
    }

    public static class GTProgrammedCircuitWrapper extends ModularWrapper<GTProgrammedCircuitWidget> {

        public GTProgrammedCircuitWrapper(GTProgrammedCircuitWidget widget) {
            super(widget);
        }
    }
}
