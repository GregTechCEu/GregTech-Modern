package com.gregtechceu.gtceu.integration.recipeviewer.jei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;

import net.minecraft.network.chat.Component;

import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;

public class GTBedrockFluidInfoCategory extends
                                        ModularUIRecipeCategory<GTBedrockFluidInfoCategory.BedrockFluidInfoWrapper> {

    public final static RecipeType<BedrockFluidInfoWrapper> RECIPE_TYPE = new RecipeType<>(
            GTCEu.id("bedrock_fluid_diagram"), BedrockFluidInfoWrapper.class);
    private final IDrawable icon;

    public GTBedrockFluidInfoCategory(IJeiHelpers helpers) {
        super(v -> new OreVeinRecipeWidget(v.fluid), v -> ClientProxy.CLIENT_FLUID_VEINS.inverse().get(v.fluid));
        this.icon = helpers.getGuiHelper()
                .createDrawableItemStack(GTMaterials.Oil.getFluid().getBucket().asItem().getDefaultInstance());
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, ClientProxy.CLIENT_FLUID_VEINS.values().stream()
                .map(BedrockFluidInfoWrapper::new)
                .toList());
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LuV.asStack(), RECIPE_TYPE);
    }

    @NotNull
    @Override
    public RecipeType<BedrockFluidInfoWrapper> getRecipeType() {
        return RECIPE_TYPE;
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

    public record BedrockFluidInfoWrapper(BedrockFluidDefinition fluid) {}
}
