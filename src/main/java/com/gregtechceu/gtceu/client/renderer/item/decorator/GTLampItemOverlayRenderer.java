package com.gregtechceu.gtceu.client.renderer.item.decorator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.LampBlockItem;
import com.gregtechceu.gtceu.data.item.GTDataComponents;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.IItemDecorator;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class GTLampItemOverlayRenderer implements IItemDecorator {

    public static final GTLampItemOverlayRenderer INSTANCE = new GTLampItemOverlayRenderer();

    private GTLampItemOverlayRenderer() {}

    @Override
    public boolean render(@NotNull GuiGraphics graphics, @NotNull Font font,
                          ItemStack stack, int xPosition, int yPosition) {
        LampBlockItem.LampData lampData = stack.get(GTDataComponents.LAMP_DATA);
        if (lampData == null) {
            return false;
        }
        if (lampData.lit() && lampData.bloom()) {
            return false;
        }

        RenderSystem.disableDepthTest();
        if (!lampData.bloom()) {
            GuiTextures.LAMP_NO_BLOOM.draw(graphics, 0, 0, xPosition, yPosition, 16, 16);
        }
        if (!lampData.lit()) {
            GuiTextures.LAMP_NO_LIGHT.draw(graphics, 0, 0, xPosition, yPosition, 16, 16);
        }
        return true;
    }
}
