package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

public class FluidDrawable implements IDrawable {

    private FluidStack fluid = null;

    public FluidDrawable() {}

    /**
     * Takes a fluid stack, it can be null but will not draw anything
     *
     * @param fluid - fluid stack to draw
     */
    public FluidDrawable(@NotNull FluidStack fluid) {
        setFluid(fluid);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiDraw.drawFluidTexture(context.getGraphics(), fluid, x, y, width, height, context.getCurrentDrawingZ());
    }

    @Override
    public int getDefaultWidth() {
        return 16;
    }

    @Override
    public int getDefaultHeight() {
        return 16;
    }

    @Override
    public Widget<?> asWidget() {
        return IDrawable.super.asWidget().size(16);
    }

    public FluidDrawable setFluid(FluidStack fluid) {
        this.fluid = fluid;
        return this;
    }
}
