package com.gregtechceu.gtceu.integration.recipeviewer.jei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;
import lombok.Getter;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import java.util.Arrays;

public class GTBedrockOreInfoCategory extends ModularUIJeiCategory<BedrockOreDefinition> {

    public final static RecipeType<BedrockOreDefinition> RECIPE_TYPE = new RecipeType<>(
            GTCEu.id("bedrock_ore_diagram"), BedrockOreDefinition.class);
    @Getter
    private final IDrawable icon;

    public GTBedrockOreInfoCategory(IJeiHelpers helpers) {
        super(OreVeinRecipeWidget::new,
                v -> ClientProxy.CLIENT_BEDROCK_ORE_VEINS.inverse().get(v));
        this.icon = helpers.getGuiHelper()
                .createDrawableItemStack(Items.RAW_IRON.getDefaultInstance());
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, ClientProxy.CLIENT_BEDROCK_ORE_VEINS.values().stream()
                .toList());
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LuV.asStack(), RECIPE_TYPE);
    }

    @Override
    public int getMaxWidth() {
        return 180;
    }

    @Override
    public int getMaxHeight() {
        return 300;
    }

    @Override
    public void setupRecipeIngredients(IRecipeLayoutBuilder builder, BedrockOreDefinition ore, IFocusGroup focuses) {
        Arrays.stream(OreVeinRecipeWidget.getDimensionMarkers(ore.dimensionFilter))
                .forEach(v -> builder.addSlot(RecipeIngredientRole.INPUT).addIngredient(VanillaTypes.ITEM_STACK,
                        v.getIcon()));

        OreVeinRecipeWidget.getRawMaterialList(ore).forEach(
                stack -> builder.addSlot(RecipeIngredientRole.OUTPUT).addIngredient(VanillaTypes.ITEM_STACK, stack));
    }

    @Override
    public RecipeType<BedrockOreDefinition> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.bedrock_ore_diagram");
    }
}
