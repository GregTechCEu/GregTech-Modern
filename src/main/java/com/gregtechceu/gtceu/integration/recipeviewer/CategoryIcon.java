package com.gregtechceu.gtceu.integration.recipeviewer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.GTJEIPlugin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import mezz.jei.api.gui.drawable.IDrawable;

// Generic recipe viewer category icon
public class CategoryIcon {

    // Resolved lazily: constructing icons happens during RegisterEvent dispatch
    // (GTRecipeCategories.init), when the JEI runtime does not exist yet —
    // calling GTJEIPlugin.getRuntime() there crashes the client (unbound
    // gtceu:data_item follow-up). Defer wrapper creation to first get().
    private Object wrappedValue;
    private ResourceLocation texture;
    private ItemStack stack;
    private boolean resolved = false;

    public CategoryIcon(ResourceLocation texture) {
        this.texture = texture;
    }

    public CategoryIcon(ItemStack stack) {
        this.stack = stack;
    }

    public Object get() {
        if (!resolved) {
            resolved = true;
            if (GTCEu.isClientSide()) {
                if (GTCEu.Mods.isEMILoaded()) {
                    wrappedValue = texture != null ? EmiCallWrapper.getRenderable(texture) :
                            EmiCallWrapper.getRenderable(stack);
                } else if (GTCEu.Mods.isJEILoaded()) {
                    wrappedValue = texture != null ? JeiCallWrapper.getRenderable(texture) :
                            JeiCallWrapper.getRenderable(stack);
                }
            }
        }
        return wrappedValue;
    }

    private static class EmiCallWrapper {

        public static EmiRenderable getRenderable(ResourceLocation location) {
            return new EmiTexture(location, 0, 0, 16, 16, 16, 16, 16, 16);
        }

        public static EmiRenderable getRenderable(ItemStack stack) {
            return EmiStack.of(stack);
        }
    }

    private static class JeiCallWrapper {

        public static IDrawable getRenderable(ResourceLocation location) {
            return GTJEIPlugin.getRuntime().getJeiHelpers().getGuiHelper().drawableBuilder(location, 0, 0, 16, 16)
                    .setTextureSize(16, 16).build();
        }

        public static IDrawable getRenderable(ItemStack stack) {
            return GTJEIPlugin.getRuntime().getJeiHelpers().getGuiHelper().createDrawableItemStack(stack);
        }
    }
}
