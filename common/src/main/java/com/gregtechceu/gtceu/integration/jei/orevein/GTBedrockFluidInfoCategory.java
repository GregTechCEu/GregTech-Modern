package com.gregtechceu.gtceu.integration.jei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.GTOreVeinWidget;
import com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class GTBedrockFluidInfoCategory extends ModularUIRecipeCategory<GTBedrockFluidInfoWrapper> {
    public final static RecipeType<GTBedrockFluidInfoWrapper> Recipe_Type = new RecipeType<>(GTCEu.id("bedrock_fluid_diagram"), GTBedrockFluidInfoWrapper.class);
    private final IDrawable background;
    private final IDrawable icon;

    public GTBedrockFluidInfoCategory(IJeiHelpers helpers) {
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(GTOreVeinWidget.width, 120);
        this.icon = helpers.getGuiHelper().createDrawableItemStack(GTMaterials.Oil.getFluid().getBucket().asItem().getDefaultInstance());
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(Recipe_Type, GTRegistries.BEDROCK_FLUID_DEFINITIONS.values().stream()
                .map(GTBedrockFluidInfoWrapper::new)
                .toList());
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LV.asStack(), Recipe_Type);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), Recipe_Type);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LUV.asStack(), Recipe_Type);
    }

    @Nonnull
    @Override
    public RecipeType<GTBedrockFluidInfoWrapper> getRecipeType() {
        return Recipe_Type;
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.bedrock_fluid_diagram");
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Nonnull
    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
