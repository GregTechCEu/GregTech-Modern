package com.gregtechceu.gtceu.integration.recipeviewer.jei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;
import lombok.Getter;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;

public class GTBedrockOreInfoCategory extends
                                      ModularUIRecipeCategory<GTBedrockOreInfoCategory.GTBedrockOreInfoWrapper> {

    public final static RecipeType<GTBedrockOreInfoWrapper> RECIPE_TYPE = new RecipeType<>(
            GTCEu.id("bedrock_ore_diagram"), GTBedrockOreInfoWrapper.class);
    @Getter
    private final IDrawable icon;

    public GTBedrockOreInfoCategory(IJeiHelpers helpers) {
        super(v -> new OreVeinRecipeWidget(v.bedrockOre),
                v -> ClientProxy.CLIENT_BEDROCK_ORE_VEINS.inverse().get(v.bedrockOre));
        this.icon = helpers.getGuiHelper()
                .createDrawableItemStack(Items.RAW_IRON.getDefaultInstance());
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, ClientProxy.CLIENT_BEDROCK_ORE_VEINS.values().stream()
                .map(GTBedrockOreInfoWrapper::new)
                .toList());
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LuV.asStack(), RECIPE_TYPE);
    }

    @NotNull
    @Override
    public RecipeType<GTBedrockOreInfoWrapper> getRecipeType() {
        return RECIPE_TYPE;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.bedrock_ore_diagram");
    }

    public record GTBedrockOreInfoWrapper(BedrockOreDefinition bedrockOre) {}
}
