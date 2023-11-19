package com.gregtechceu.gtceu.integration.jei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
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

public class GTOreVeinInfoCategory extends ModularUIRecipeCategory<GTOreVeinInfoWrapper> {
    public final static RecipeType<GTOreVeinInfoWrapper> Recipe_Type = new RecipeType<>(GTCEu.id("ore_vein_diagram"), GTOreVeinInfoWrapper.class);
    private final IDrawable background;
    private final IDrawable icon;

    public GTOreVeinInfoCategory(IJeiHelpers helpers) {
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(GTOreVeinWidget.width, 120);
        this.icon = helpers.getGuiHelper().createDrawableItemStack(ChemicalHelper.get(TagPrefix.ingot, GTMaterials.Iron));
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(Recipe_Type, GTRegistries.ORE_VEINS.values().stream()
                .map(GTOreVeinInfoWrapper::new)
                .toList());
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LV.asStack(), Recipe_Type);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), Recipe_Type);
        registration.addRecipeCatalyst(GTItems.PROSPECTOR_LUV.asStack(), Recipe_Type);
    }

    @Nonnull
    @Override
    public RecipeType<GTOreVeinInfoWrapper> getRecipeType() {
        return Recipe_Type;
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.ore_vein_diagram");
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
