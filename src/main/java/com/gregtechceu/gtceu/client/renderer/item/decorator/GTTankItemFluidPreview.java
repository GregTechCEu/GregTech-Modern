package com.gregtechceu.gtceu.client.renderer.item.decorator;

import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Range;

/**
 * An Item Decorator to render contained fluid icons for items with {@link Capabilities.FluidHandler#ITEM}.
 * <p>
 * The fluid type count can be up to 4, set by {@link #setMaxRenderCount(int)}, 1 by default.
 *
 * @author Taskeren
 */
public class GTTankItemFluidPreview implements IItemDecorator {

    public static final GTTankItemFluidPreview DRUM = new GTTankItemFluidPreview() {

        @Override
        public boolean render(GuiGraphics guiGraphics, Font font, ItemStack itemStack, int x, int y) {
            if (!ConfigHolder.INSTANCE.client.tankItemFluidPreview.drum) return false;
            return super.render(guiGraphics, font, itemStack, x, y);
        }
    };

    public static final GTTankItemFluidPreview QUANTUM_TANK = new GTTankItemFluidPreview() {

        @Override
        public boolean render(GuiGraphics guiGraphics, Font font, ItemStack itemStack, int x, int y) {
            if (!ConfigHolder.INSTANCE.client.tankItemFluidPreview.quantumTank) return false;
            return super.render(guiGraphics, font, itemStack, x, y);
        }
    };

    /**
     * The fluid icon draw offset to the icon top-left, it should be in order of bottom-right, bottom-left, top-right,
     * top-left.
     */
    private static final float[][] OFFSET = { { 8, 8 }, { 0, 8 }, { 8, 0 }, { 0, 0 } };

    /**
     * The maximum count of fluids to be rendered, in range from 0 (render nothing) to 4.
     */
    @Getter
    @Range(from = 0, to = 4)
    private int maxRenderCount = 1;

    /**
     * If {@code true}, the fluid icon is rendered on top of the item.
     */
    @Getter
    @Setter
    private boolean renderOnTopOfItem = true;

    @Getter
    @Setter
    private boolean requireShiftKeyDown = false;

    public void setMaxRenderCount(int maxRenderCount) {
        if (maxRenderCount < 0 || maxRenderCount > 4) {
            throw new IllegalArgumentException("maxRenderCount must be between 0 and 4");
        }
        this.maxRenderCount = maxRenderCount;
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack itemStack, int x, int y) {
        if (isRequireShiftKeyDown() && !Screen.hasShiftDown()) {
            return false;
        }

        IFluidHandlerItem fluidHandler = itemStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler == null) {
            return false;
        }

        if (isRenderOnTopOfItem()) {
            RenderSystem.disableDepthTest();
        }

        for (int index = 0, renderedCount = 0; index < fluidHandler.getTanks() &&
                renderedCount < getMaxRenderCount(); index++) {
            FluidStack fluidInTank = fluidHandler.getFluidInTank(index);
            if (fluidInTank.isEmpty()) {
                continue;
            }
            DrawerHelper.drawFluidForGui(guiGraphics, fluidInTank,
                    x + OFFSET[renderedCount][0], y + OFFSET[renderedCount][1], 8.0F, 8.0F);
            renderedCount++;
        }

        return true;
    }
}
