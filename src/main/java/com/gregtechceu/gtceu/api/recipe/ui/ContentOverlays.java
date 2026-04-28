package com.gregtechceu.gtceu.api.recipe.ui;

import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GradientUtil;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import org.jetbrains.annotations.Nullable;

public final class ContentOverlays {

    private ContentOverlays() {}

    public static Object createOverlay(Content content, boolean perTick, int recipeTier, int chanceTier,
                                       @Nullable ChanceBoostFunction function) {
        return new IGuiTexture() {

            @Override
            @OnlyIn(Dist.CLIENT)
            public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
                drawChance(content, graphics, x, y, width, height, recipeTier, chanceTier, function);
                drawRangeAmount(content, graphics, x, y, width, height);
                drawFluidAmount(content, graphics, x, y, width, height);
                if (perTick) {
                    drawTick(content, graphics, x, y, width, height);
                }
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawRangeAmount(Content content, GuiGraphics graphics, float x, float y, int width,
                                       int height) {
        if (content.content instanceof IntProviderIngredient ingredient) {
            int min = ingredient.getCountProvider().minInclusive();
            int max = ingredient.getCountProvider().maxInclusive();
            String text = String.format("%s-%s", min, max);
            int color = 0xFFFFFF;
            if (text.length() > 5) {
                text = "X-Y";
                color = ChatFormatting.GOLD.getColor();
            }
            drawHalfScaleString(graphics, text, x, y, width, height, color, 21, 0, true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawFluidAmount(Content content, GuiGraphics graphics, float x, float y, int width,
                                       int height) {
        if (content.content instanceof SizedFluidIngredient ingredient) {
            Font font = Minecraft.getInstance().font;
            int color;
            String text;
            if (ingredient.ingredient() instanceof IntProviderFluidIngredient) {
                color = ChatFormatting.GOLD.getColor();
                text = "X-Y";
            } else {
                int amount = ingredient.amount();
                color = 0xFFFFFF;
                text = FormattingUtil.formatBuckets(amount);
                if (font.width(text) > 32) {
                    text = FormattingUtil.formatNumberReadable(amount, true, FormattingUtil.DECIMAL_FORMAT_1F, "B");
                }
                if (font.width(text) > 32) {
                    text = FormattingUtil.formatNumberReadable(amount, true, FormattingUtil.DECIMAL_FORMAT_0F, "B");
                }
            }
            drawHalfScaleString(graphics, text, x, y, width, height, color, 22, 0, true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawChance(Content content, GuiGraphics graphics, float x, float y, int width, int height,
                                  int recipeTier, int chanceTier, @Nullable ChanceBoostFunction function) {
        if (content.chance == ChanceLogic.getMaxChancedValue()) return;

        var boostFunction = function == null ? ChanceBoostFunction.NONE : function;
        int chance = boostFunction.getBoostedChance(content, recipeTier, chanceTier);
        float chanceFloat = 1f * chance / content.maxChance;
        String percent = FormattingUtil.formatNumber2Places(100 * chanceFloat);
        String text = chance == 0 ? LocalizationUtils.format("gtceu.gui.content.chance_nc_short") : percent + "%";
        int color = chance == 0 ? 0xFF0000 : GradientUtil.toRGB(Mth.lerp(chanceFloat, 29f, 167f), 100f, 50f);
        drawHalfScaleString(graphics, text, x, y, width, height, color, 23, -height, true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawTick(Content content, GuiGraphics graphics, float x, float y, int width, int height) {
        String text = LocalizationUtils.format("gtceu.gui.content.tips.per_tick_short");
        int yOffset = -height + (content.chance == ChanceLogic.getMaxChancedValue() ? 0 : 10);
        drawHalfScaleString(graphics, text, x, y, width, height, 0xFFFF00, 23, yOffset, false);
    }

    @OnlyIn(Dist.CLIENT)
    private static void drawHalfScaleString(GuiGraphics graphics, String text, float x, float y, int width,
                                            int height, int color, int xOffset, int yOffset, boolean shadow) {
        Font font = Minecraft.getInstance().font;
        graphics.nextStratum();
        graphics.pose().pushMatrix();
        graphics.pose().scale(0.5f, 0.5f);
        graphics.drawString(font, text, (int) ((x + (width / 3f)) * 2 - font.width(text) + xOffset),
                (int) ((y + (height / 3f) + 6) * 2 + yOffset), color, shadow);
        graphics.pose().popMatrix();
    }
}
