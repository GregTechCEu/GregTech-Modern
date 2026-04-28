package com.gregtechceu.gtceu.api.recipe.ui;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class GTRecipeCategoryIcons {

    private GTRecipeCategoryIcons() {}

    public static Object defaultIcon(GTRecipeType recipeType) {
        if (recipeType.getIconSupplier() != null) {
            return new ItemStackTexture(recipeType.getIconSupplier().get());
        }
        return new ItemStackTexture(Items.BARRIER);
    }

    public static Object resource(Identifier location) {
        return new ResourceTexture(ResourceLocation.fromIdentifier(
                location.withPrefix("textures/").withSuffix(".png")));
    }

    public static Object itemStack(ItemStack... stacks) {
        return new ItemStackTexture(stacks);
    }
}
