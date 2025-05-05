package com.gregtechceu.gtceu.client.renderer.item.decorator;

import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemDecoratorComponent;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public final class GTComponentItemDecorator implements IItemDecorator {

    public static final GTComponentItemDecorator INSTANCE = new GTComponentItemDecorator();

    private GTComponentItemDecorator() {}

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (stack.getItem() instanceof IComponentItem componentItem) {
            var retVal = false;
            for (var component : componentItem.getComponents()) {
                if (component instanceof IItemDecoratorComponent itemDecoratorComponent) {
                    retVal |= itemDecoratorComponent.render(guiGraphics, font, stack, xOffset, yOffset);
                }
            }
            return retVal;
        }
        return false;
    }
}
