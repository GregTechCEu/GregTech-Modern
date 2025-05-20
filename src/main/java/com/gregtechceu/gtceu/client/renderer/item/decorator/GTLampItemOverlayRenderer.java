package com.gregtechceu.gtceu.client.renderer.item.decorator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemDecorator;

import com.mojang.blaze3d.systems.RenderSystem;

import static com.gregtechceu.gtceu.common.block.LampBlock.isBloomEnabled;
import static com.gregtechceu.gtceu.common.block.LampBlock.isLightEnabled;

@OnlyIn(Dist.CLIENT)
public class GTLampItemOverlayRenderer implements IItemDecorator {

    public static final GTLampItemOverlayRenderer INSTANCE = new GTLampItemOverlayRenderer();

    private GTLampItemOverlayRenderer() {}

    public static OverlayType getOverlayType(boolean light, boolean bloom) {
        if (light) {
            return bloom ? OverlayType.NONE : OverlayType.NO_BLOOM;
        } else {
            return bloom ? OverlayType.NO_LIGHT : OverlayType.NO_BLOOM_NO_LIGHT;
        }
    }

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int xPosition, int yPosition) {
        if (stack.hasTag()) {
            var tag = stack.getOrCreateTag();
            var overlayType = getOverlayType(isLightEnabled(tag), isBloomEnabled(tag));
            if (overlayType == OverlayType.NONE) {
                return true;
            }

            RenderSystem.disableDepthTest();
            if (overlayType.noBloom()) {
                GuiTextures.LAMP_NO_BLOOM.draw(graphics, 0, 0, xPosition, yPosition, 16, 16);
            }

            if (overlayType.noLight()) {
                GuiTextures.LAMP_NO_LIGHT.draw(graphics, 0, 0, xPosition, yPosition, 16, 16);
            }
            RenderSystem.enableDepthTest();
            return true;
        }
        return false;
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
