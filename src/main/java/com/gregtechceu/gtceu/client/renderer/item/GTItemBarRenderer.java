package com.gregtechceu.gtceu.client.renderer.item;

import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.client.util.ToolChargeBarRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import org.jetbrains.annotations.NotNull;

public class GTItemBarRenderer implements IItemDecorator {
    @Override
    public boolean render(@NotNull GuiGraphics guiGraphics, @NotNull Font font, ItemStack stack, int x, int y) {
        if (stack.getItem() instanceof IGTTool toolItem) {
            ToolChargeBarRenderer.renderBarsTool(guiGraphics, toolItem, stack, x, y);
        } else if (stack.getItem() instanceof IComponentItem componentItem) {
            ToolChargeBarRenderer.renderBarsItem(guiGraphics, componentItem, stack, x, y);
        }
        return false;
    }
}
