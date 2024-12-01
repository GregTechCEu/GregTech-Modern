package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.resources.ResourceLocation;

public class GTRecipeCategoryBuilder extends BuilderBase<GTRecipeCategory> {

    public transient String name;
    public transient GTRecipeType recipeType;
    private transient IGuiTexture icon;

    public GTRecipeCategoryBuilder(ResourceLocation id, Object... args) {
        super(id);
        name = id.getPath();
        recipeType = null;
        icon = null;
    }

    public GTRecipeCategoryBuilder recipeType(GTRecipeType recipeType) {
        this.recipeType = recipeType;
        return this;
    }

    public GTRecipeCategoryBuilder setIcon(IGuiTexture guiTexture) {
        this.icon = guiTexture;
        return this;
    }

    public GTRecipeCategoryBuilder setCustomIcon(ResourceLocation location) {
        this.icon = new ResourceTexture(location.withPrefix("textures/").withSuffix(".png"));
        return this;
    }

    @Override
    public GTRecipeCategory register() {
        var category = GTRecipeCategories.register(name, recipeType);
        category.setIcon(icon);
        return value = category;
    }
}
