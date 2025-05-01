package com.gregtechceu.gtceu.client.renderer.item;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.component.IDurabilityBar;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.client.util.DrawUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.ints.IntIntPair;

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
        DrawUtil.fillHorizontalGradient(graphics, RenderType.guiOverlay(), x, y, x + level, y + 1, left, right, 400);
        // graphics.fill(RenderType.guiOverlay(), x + BAR_W, y, x + BAR_W - level, y - 1, colorBG);
    }

    public static void renderBarsTool(GuiGraphics graphics, IGTTool tool, ItemStack stack, int xPosition,
                                      int yPosition) {
        boolean renderedDurability = false;
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.getBoolean(ToolHelper.UNBREAKABLE_KEY)) {
            renderedDurability = renderDurabilityBar(graphics, stack.getBarWidth(), xPosition, yPosition);
        }
        if (tool.isElectric()) {
            renderElectricBar(graphics, tool.getCharge(stack), tool.getMaxCharge(stack), xPosition, yPosition,
                    renderedDurability);
        }
    }

    public static boolean renderElectricBar(GuiGraphics graphics, long charge, long maxCharge, int xPosition,
                                            int yPosition, boolean renderedDurability) {
        if (charge > 0 && maxCharge > 0) {
            int level = Math.round(charge * 13.0F / maxCharge);
            render(graphics, level, xPosition, yPosition, renderedDurability ? 2 : 0, true, colorBarLeftEnergy,
                    colorBarRightEnergy, true);
            return true;
        }
        return false;
    }

    public static boolean renderDurabilityBar(GuiGraphics graphics, ItemStack stack, IDurabilityBar manager,
                                              int xPosition, int yPosition) {
        float level = manager.getDurabilityForDisplay(stack);
        if (level == 0.0 && !manager.showEmptyBar(stack)) return false;
        if (level == 1.0 && !manager.showFullBar(stack)) return false;
        IntIntPair colors = manager.getDurabilityColorsForDisplay(stack);
        boolean doDepletedColor = manager.doDamagedStateColors(stack);
        int left = colors != null ? colors.leftInt() : colorBarLeftDurability;
        int right = colors != null ? colors.rightInt() : colorBarRightDurability;
        render(graphics, manager.getBarWidth(stack), xPosition, yPosition, 0, true, left, right, doDepletedColor);
        return true;
    }

    private static boolean renderDurabilityBar(GuiGraphics graphics, int level, int xPosition, int yPosition) {
        render(graphics, level, xPosition, yPosition, 0, true, colorBarLeftDurability, colorBarRightDurability, true);
        return true;
    }

    private ToolChargeBarRenderer() {}
}
