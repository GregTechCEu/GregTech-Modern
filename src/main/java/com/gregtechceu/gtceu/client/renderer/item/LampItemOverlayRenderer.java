package com.gregtechceu.gtceu.client.renderer.item;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.LampBlockItem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;

@OnlyIn(Dist.CLIENT)
public class LampItemOverlayRenderer {

    private LampItemOverlayRenderer() {}

    public static OverlayType getOverlayType(boolean light, boolean bloom) {
        if (light) {
            return bloom ? OverlayType.NONE : OverlayType.NO_BLOOM;
        } else {
            return bloom ? OverlayType.NO_LIGHT : OverlayType.NO_BLOOM_NO_LIGHT;
        }
    }

    public static void renderOverlay(GuiGraphics graphics, LampBlockItem lamp, ItemStack stack, int xPosition,
                                     int yPosition) {
        var overlayType = getOverlayType(lamp.isLightEnabled(stack), lamp.isBloomEnabled(stack));
        if (overlayType == OverlayType.NONE) {
            return;
        }

        RenderSystem.disableDepthTest();
        if (overlayType.noBloom()) {
            GuiTextures.LAMP_NO_BLOOM.draw(graphics, 0, 0, xPosition, yPosition, 16, 16);
        }

        if (overlayType.noLight()) {
            GuiTextures.LAMP_NO_LIGHT.draw(graphics, 0, 0, xPosition, yPosition, 16, 16);
        }
        RenderSystem.enableDepthTest();
    }

    public enum OverlayType {

        NONE,
        NO_BLOOM,
        NO_LIGHT,
        NO_BLOOM_NO_LIGHT;

        public boolean noLight() {
            return this == NO_LIGHT || this == NO_BLOOM_NO_LIGHT;
        }

        public boolean noBloom() {
            return this == NO_BLOOM || this == NO_BLOOM_NO_LIGHT;
        }
    }
}
