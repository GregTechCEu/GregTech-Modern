package com.lowdragmc.lowdraglib.gui.util;

import com.lowdragmc.lowdraglib.utils.Rect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class DrawerHelper {

    public static void init() {}

    public static void drawFluidForGui(GuiGraphics graphics, FluidStack stack, long capacity, int x, int y, int width,
                                       int height) {
        drawSolidRect(graphics, x, y, width, height, 0x7F3F76E4);
    }

    public static void drawFluidForGui(GuiGraphics graphics, FluidStack stack, float x, float y, float width,
                                       float height) {
        drawSolidRect(graphics, (int) x, (int) y, (int) width, (int) height, 0x7F3F76E4);
    }

    public static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color, int border) {
        graphics.fill(x, y, x + width, y + border, color);
        graphics.fill(x, y + height - border, x + width, y + height, color);
        graphics.fill(x, y, x + border, y + height, color);
        graphics.fill(x + width - border, y, x + width, y + height, color);
    }

    public static void drawStringSized(GuiGraphics graphics, String text, float x, float y, int color,
                                       boolean dropShadow, float scale, boolean center) {
        graphics.drawString(Minecraft.getInstance().font, text, (int) x, (int) y, color, dropShadow);
    }

    public static void drawStringFixedCorner(GuiGraphics graphics, String text, float x, float y, int color,
                                             boolean dropShadow, float scale) {
        drawStringSized(graphics, text, x, y, color, dropShadow, scale, false);
    }

    public static void drawText(GuiGraphics graphics, String text, float x, float y, float scale, int color) {
        drawText(graphics, text, x, y, scale, color, false);
    }

    public static void drawText(GuiGraphics graphics, String text, float x, float y, float scale, int color,
                                boolean dropShadow) {
        drawStringSized(graphics, text, x, y, color, dropShadow, scale, false);
    }

    public static void drawItemStack(GuiGraphics graphics, ItemStack stack, int x, int y, int color, String altText) {
        graphics.renderItem(stack, x, y);
    }

    public static List<Component> getItemToolTip(ItemStack stack) {
        return stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, Minecraft.getInstance().player,
                net.minecraft.world.item.TooltipFlag.Default.NORMAL);
    }

    public static void drawSolidRect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
    }

    public static void drawSolidRect(GuiGraphics graphics, Rect rect, int color) {
        drawSolidRect(graphics, rect.position.x, rect.position.y, rect.size.width, rect.size.height, color);
    }

    public static void drawRectShadow(GuiGraphics graphics, int x, int y, int width, int height, int distance) {}

    public static void drawGradientRect(GuiGraphics graphics, int x, int y, int width, int height, int startColor,
                                        int endColor) {
        graphics.fillGradient(x, y, x + width, y + height, startColor, endColor);
    }

    public static void drawGradientRect(GuiGraphics graphics, float x, float y, float width, float height,
                                        int startColor, int endColor, boolean horizontal) {
        drawGradientRect(graphics, (int) x, (int) y, (int) width, (int) height, startColor, endColor);
    }

    public static void drawLines(GuiGraphics graphics, List<Vec2> points, int color, int width, float partialTicks) {}

    public static void drawTextureRect(GuiGraphics graphics, float x, float y, float width, float height) {}
}
