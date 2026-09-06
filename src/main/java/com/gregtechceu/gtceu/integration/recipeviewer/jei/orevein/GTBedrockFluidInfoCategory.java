package com.gregtechceu.gtceu.integration.recipeviewer.jei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class GTBedrockFluidInfoCategory extends ModularUIJeiCategory<BedrockFluidDefinition> {

    public final static RecipeType<BedrockFluidDefinition> RECIPE_TYPE = new RecipeType<>(
            GTCEu.id("bedrock_fluid_diagram"), BedrockFluidDefinition.class);

    private final IDrawable icon;

    public GTBedrockFluidInfoCategory(IJeiHelpers helpers) {
        super(OreVeinRecipeWidget::new, v -> ClientProxy.CLIENT_FLUID_VEINS.inverse().get(v));
        this.icon = helpers.getGuiHelper()
                .createDrawableItemStack(GTMaterials.Oil.getFluid().getBucket().asItem().getDefaultInstance());
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, ClientProxy.CLIENT_FLUID_VEINS.values().stream()
                .toList());
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LuV.asStack(), RECIPE_TYPE);
    }

    @NotNull
    @Override
    public RecipeType<BedrockFluidDefinition> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public int getMaxHeight() {
        return 250;
    }

    @Override
    public int getMaxWidth() {
        return 180;
    }

    @Override
    public void setupRecipeIngredients(IRecipeLayoutBuilder builder, BedrockFluidDefinition fluid,
                                       IFocusGroup focuses) {
        Arrays.stream(OreVeinRecipeWidget.getDimensionMarkers(fluid.dimensionFilter))
                .forEach(v -> builder.addSlot(RecipeIngredientRole.INPUT).addIngredient(VanillaTypes.ITEM_STACK,
                        v.getIcon()));

        builder.addSlot(RecipeIngredientRole.OUTPUT).addIngredient(ForgeTypes.FLUID_STACK,
                new FluidStack(fluid.getStoredFluid().get(), 1000));
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.bedrock_fluid_diagram");
    }

    @NotNull
    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
