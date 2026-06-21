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

import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GTOreVeinInfoCategory extends ModularUIRecipeCategory<GTOreVeinInfoCategory.GTOreVeinInfoWrapper> {

    public final static RecipeType<GTOreVeinInfoWrapper> RECIPE_TYPE = new RecipeType<>(GTCEu.id("ore_vein_diagram"),
            GTOreVeinInfoWrapper.class);
    private final IDrawable icon;

    public GTOreVeinInfoCategory(IJeiHelpers helpers) {
        super(v -> new OreVeinRecipeWidget(v.oreDefinition),
                v -> ClientProxy.CLIENT_ORE_VEINS.inverse().get(v.oreDefinition));

        this.icon = helpers.getGuiHelper()
                .createDrawableItemStack(ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Iron));
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, ClientProxy.CLIENT_ORE_VEINS.values().stream()
                .map(GTOreVeinInfoWrapper::new)
                .toList());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GTOreVeinInfoWrapper wrapper, IFocusGroup focuses) {
        super.setRecipe(builder, wrapper, focuses);
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                .addItemStacks(OreVeinRecipeWidget.getContainedOresAndBlocks(wrapper.oreDefinition));
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LuV.asStack(), RECIPE_TYPE);
    }

    @NotNull
    @Override
    public RecipeType<GTOreVeinInfoWrapper> getRecipeType() {
        return RECIPE_TYPE;
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

    public record GTOreVeinInfoWrapper(GTOreDefinition oreDefinition) {}
}
