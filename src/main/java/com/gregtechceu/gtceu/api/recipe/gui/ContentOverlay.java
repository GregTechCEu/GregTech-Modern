package com.gregtechceu.gtceu.api.recipe.gui;

import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GradientUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public record ContentOverlay(Content content, boolean perTick, int recipeTier, int chanceTier,
                             @Nullable ChanceBoostFunction function)
        implements IDrawable {

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        drawChance(context.getGraphics(), x, y, width, height, recipeTier, chanceTier, function);
        drawRangeAmount(context.getGraphics(), x, y, width, height);
        drawFluidAmount(context.getGraphics(), x, y, width, height);
        if (perTick) {
            drawTick(context.getGraphics(), x, y, width, height);
        }
    }

    public void drawRangeAmount(GuiGraphics graphics, float x, float y, int width, int height) {
        if (content.content() instanceof IntProviderIngredient ingredient) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 400);
            graphics.pose().scale(0.5f, 0.5f, 1);
            int min = ingredient.getCountProvider().getMinValue();
            int max = ingredient.getCountProvider().getMaxValue();
            String s = String.format("%s-%s", min, max);
            int color = 0xFFFFFF;
            Font fontRenderer = Minecraft.getInstance().font;
            // 5 == max num of characters that fit in a slot at 0.5x render size
            if (s.length() > 5) {
                s = "X-Y";
                color = ChatFormatting.GOLD.getColor(); // Orange?
            }
            graphics.drawString(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 21),
                    (int) ((y + (height / 3f) + 6) * 2), color, true);
            graphics.pose().popPose();
        }
    }

    public void drawFluidAmount(GuiGraphics graphics, float x, float y, int width, int height) {
        if (content.content() instanceof FluidIngredient ingredient) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 400);
            graphics.pose().scale(0.5f, 0.5f, 1);
            Font fontRenderer = Minecraft.getInstance().font;
            int color;
            String s;
            if (content.content() instanceof IntProviderFluidIngredient) {
                // with only 5 characters worth of space, that's not enough for a fluid range
                color = ChatFormatting.GOLD.getColor();
                s = "X-Y";
            } else {
                int amount = ingredient.getAmount();
                color = 0xFFFFFF;
                s = FormattingUtil.formatBuckets(amount);
                if (fontRenderer.width(s) > 32)
                    s = FormattingUtil.formatNumberReadable(amount, true, FormattingUtil.DECIMAL_FORMAT_1F, "B");
                if (fontRenderer.width(s) > 32)
                    s = FormattingUtil.formatNumberReadable(amount, true, FormattingUtil.DECIMAL_FORMAT_0F, "B");
            }
            graphics.drawString(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 22),
                    (int) ((y + (height / 3f) + 6) * 2), color, true);
            graphics.pose().popPose();
        }
    }

    public void drawChance(GuiGraphics graphics, float x, float y, int width, int height, int recipeTier,
                           int chanceTier, @Nullable ChanceBoostFunction function) {
        if (content.chance() == ChanceLogic.getMaxChancedValue()) return;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        graphics.pose().scale(0.5f, 0.5f, 1);
        var func = function == null ? ChanceBoostFunction.NONE : function;
        int chance = func.getBoostedChance(content, recipeTier, chanceTier);
        float chanceFloat = 1f * chance / content.maxChance();
        String percent = FormattingUtil.formatNumber2Places(100 * chanceFloat);

        Component s = chance == 0 ? Component.translatable("gtceu.gui.content.chance_nc_short") :
                Component.literal(percent + "%");

        int color = chance == 0 ? 0xFF0000 : GradientUtil.toRGB(Mth.lerp(chanceFloat, 29f, 167f), 100f, 50f);
        Font fontRenderer = Minecraft.getInstance().font;
        graphics.drawString(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 23),
                (int) ((y + (height / 3f) + 6) * 2 - height), color, true);
        graphics.pose().popPose();
    }

    public void drawTick(GuiGraphics graphics, float x, float y, int width, int height) {
        graphics.pose().pushPose();
        RenderSystem.disableDepthTest();
        graphics.pose().translate(0, 0, 400);
        graphics.pose().scale(0.5f, 0.5f, 1);

        Component s = Component.translatable("gtceu.gui.content.tips.per_tick_short");

        int color = 0xFFFF00;
        Font fontRenderer = Minecraft.getInstance().font;
        graphics.drawString(fontRenderer, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 23),
                (int) ((y + (height / 3f) + 6) * 2 - height +
                        (content.chance() == ChanceLogic.getMaxChancedValue() ? 0 : 10)),
                color);
        graphics.pose().popPose();
    }
}
