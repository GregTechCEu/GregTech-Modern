package com.gregtechceu.gtceu.integration.recipeviewer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.GTJEIPlugin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import mezz.jei.api.gui.drawable.IDrawable;

// Generic recipe viewer category icon
public class CategoryIcon {

    private Object wrappedValue;

    public CategoryIcon(ResourceLocation texture) {
        if (!GTCEu.isClientSide()) return;
        if (GTCEu.Mods.isEMILoaded()) {
            wrappedValue = EmiCallWrapper.getRenderable(texture);
        } else if (GTCEu.Mods.isREILoaded()) {
            wrappedValue = ReiCallWrapper.getRenderable(texture);
        } else if (GTCEu.Mods.isJEILoaded()) {
            wrappedValue = JeiCallWrapper.getRenderable(texture);
        }
    }

    public CategoryIcon(ItemStack stack) {
        if (!GTCEu.isClientSide()) return;
        if (GTCEu.Mods.isEMILoaded()) {
            wrappedValue = EmiCallWrapper.getRenderable(stack);
        } else if (GTCEu.Mods.isREILoaded()) {
            wrappedValue = ReiCallWrapper.getRenderable(stack);
        } else if (GTCEu.Mods.isJEILoaded()) {
            wrappedValue = JeiCallWrapper.getRenderable(stack);
        }
    }

    public Object get() {
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

    private static class ReiCallWrapper {

        public static Renderer getRenderable(ResourceLocation location) {
            return Widgets.createTexturedWidget(location, 0, 0, 16, 16);
        }

        public static Renderer getRenderable(ItemStack stack) {
            return EntryStack.of(VanillaEntryTypes.ITEM, stack);
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
