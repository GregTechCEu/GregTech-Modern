package com.gregtechceu.gtceu.integration.recipeviewer.jei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import java.util.Arrays;
import java.util.Objects;

public class GTBedrockFluidInfoCategory extends
                                        ModularUIRecipeCategory<BedrockFluidDefinition> {

    public final static RecipeType<BedrockFluidDefinition> RECIPE_TYPE = new RecipeType<>(
            GTCEu.id("bedrock_fluid_diagram"), BedrockFluidDefinition.class);
    private final IDrawable icon;

    public GTBedrockFluidInfoCategory(IJeiHelpers helpers) {
        super(OreVeinRecipeWidget::new,
                v -> Objects.requireNonNull(Minecraft.getInstance().level).registryAccess()
                        .registryOrThrow(GTRegistries.Keys.BEDROCK_FLUID).getKey(v));
        this.icon = helpers.getGuiHelper()
                .createDrawableItemStack(GTMaterials.Oil.getBucket().getDefaultInstance());
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, Objects.requireNonNull(Minecraft.getInstance().level).registryAccess()
                .registryOrThrow(GTRegistries.Keys.BEDROCK_FLUID).stream()
                .toList());
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LuV.asStack(), RECIPE_TYPE);
    }

    public int getMaxHeight() {
        return 250;
    }

    public int getMaxWidth() {
        return 180;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BedrockFluidDefinition fluid,
                          IFocusGroup focuses) {
        Arrays.stream(OreVeinRecipeWidget.getDimensionMarkers(fluid.dimensionFilter))
                .forEach(v -> builder.addSlot(RecipeIngredientRole.INPUT).addIngredient(VanillaTypes.ITEM_STACK,
                        v.getIcon()));

        builder.addSlot(RecipeIngredientRole.OUTPUT).addIngredient(NeoForgeTypes.FLUID_STACK,
                new FluidStack(fluid.getStoredFluid(), 1000));
    }

    @Override
    public RecipeType<BedrockFluidDefinition> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.bedrock_fluid_diagram");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
