package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.component.IDurabilityBar;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;

public final class ToolChargeBarRenderer {

    private static final int BAR_W = 12;

    private static final int colorShadow = FastColor.ARGB32.color(255, 0, 0, 0);
    private static final int colorBG = FastColor.ARGB32.color(255, 0x0E, 0x01, 0x16);

    private static final int colorBarLeftEnergy = FastColor.ARGB32.color(255, 0, 101, 178);
    private static final int colorBarRightEnergy = FastColor.ARGB32.color(255, 217, 238, 255);

    private static final int colorBarLeftDurability = FastColor.ARGB32.color(255, 20, 124, 0);
    private static final int colorBarRightDurability = FastColor.ARGB32.color(255, 115, 255, 89);

    private static final int colorBarLeftDepleted = FastColor.ARGB32.color(255, 122, 0, 0);
    private static final int colorBarRightDepleted = FastColor.ARGB32.color(255, 255, 27, 27);

    public static void render(GuiGraphics graphics, int level, int xPosition, int yPosition, int offset, boolean shadow,
                              int left, int right, boolean doDepletedColor) {
        if (doDepletedColor && level <= BAR_W / 4) {
            left = colorBarLeftDepleted;
            right = colorBarRightDepleted;
        }

        int x = xPosition + 2;
        int y = yPosition + 13 - offset;
        graphics.fill(RenderType.guiOverlay(), x, y, x + 13, y + (shadow ? 2 : 1), 400, colorShadow);
        fillHorizontalGradient(graphics, RenderType.guiOverlay(), x, y, x + level, y + 1, left, right, 400);
        // graphics.fill(RenderType.guiOverlay(), x + BAR_W, y, x + BAR_W - level, y - 1, colorBG);
    }

    public static void renderBarsTool(GuiGraphics graphics, IGTTool tool, ItemStack stack, int xPosition,
                                      int yPosition) {
        boolean renderedDurability = false;
        if (!stack.has(DataComponents.UNBREAKABLE)) {
            renderedDurability = renderDurabilityBar(graphics, stack.getBarWidth(), xPosition,
                    yPosition);
        }
        if (tool.isElectric()) {
            renderElectricBar(graphics, tool.getCharge(stack), tool.getMaxCharge(stack), xPosition, yPosition,
                    renderedDurability);
        }
    }

    public static void renderBarsItem(GuiGraphics graphics, IComponentItem item, ItemStack stack, int xPosition,
                                      int yPosition) {
        boolean renderedDurability = false;
        IDurabilityBar bar = null;
        for (IItemComponent component : item.getComponents()) {
            if (component instanceof IDurabilityBar durabilityBar) {
                bar = durabilityBar;
            }
        }
        if (bar != null) {
            renderedDurability = renderDurabilityBar(graphics, stack, bar, xPosition, yPosition);
        }

        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null) {
            renderElectricBar(graphics, electricItem.getCharge(), electricItem.getMaxCharge(), xPosition, yPosition,
                    renderedDurability);
        }
    }

    private static void renderElectricBar(GuiGraphics graphics, long charge, long maxCharge, int xPosition,
                                          int yPosition, boolean renderedDurability) {
        if (charge > 0 && maxCharge > 0) {
            int level = Math.round(charge * 13.0F / maxCharge);
            render(graphics, level, xPosition, yPosition, renderedDurability ? 2 : 0, true, colorBarLeftEnergy,
                    colorBarRightEnergy, true);
        }
    }

    private static boolean renderDurabilityBar(GuiGraphics graphics, ItemStack stack, IDurabilityBar manager,
                                               int xPosition, int yPosition) {
        float level = manager.getDurabilityForDisplay(stack);
        if (level == 0.0 && !manager.showEmptyBar(stack)) return false;
        if (level == 1.0 && !manager.showFullBar(stack)) return false;
        Pair<Integer, Integer> colors = manager.getDurabilityColorsForDisplay(stack);
        boolean doDepletedColor = manager.doDamagedStateColors(stack);
        int left = colors != null ? colors.getLeft() : colorBarLeftDurability;
        int right = colors != null ? colors.getRight() : colorBarRightDurability;
        render(graphics, manager.getBarWidth(stack), xPosition, yPosition, 0, true, left, right, doDepletedColor);
        return true;
    }

    private static boolean renderDurabilityBar(GuiGraphics graphics, int level, int xPosition, int yPosition) {
        render(graphics, level, xPosition, yPosition, 0, true, colorBarLeftDurability, colorBarRightDurability, true);
        return true;
    }

    /**
     * Fills a rectangle with a gradient color from colorFrom to colorTo at the specified z-level using the given render
     * type and coordinates as the boundaries.
     *
     * @param y2         the y-coordinate of the second corner of the rectangle.
     * @param x2         the x-coordinate of the second corner of the rectangle.
     * @param y1         the y-coordinate of the first corner of the rectangle.
     * @param x1         the x-coordinate of the first corner of the rectangle.
     * @param renderType the render type to use.
     * @param z          the z-level of the rectangle.
     * @param colorTo    the ending color of the gradient.
     * @param colorFrom  the starting color of the gradient.
     */
    public static void fillHorizontalGradient(GuiGraphics graphics, RenderType renderType, int x1, int y1, int x2,
                                              int y2, int colorFrom, int colorTo, int z) {
        VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(renderType);
        fillHorizontalGradient(graphics, vertexconsumer, x1, y1, x2, y2, z, colorFrom, colorTo);
    }

    /**
     * The core `fillGradient` method.
     * <p>
     * Fills a rectangle with a gradient color from colorFrom to colorTo at the specified z-level using the given render
     * type and coordinates as the boundaries.
     *
     * @param consumer  the {@linkplain VertexConsumer} object for drawing the vertices on screen.
     * @param x1        the x-coordinate of the first corner of the rectangle.
     * @param y1        the y-coordinate of the first corner of the rectangle.
     * @param x2        the x-coordinate of the second corner of the rectangle.
     * @param y2        the y-coordinate of the second corner of the rectangle.
     * @param z         the z-level of the rectangle.
     * @param colorFrom the starting color of the gradient.
     * @param colorTo   the ending color of the gradient.
     */
    private static void fillHorizontalGradient(GuiGraphics graphics, VertexConsumer consumer, int x1, int y1, int x2,
                                               int y2, int z, int colorFrom, int colorTo) {
        float a1 = (float) FastColor.ARGB32.alpha(colorFrom) / 255.0F;
        float r1 = (float) FastColor.ARGB32.red(colorFrom) / 255.0F;
        float g1 = (float) FastColor.ARGB32.green(colorFrom) / 255.0F;
        float b1 = (float) FastColor.ARGB32.blue(colorFrom) / 255.0F;
        float a2 = (float) FastColor.ARGB32.alpha(colorTo) / 255.0F;
        float r2 = (float) FastColor.ARGB32.red(colorTo) / 255.0F;
        float g2 = (float) FastColor.ARGB32.green(colorTo) / 255.0F;
        float b2 = (float) FastColor.ARGB32.blue(colorTo) / 255.0F;
        Matrix4f matrix4f = graphics.pose().last().pose();
        consumer.vertex(matrix4f, (float) x1, (float) y1, (float) z).color(r1, g1, b1, a1).endVertex();
        consumer.vertex(matrix4f, (float) x1, (float) y2, (float) z).color(r1, g1, b1, a1).endVertex();
        consumer.vertex(matrix4f, (float) x2, (float) y2, (float) z).color(r2, g2, b2, a2).endVertex();
        consumer.vertex(matrix4f, (float) x2, (float) y1, (float) z).color(r2, g2, b2, a2).endVertex();
    }

    private ToolChargeBarRenderer() {}
}
