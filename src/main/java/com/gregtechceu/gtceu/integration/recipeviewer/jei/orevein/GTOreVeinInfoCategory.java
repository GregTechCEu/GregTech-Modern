package com.gregtechceu.gtceu.integration.recipeviewer.jei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;

import net.minecraft.network.chat.Component;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;
import mezz.jei.api.constants.VanillaTypes;
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

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GTOreVeinInfoCategory extends ModularUIJeiCategory<GTOreDefinition> {

    public final static RecipeType<GTOreDefinition> RECIPE_TYPE = new RecipeType<>(GTCEu.id("ore_vein_diagram"),
            GTOreDefinition.class);
    private final IDrawable icon;

    public GTOreVeinInfoCategory(IJeiHelpers helpers) {
        super(OreVeinRecipeWidget::new, v -> ClientProxy.CLIENT_ORE_VEINS.inverse().get(v));

        this.icon = helpers.getGuiHelper()
                .createDrawableItemStack(ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Iron));
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, ClientProxy.CLIENT_ORE_VEINS.values().stream()
                .toList());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GTOreDefinition oreDefinition, IFocusGroup focuses) {
        super.setRecipe(builder, oreDefinition, focuses);
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                .addItemStacks(OreVeinRecipeWidget.getContainedOresAndBlocks(oreDefinition));
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LuV.asStack(), RECIPE_TYPE);
    }

    @NotNull
    @Override
    public RecipeType<GTOreDefinition> getRecipeType() {
        return RECIPE_TYPE;
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
    public void setupRecipeIngredients(IRecipeLayoutBuilder builder, GTOreDefinition ore, IFocusGroup focuses) {
        Arrays.stream(OreVeinRecipeWidget.getDimensionMarkers(ore.dimensionFilter()))
                .forEach(v -> builder.addSlot(RecipeIngredientRole.INPUT).addIngredient(VanillaTypes.ITEM_STACK,
                        v.getIcon()));

        OreVeinRecipeWidget.getContainedOresAndBlocks(ore)
                .forEach(v -> builder.addSlot(RecipeIngredientRole.OUTPUT).addIngredient(VanillaTypes.ITEM_STACK, v));
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.ore_vein_diagram");
    }

    @NotNull
    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
