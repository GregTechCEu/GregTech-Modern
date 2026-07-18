package com.gregtechceu.gtceu.integration.ae2.gui;

import com.gregtechceu.gtceu.integration.ae2.utils.AEUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class AEGuiHelper {

    private AEGuiHelper() {}

    public static void drawFluid(GuiGraphics graphics, FluidStack fluid, int x, int y, int width, int height) {
        if (fluid.isEmpty()) return;
        var renderProps = IClientFluidTypeExtensions.of(fluid.getFluid());
        var stillTexture = renderProps.getStillTexture(fluid);
        if (stillTexture == null) return;
        int color = renderProps.getTintColor(fluid);
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(stillTexture);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;
        if (a == 0) a = 1f;
        graphics.setColor(r, g, b, a);
        graphics.blit(x, y, 0, width, height, sprite);
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    public static void drawFluid(GuiGraphics graphics, GenericStack stack, int x, int y) {
        if (stack.what() instanceof AEFluidKey fluidKey) {
            drawFluid(graphics, AEUtil.toFluidStack(fluidKey, 1), x, y, 16, 16);
        }
    }

    public static void drawAmountOverlay(GuiGraphics graphics, long amount, int x, int y) {
        String text = formatAmount(amount);
        var font = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);
        graphics.pose().scale(0.5f, 0.5f, 1f);
        int textX = (x + 16) * 2 - font.width(text);
        int textY = (y + 16) * 2 - font.lineHeight;
        graphics.drawString(font, text, textX, textY, 0xFFFFFF, true);
        graphics.pose().popPose();
    }

    public static void drawSelectionOverlay(GuiGraphics graphics, int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        graphics.fill(x, y, x + width, y + height, 0x80FFFFFF);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    public static String formatAmountFull(long amount) {
        return String.format("%,d", amount);
    }

    public static String formatAmount(long amount) {
        if (amount >= 1_000_000_000) {
            return String.format("%.1fG", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000.0);
        } else if (amount >= 10_000) {
            return String.format("%.1fK", amount / 1_000.0);
        }
        return String.valueOf(amount);
    }

    public static long parseAmount(String text) {
        if (text == null || text.isBlank()) return -1;
        try {
            long result = Math.max(1, (long) evaluateExpression(text.trim().toLowerCase()));
            return Math.min(result, Integer.MAX_VALUE);
        } catch (Exception e) {
            return -1;
        }
    }

    private static double evaluateExpression(String expr) {
        expr = expr.replaceAll("\\s+", "");
        List<Double> terms = new java.util.ArrayList<>();
        List<Character> ops = new java.util.ArrayList<>();
        int start = 0;
        for (int i = 0; i <= expr.length(); i++) {
            if (i == expr.length() || ((expr.charAt(i) == '+' || expr.charAt(i) == '-') && i > start)) {
                String token = expr.substring(start, i);
                if (!token.isEmpty()) {
                    terms.add(evaluateTerm(token));
                    if (i < expr.length()) {
                        ops.add(expr.charAt(i));
                        start = i + 1;
                    }
                } else {
                    start = i;
                }
            }
        }
        double result = terms.isEmpty() ? 0 : terms.get(0);
        for (int i = 0; i < ops.size(); i++) {
            result = ops.get(i) == '+' ? result + terms.get(i + 1) : result - terms.get(i + 1);
        }
        return result;
    }

    private static double evaluateTerm(String term) {
        List<Double> factors = new java.util.ArrayList<>();
        List<Character> ops = new java.util.ArrayList<>();
        int start = 0;
        for (int i = 0; i <= term.length(); i++) {
            if (i == term.length() || term.charAt(i) == '*' || term.charAt(i) == '/') {
                if (i > start) {
                    factors.add(parseToken(term.substring(start, i)));
                }
                if (i < term.length()) {
                    ops.add(term.charAt(i));
                }
                start = i + 1;
            }
        }
        double result = factors.isEmpty() ? 0 : factors.get(0);
        for (int i = 0; i < ops.size(); i++) {
            result = ops.get(i) == '*' ? result * factors.get(i + 1) : result / factors.get(i + 1);
        }
        return result;
    }

    private static double parseToken(String token) {
        token = token.trim();
        long multiplier = 1;
        if (token.endsWith("k")) {
            multiplier = 1_000;
            token = token.substring(0, token.length() - 1);
        } else if (token.endsWith("m")) {
            multiplier = 1_000_000;
            token = token.substring(0, token.length() - 1);
        } else if (token.endsWith("g") || token.endsWith("b")) {
            multiplier = 1_000_000_000;
            token = token.substring(0, token.length() - 1);
        }
        return Double.parseDouble(token) * multiplier;
    }
}
