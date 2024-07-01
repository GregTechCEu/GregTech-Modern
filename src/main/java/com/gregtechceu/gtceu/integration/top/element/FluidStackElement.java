package com.gregtechceu.gtceu.integration.top.element;

import com.gregtechceu.gtceu.GTCEu;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.systems.RenderSystem;
import mcjty.theoneprobe.api.IElement;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidStackElement implements IElement {

    private final FluidStack fluidStack;
    private final IFluidStyle style;

    public FluidStackElement(FluidStack fluidStack, IFluidStyle style) {
        this.fluidStack = fluidStack;
        this.style = style;
    }

    public FluidStackElement(RegistryFriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            this.fluidStack = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
        } else {
            this.fluidStack = FluidStack.EMPTY;
        }

        this.style = new FluidStyle().width(buf.readInt()).height(buf.readInt());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y) {
        RenderSystem.disableBlend();
        if (!fluidStack.isEmpty()) {
            x += 2;
            y += 2;
            int width = style.getWidth() - 4;
            int height = style.getHeight() - 4;
            DrawerHelper.drawFluidForGui(guiGraphics, fluidStack, fluidStack.getAmount(), x, y, width, height);

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

    @Override
    public int getWidth() {
        return style.getWidth();
    }

    @Override
    public int getHeight() {
        return style.getHeight();
    }

    @Override
    public void toBytes(FriendlyByteBuf friendlyByteBuf) {
        RegistryFriendlyByteBuf registryFriendlyByteBuf = new RegistryFriendlyByteBuf(friendlyByteBuf,
                GTRegistries.builtinRegistry());
        if (!fluidStack.isEmpty()) {
            friendlyByteBuf.writeBoolean(true);
            FluidStack.OPTIONAL_STREAM_CODEC.encode(registryFriendlyByteBuf, fluidStack);
        } else {
            friendlyByteBuf.writeBoolean(false);
        }

        friendlyByteBuf.writeInt(this.style.getWidth());
        friendlyByteBuf.writeInt(this.style.getHeight());
    }

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("fluid_element");
    }
}
