package com.gregtechceu.gtceu.api.recipe.ui;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class GTRecipeCategoryIcons {

    private GTRecipeCategoryIcons() {}

    public static IGuiTexture defaultIcon(GTRecipeType recipeType) {
        if (recipeType.getIconSupplier() != null) {
            return new ItemStackTexture(recipeType.getIconSupplier().get());
        }
        return new ItemStackTexture(Items.BARRIER);
    }

    public static IGuiTexture resource(Identifier location) {
        return new ResourceTexture(location.withPrefix("textures/").withSuffix(".png"));
    }

    public static IGuiTexture itemStack(ItemStack... stacks) {
        return new ItemStackTexture(stacks);
    }
}
