package com.gregtechceu.gtceu.api.mui.widget.scroll;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Box;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Scrollable area
 * <p>
 * This class is responsible for storing information for scrollable one
 * directional objects.
 */
@Accessors(chain = true)
public class ScrollArea extends Area {

    @Getter
    @Setter
    private HorizontalScrollData scrollX;
    @Getter
    @Setter
    private VerticalScrollData scrollY;
    private final ScrollPadding scrollPadding = new ScrollPadding();
    @Getter
    @Setter
    private int scrollBarBackgroundColor = Color.withAlpha(Color.BLACK.main, 0.25f);

    public ScrollArea(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public ScrollArea() {}

    @Override

    public Box getPadding() {
        return this.scrollPadding;
    }

    public ScrollPadding getScrollPadding() {
        return this.scrollPadding;
    }

    public void setScrollData(ScrollData data) {
        if (data instanceof HorizontalScrollData scrollData) {
            this.scrollX = scrollData;
        } else if (data instanceof VerticalScrollData scrollData) {
            this.scrollY = scrollData;
        }
    }

    public void removeScrollData() {
        this.scrollX = null;
        this.scrollY = null;
    }

    public ScrollData getScrollData(GuiAxis axis) {
        return axis.isVertical() ? this.scrollY : this.scrollX;
    }

    /* GUI code for easier manipulations */

    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(GuiContext context) {
        return this.mouseClicked(context.getMouseX(), context.getMouseY());
    }

    /**
     * This method should be invoked to register dragging
     */
    public boolean mouseClicked(int x, int y) {
        if (this.scrollX != null && this.scrollX.isInsideScrollbarArea(this, x, y)) {
            return this.scrollX.onMouseClicked(this, x, y, 0);
        } else if (this.scrollY != null && this.scrollY.isInsideScrollbarArea(this, x, y)) {
            return this.scrollY.onMouseClicked(this, y, x, 0);
        } else {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean mouseScroll(GuiContext context) {
        return this.mouseScroll(context.getMouseX(), context.getMouseY(), context.getMouseScrollDelta(),
                Screen.hasShiftDown());
    }

    /**
     * This method should be invoked when mouse wheel is scrolling
     */
    public boolean mouseScroll(int x, int y, double scroll, boolean shift) {
        ScrollData data;
        if (this.scrollX != null) {
            data = this.scrollY == null || shift ? this.scrollX : this.scrollY;
        } else if (this.scrollY != null) {
            data = this.scrollY;
        } else {
            // no scroll data present -> cant be scrolled
            return false;
        }

        int scrollAmount = (int) Math.copySign(data.getScrollSpeed(), scroll);
        int scrollTo;
        if (data.isAnimating()) {
            scrollTo = data.getAnimatingTo() - scrollAmount;
        } else {
            scrollTo = data.getScroll() - scrollAmount;
        }

        // simulate scroll to determine whether event should be canceled
        int oldScroll = data.getScroll();
        data.scrollTo(this, scrollTo);
        boolean changed = data.getScroll() != oldScroll;
        data.scrollTo(this, oldScroll);
        if (changed) {
            data.animateTo(this, scrollTo);
            return true;
        }
        return data.isCancelScrollEdge();
    }

    @OnlyIn(Dist.CLIENT)
    public void mouseReleased(GuiContext context) {
        this.mouseReleased(context.getMouseX(), context.getMouseY());
    }

    /**
     * When mouse button gets released
     */
    public void mouseReleased(int x, int y) {
        if (this.scrollX != null) {
            this.scrollX.dragging = false;
            this.scrollX.clickOffset = 0;
        }
        if (this.scrollY != null) {
            this.scrollY.dragging = false;
            this.scrollY.clickOffset = 0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void drag(GuiContext context) {
        this.drag(context.getMouseX(), context.getMouseY());
    }

    /**
     * This should be invoked in a drawing or and update method. It's
     * responsible for scrolling through this view when dragging.
     */
    public void drag(int x, int y) {
        ScrollData data;
        float progress;
        if (this.scrollX != null && this.scrollX.dragging) {
            data = this.scrollX;
            progress = data.getProgress(this, x, y);
        } else if (this.scrollY != null && this.scrollY.dragging) {
            data = this.scrollY;
            progress = data.getProgress(this, y, x);
        } else {
            return;
        }
        progress = Mth.clamp(progress, 0f, 1f);
        data.scrollTo(this,
                (int) (progress * (data.getScrollSize() - data.getFullVisibleSize(this) + data.getThickness())));
    }

    public boolean isInsideScrollbarArea(int x, int y) {
        if (!isInside(x, y)) {
            return false;
        }
        if (this.scrollX != null && this.scrollX.isInsideScrollbarArea(this, x, y)) {
            return true;
        }
        return this.scrollY != null && this.scrollY.isInsideScrollbarArea(this, x, y);
    }

    public boolean isScrollBarXActive() {
        return this.scrollX != null && this.scrollX.isScrollBarActive(this);
    }

    public boolean isScrollBarYActive() {
        return this.scrollY != null && this.scrollY.isScrollBarActive(this);
    }

    public boolean isDragging() {
        return (this.scrollX != null && this.scrollX.isDragging()) ||
                (this.scrollY != null && this.scrollY.isDragging());
    }

    public void applyWidgetTheme(WidgetTheme widgetTheme) {
        if (this.scrollX != null) this.scrollX.applyWidgetTheme(widgetTheme);
        if (this.scrollY != null) this.scrollY.applyWidgetTheme(widgetTheme);
    }

    /**
     * This method is responsible for drawing a scroll bar
     */
    @OnlyIn(Dist.CLIENT)
    public void drawScrollbar(GuiContext context, WidgetTheme widgetTheme, IDrawable texture) {
        boolean isXActive = false; // micro optimisation
        if (this.scrollX != null && this.scrollX.isScrollBarActive(this, false)) {
            isXActive = true;
            this.scrollX.drawScrollbar(context, this, widgetTheme, texture);
        }
        if (this.scrollY != null && this.scrollY.isScrollBarActive(this, isXActive)) {
            this.scrollY.drawScrollbar(context, this, widgetTheme, texture);
        }
    }
}
