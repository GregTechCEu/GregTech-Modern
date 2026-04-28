package com.gregtechceu.gtceu.client.renderer.item.decorator;

import com.gregtechceu.gtceu.api.item.IComponentItem;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

import org.jetbrains.annotations.NotNull;

public final class GTComponentItemDecorator implements IItemDecorator {

    public static final GTComponentItemDecorator INSTANCE = new GTComponentItemDecorator();

    private GTComponentItemDecorator() {}

    @Override
    public boolean render(@NotNull GuiGraphicsExtractor GuiGraphicsExtractor, @NotNull Font font,
                          ItemStack stack, int xOffset, int yOffset) {
        if (!(stack.getItem() instanceof IComponentItem componentItem)) {
            return false;
        }

        boolean modified = false;
        for (var component : componentItem.getComponents()) {
            if (component instanceof IItemDecorator decorator) {
                modified |= decorator.render(GuiGraphicsExtractor, font, stack, xOffset, yOffset);
            }
        }
        return modified;
    }
}
