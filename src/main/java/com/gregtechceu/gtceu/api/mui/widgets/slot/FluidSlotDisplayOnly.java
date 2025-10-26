package com.gregtechceu.gtceu.api.mui.widgets.slot;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.FluidSlotSyncHandler;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class FluidSlotDisplayOnly extends FluidSlot {

    public static final int DEFAULT_SIZE = 18;
    public static final String UNIT_BUCKET = "B";
    public static final String UNIT_LITER = "L";
    private static final DecimalFormat TOOLTIP_FORMAT = new DecimalFormat("#.##");
    private static final IFluidTank EMPTY = new FluidTank(0);

    static {
        TOOLTIP_FORMAT.setGroupingUsed(true);
        TOOLTIP_FORMAT.setGroupingSize(3);
    }

    private final TextRenderer textRenderer = new TextRenderer();
    private FluidSlotSyncHandler syncHandler;
    private int contentOffsetX = 1, contentOffsetY = 1;
    private boolean alwaysShowFull = true;
    @Nullable
    private IDrawable overlayTexture = null;
    private boolean shouldShowAmount = true;

    public FluidSlotDisplayOnly() {
        size(DEFAULT_SIZE);
        tooltip().autoUpdate(true);// .setHasTitleMargin(true);
        tooltipBuilder(this::addToolTip);
    }

    @Override
    public void onInit() {
        super.onInit();
    }

    protected void addToolTip(RichTooltip tooltip) {
        FluidStack fluid = this.syncHandler.getValue();
        if (fluid != null) {
            tooltip.addLine(IKey.lang(fluid.getDisplayName())).spaceLine(2);
        } else {
            tooltip.addLine(IKey.lang("modularui.fluid.empty"));
        }
    }

    @Override
    public FluidSlotDisplayOnly syncHandler(FluidSlotSyncHandler syncHandler) {
        setSyncHandler(syncHandler);
        this.syncHandler = syncHandler;
        return this;
    }

    public FluidSlotDisplayOnly setShowAmount(boolean bool) {
        this.shouldShowAmount = bool;
        return this;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        IFluidTank fluidTank = getFluidTank();
        FluidStack content = this.syncHandler.getValue();
        if (content != null && !content.isEmpty()) {
            float y = this.contentOffsetY;
            float height = getArea().height - y * 2;
            if (!this.alwaysShowFull) {
                float newHeight = height * content.getAmount() * 1f / fluidTank.getCapacity();
                y += height - newHeight;
                height = newHeight;
            }
            GuiDraw.drawFluidTexture(context.getGraphics(), content,
                    this.contentOffsetX, y, getArea().width - this.contentOffsetX * 2, height, 0);
        }
        if (this.overlayTexture != null) {
            this.overlayTexture.drawAtZero(context, getArea(), getActiveWidgetTheme(widgetTheme, isHovering()));
        }
        if (content != null && this.syncHandler.controlsAmount() && this.shouldShowAmount) {
            String s = FormattingUtil.formatNumberReadable2F(content.getAmount(), true) + getBaseUnit();
            this.textRenderer.setAlignment(Alignment.CenterRight, getArea().width - this.contentOffsetX - 1f);
            this.textRenderer.setPos((int) (this.contentOffsetX + 0.5f), (int) (getArea().height - 5.5f));
            this.textRenderer.draw(context.getGraphics(), Component.literal(s));
        }
    }
}
