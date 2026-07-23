package com.gregtechceu.gtceu.integration.recipeviewer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.GTJEIPlugin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import mezz.jei.api.gui.drawable.IDrawable;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

// Generic recipe viewer category icon
public class CategoryIcon {

    @Nullable
    private Supplier<Object> lazyValue;
    @Nullable
    private Object resolvedValue;

    public CategoryIcon(ResourceLocation texture) {
        if (!GTCEu.isClientSide()) return;
        if (GTCEu.Mods.isEMILoaded()) {
            resolvedValue = EmiCallWrapper.getRenderable(texture);
        } else if (GTCEu.Mods.isJEILoaded()) {
            lazyValue = () -> JeiCallWrapper.getRenderable(texture);
        }
    }

    public CategoryIcon(ItemStack stack) {
        if (!GTCEu.isClientSide()) return;
        if (GTCEu.Mods.isEMILoaded()) {
            resolvedValue = EmiCallWrapper.getRenderable(stack);
        } else if (GTCEu.Mods.isJEILoaded()) {
            lazyValue = () -> JeiCallWrapper.getRenderable(stack);
        }
    }

    @Nullable
    public Object get() {
        if (resolvedValue == null && lazyValue != null) {
            resolvedValue = lazyValue.get();
            lazyValue = null;
        }
        return resolvedValue;
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
