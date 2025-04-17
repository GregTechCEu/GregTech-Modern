package com.gregtechceu.gtceu.client.renderer.item.decorator;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.client.renderer.item.ToolChargeBarRenderer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

import org.jetbrains.annotations.NotNull;

public class GTItemBarRenderer implements IItemDecorator {

    public static final GTItemBarRenderer INSTANCE = new GTItemBarRenderer();

    @Override
    public boolean render(@NotNull GuiGraphics guiGraphics, @NotNull Font font, ItemStack stack, int x, int y) {
        GTCEu.LOGGER.info("Rendering item bar decorator");
        if (stack.getItem() instanceof IGTTool toolItem) {
            GTCEu.LOGGER.info("Rendering tool bar decorator");
            ToolChargeBarRenderer.renderBarsTool(guiGraphics, toolItem, stack, x, y);
            return true;
        } else if (stack.getItem() instanceof IComponentItem componentItem) {
            GTCEu.LOGGER.info("Rendering component item bar decorator");
            ToolChargeBarRenderer.renderBarsItem(guiGraphics, componentItem, stack, x, y);
            return true;
        }
        return false;
    }
}
