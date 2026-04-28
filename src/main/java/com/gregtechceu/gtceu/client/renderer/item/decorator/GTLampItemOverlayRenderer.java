package com.gregtechceu.gtceu.client.renderer.item.decorator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.LampBlockItem;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.IItemDecorator;

import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class GTLampItemOverlayRenderer implements IItemDecorator {

    public static final GTLampItemOverlayRenderer INSTANCE = new GTLampItemOverlayRenderer();

    private GTLampItemOverlayRenderer() {}

    @Override
    public boolean render(@NotNull GuiGraphicsExtractor graphics, @NotNull Font font,
                          ItemStack stack, int xPosition, int yPosition) {
        LampBlockItem.LampData lampData = stack.get(GTDataComponents.LAMP_DATA);
        if (lampData == null) {
            return false;
        }
        if (lampData.lit() && lampData.bloom()) {
            return false;
        }

        if (!lampData.bloom()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, GuiTextures.LAMP_NO_BLOOM.imageLocation,
                    xPosition, yPosition, 0, 0, 16, 16, 16, 16);
        }
        if (!lampData.lit()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, GuiTextures.LAMP_NO_LIGHT.imageLocation,
                    xPosition, yPosition, 0, 0, 16, 16, 16, 16);
        }
        return true;
    }
}
