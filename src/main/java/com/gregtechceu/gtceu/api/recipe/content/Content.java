package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class Content {
    @Getter
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

    public Content copy(RecipeCapability<?> capability, @Nullable ContentModifier modifier) {
        if (modifier == null || chance == 0) {
            return new Content(capability.copyContent(content), chance, tierChanceBoost, slotName, uiName);
        } else {
            return new Content(capability.copyContent(content, modifier), chance, tierChanceBoost, slotName, uiName);
        }
    }

    public IGuiTexture createOverlay(boolean perTick) {
        return new IGuiTexture() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public void draw(PoseStack stack, int mouseX, int mouseY, float x, float y, int width, int height) {
                drawChance(stack, x, y, width, height);
                if (LDLib.isEmiLoaded()) {
                    drawEmiAmount(stack, x, y, width, height);
                }
                if (perTick) {
                    drawTick(stack, x, y, width, height);
                }
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    public void drawEmiAmount(PoseStack stack, float x, float y, int width, int height) {
        if (content instanceof FluidIngredient ingredient) {
            stack.pushPose();
            stack.translate(0, 0, 400);
            stack.scale(0.5f, 0.5f, 1);
            long amount = ingredient.getAmount();
            String s;
            if (amount >= 1000) {
                amount /= 1000;
                s = amount + "B";
            } else {
                s = amount + "mB";
            }
            Font fontRenderer = Minecraft.getInstance().font;
            fontRenderer.drawShadow(stack, s, (int) ((x + (width / 3f)) * 2 - fontRenderer.width(s) + 21), (int) ((y + (height / 3f) + 6) * 2), 0xFFFFFF);
            stack.popPose();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void drawChance(PoseStack matrixStack, float x, float y, int width, int height) {
        if (chance == 1) return;
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 400);
        matrixStack.scale(0.5f, 0.5f, 1);
        String s = chance == 0 ? LocalizationUtils.format("gtceu.gui.content.chance_0_short") : String.format("%.2f", chance * 100) + "%";
        int color = chance == 0 ? 0xff0000 : 0xFFFF00;
        Font fontRenderer = Minecraft.getInstance().font;
        fontRenderer.drawShadow(matrixStack, s, (x + (width / 3f)) * 2 - fontRenderer.width(s) + 23, (y + (height / 3f) + 6) * 2 - height, color);
        matrixStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
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
