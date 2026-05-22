package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreProcessingRecipeWidget;

import net.minecraft.network.chat.Component;

import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.rawOre;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Iron;

public class GTOreProcessingJeiCategory extends
                                        ModularUIRecipeCategory<GTOreProcessingJeiCategory.GTOreProcessingInfoWrapper> {

    public final static RecipeType<GTOreProcessingInfoWrapper> RECIPE_TYPE = new RecipeType<>(
            GTCEu.id("ore_processing_diagram"), GTOreProcessingInfoWrapper.class);
    private final IDrawable icon;

    public GTOreProcessingJeiCategory(IJeiHelpers helpers) {
        super(v -> new OreProcessingRecipeWidget(v.material), v -> v.material.getResourceLocation());
        this.icon = helpers.getGuiHelper().createDrawableItemStack(ChemicalHelper.get(rawOre, Iron));
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, GTCEuAPI.materialManager.getRegisteredMaterials().stream()
                .filter((material) -> material.hasProperty(PropertyKey.ORE) &&
                        !material.hasFlag(MaterialFlags.NO_ORE_PROCESSING_TAB))
                .map(GTOreProcessingInfoWrapper::new)
                .toList());
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(MACERATOR[GTValues.LV].asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(ORE_WASHER[GTValues.LV].asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(THERMAL_CENTRIFUGE[GTValues.LV].asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(CENTRIFUGE[GTValues.LV].asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(CHEMICAL_BATH[GTValues.LV].asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(ELECTROMAGNETIC_SEPARATOR[GTValues.LV].asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(SIFTER[GTValues.LV].asStack(), RECIPE_TYPE);
    }

    @Override
    @NotNull
    public RecipeType<GTOreProcessingInfoWrapper> getRecipeType() {
        return RECIPE_TYPE;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.ore_processing_diagram");
    }

    @NotNull
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    public record GTOreProcessingInfoWrapper(Material material) {}
}
