package com.gregtechceu.gtceu.integration.jei.circuit;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.xeiwidgets.GTProgrammedCircuitWidget;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;
import dev.latvian.mods.rhino.Wrapper;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class GTProgrammedCircuitCategory extends ModularUIRecipeCategory<GTProgrammedCircuitCategory.GTProgrammedCircuitWrapper> {

    public final static RecipeType<GTProgrammedCircuitWrapper> RECIPE_TYPE = new RecipeType<>(
            GTCEu.id("programmed_circuit"), GTProgrammedCircuitWrapper.class);

    private final IDrawable background;
    private final IDrawable icon;

    public GTProgrammedCircuitCategory(IJeiHelpers helpers) {
        background = helpers.getGuiHelper().createBlankDrawable(150, 80);
        icon = helpers.getGuiHelper().createDrawableItemStack(GTItems.PROGRAMMED_CIRCUIT.asStack());
    }

    @Override
    public RecipeType<GTProgrammedCircuitWrapper> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.programmed_circuit_page");
    }

    @Override
    public @Nullable IDrawable getBackground() {
        return background;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    public static class GTProgrammedCircuitWrapper extends ModularWrapper<GTProgrammedCircuitWidget> {
        public GTProgrammedCircuitWrapper() {
            super(new GTProgrammedCircuitWidget());
        }
    }
}
