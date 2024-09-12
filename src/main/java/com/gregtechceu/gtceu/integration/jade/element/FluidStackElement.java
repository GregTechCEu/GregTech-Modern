package com.gregtechceu.gtceu.integration.jade.element;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;

public class FluidStackElement extends Element {

    private final FluidStack fluidStack;
    private float width, height;

    public FluidStackElement(FluidStack fluidStack, float width, float height) {
        this.fluidStack = fluidStack;
        this.width = width;
        this.height = height;
    }

    @Override
    public Vec2 getSize() {
        return new Vec2(width, height);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float width, float height) {
        RenderSystem.disableBlend();
        if (!fluidStack.isEmpty()) {
            x += 2;
            y += 2;
            DrawerHelper.drawFluidForGui(guiGraphics, fluidStack, fluidStack.getAmount(),
                    (int) x, (int) y, (int) width, (int) height);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.5F, 0.5F, 1);
            String s = TextFormattingUtil.formatLongToCompactStringBuckets(fluidStack.getAmount(), 3) + "B";
            Font fontRenderer = Minecraft.getInstance().font;
            guiGraphics.drawString(fontRenderer, s, (x + (width / 3f)) * 2 - fontRenderer.width(s) + 21,
                    (y + (height / 3f) + 6) * 2, 0xFFFFFF, true);
            guiGraphics.pose().popPose();
        }
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);

    }
}
