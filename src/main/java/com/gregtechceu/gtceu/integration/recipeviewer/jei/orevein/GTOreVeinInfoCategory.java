package com.gregtechceu.gtceu.integration.recipeviewer.jei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;
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
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GTOreVeinInfoCategory extends ModularUIRecipeCategory<GTOreDefinition> {

    public final static RecipeType<GTOreDefinition> RECIPE_TYPE = new RecipeType<>(GTCEu.id("ore_vein_diagram"),
            GTOreDefinition.class);
    private final IDrawable icon;

    public GTOreVeinInfoCategory(IJeiHelpers helpers) {
        super(OreVeinRecipeWidget::new,
                v -> Objects.requireNonNull(Minecraft.getInstance().level).registryAccess()
                        .registryOrThrow(GTRegistries.Keys.ORE_VEIN).getKey(v));

        this.icon = helpers.getGuiHelper()
                .createDrawableItemStack(ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Iron));
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, Minecraft.getInstance().level.registryAccess()
                .registryOrThrow(GTRegistries.Keys.ORE_VEIN).stream()
                .toList());
    }

    public int getMaxWidth() {
        return 180;
    }

    public int getMaxHeight() {
        return 300;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GTOreDefinition ore, IFocusGroup focuses) {
        Arrays.stream(OreVeinRecipeWidget.getDimensionMarkers(ore.dimensionFilter()))
                .forEach(v -> builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addIngredient(
                        VanillaTypes.ITEM_STACK,
                        v.getIcon()));

        OreVeinRecipeWidget.getContainedOresAndBlocks(ore)
                .forEach(v -> builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                        .addIngredient(VanillaTypes.ITEM_STACK, v));
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LuV.asStack(), RECIPE_TYPE);
    }

    @Override
    public RecipeType<GTOreDefinition> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.ore_vein_diagram");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
