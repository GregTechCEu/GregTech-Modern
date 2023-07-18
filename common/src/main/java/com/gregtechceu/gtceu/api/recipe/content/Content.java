package com.gregtechceu.gtceu.api.recipe.content;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import javax.annotation.Nullable;

public class Content {
    public Object content;
    public float chance;
    public float tierChanceBoost;
    @Nullable
    public String slotName;
    @Nullable
    public String uiName;

    public Content(Object content, float chance, float tierChanceBoost, @Nullable String slotName, @Nullable String uiName) {
        this.content = content;
        this.chance = chance;
        this.tierChanceBoost = tierChanceBoost;
        this.slotName = slotName;
        this.uiName = uiName;
    }

    public Object getContent() {
        return content;
    }

    public IGuiTexture createOverlay(boolean perTick) {
        return new IGuiTexture() {
            @Override
            @Environment(EnvType.CLIENT)
            public void draw(PoseStack stack, int mouseX, int mouseY, float x, float y, int width, int height) {
                drawChance(stack, x, y, width, height);
                if (perTick) {
                    drawTick(stack, x, y, width, height);
                }
            }
        };
    }

    @Environment(EnvType.CLIENT)
    public void drawChance(PoseStack matrixStack, float x, float y, int width, int height) {
        if (chance == 1) return;
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 400);
        matrixStack.scale(0.5f, 0.5f, 1);
        String s = chance == 0 ? LocalizationUtils.format("gtceu.gui.content.chance_0_short") : String.format("%.1f", chance * 100) + "%";
        int color = chance == 0 ? 0xff0000 : 0xFFFF00;
        Font fontRenderer = Minecraft.getInstance().font;
        fontRenderer.drawShadow(matrixStack, s, (x + (width / 3f)) * 2 - fontRenderer.width(s) + 23, (y + (height / 3f) + 6) * 2 - height, color);
        matrixStack.popPose();
    }

    @Environment(EnvType.CLIENT)
    public void drawTick(PoseStack matrixStack, float x, float y, int width, int height) {
        matrixStack.pushPose();
        RenderSystem.disableDepthTest();
        matrixStack.translate(0, 0, 400);
        matrixStack.scale(0.5f, 0.5f, 1);
        String s = LocalizationUtils.format("gtceu.gui.content.tips.per_tick_short");
        int color = 0xFFFF00;
        Font fontRenderer = Minecraft.getInstance().font;
        fontRenderer.drawShadow(matrixStack, s, (x + (width / 3f)) * 2 - fontRenderer.width(s) + 23, (y + (height / 3f) + 6) * 2 - height + (chance == 1 ? 0 : 10), color);
        matrixStack.popPose();
    }
}
