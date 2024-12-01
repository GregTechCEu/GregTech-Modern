package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import net.minecraft.resources.ResourceLocation;

public class GTRecipeCategoryBuilder extends BuilderBase<GTRecipeCategory> {

    public transient String name;
    public transient GTRecipeType recipeType;
    private transient IGuiTexture icon;

    public GTRecipeCategoryBuilder(ResourceLocation id, Object... args) {
        super(id);
        name = id.getPath();
    }

    @Override
    public GTRecipeCategory register() {
        return null;
    }
}
