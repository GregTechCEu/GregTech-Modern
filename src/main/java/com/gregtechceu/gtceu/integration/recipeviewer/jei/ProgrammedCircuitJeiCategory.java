package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.ProgrammedCircuitRecipeWidget;

import net.minecraft.network.chat.Component;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class ProgrammedCircuitJeiCategory extends
                                          ModularUIJeiCategory<ProgrammedCircuitJeiCategory.GTProgrammedCircuitWrapper> {

    public final static RecipeType<GTProgrammedCircuitWrapper> RECIPE_TYPE = new RecipeType<>(
            GTCEu.id("programmed_circuit"), GTProgrammedCircuitWrapper.class);

    private final IDrawable icon;

    public ProgrammedCircuitJeiCategory(IJeiHelpers helpers) {
        super($ -> new ProgrammedCircuitRecipeWidget(), $ -> GTCEu.id("programmed_circuit"));
        icon = helpers.getGuiHelper().createDrawableItemStack(GTItems.PROGRAMMED_CIRCUIT.asStack());
    }

    @Override
    public RecipeType<GTProgrammedCircuitWrapper> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.programmed_circuit");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getMaxWidth() {
        return 250;
    }

    @Override
    public int getMaxHeight() {
        return 250;
    }

    @Override
    public void setupRecipeIngredients(IRecipeLayoutBuilder builder, GTProgrammedCircuitWrapper recipe,
                                       IFocusGroup focuses) {
        IntStream.range(0, 33)
                .mapToObj(IntCircuitBehaviour::stack)
                .forEach(i -> builder.addSlot(RecipeIngredientRole.OUTPUT).addIngredient(VanillaTypes.ITEM_STACK, i));
    }

    public static class GTProgrammedCircuitWrapper {}
}
