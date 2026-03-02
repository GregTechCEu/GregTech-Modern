package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.api.mui.base.layout.IViewport;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.base.widget.IDraggable;
import com.gregtechceu.gtceu.api.mui.utils.HoveredWidgetList;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraft.client.gui.GuiGraphics;

import lombok.Getter;

/**
 * A widget that can be picked up by the cursor.
 * Might not work as expected when a parent is scaling or rotating itself.
 */
public class DraggableWidget<W extends DraggableWidget<W>> extends Widget<W> implements IDraggable, IViewport {

    @Getter
    private boolean moving = false;
    private int relativeClickX, relativeClickY;
    @Getter
    private final Area movingArea;
    private int realX, realY;

    public DraggableWidget() {
        this.movingArea = getArea().createCopy();
    }

    @Override
    public void drawMovingState(GuiGraphics graphics, ModularGuiContext context, float partialTicks) {
        WidgetTree.drawTree(this, context, true, true);
    }

    @Override
    public boolean onDragStart(int mouseButton) {
        if (mouseButton == 0) {
            this.realX = getContext().transformX(0, 0) - getParentArea().x;
            this.realY = getContext().transformY(0, 0) - getParentArea().y;
            this.movingArea.x = this.realX;
            this.movingArea.y = this.realY;
            this.relativeClickX = getContext().getAbsMouseX() - this.realX;
            this.relativeClickY = getContext().getAbsMouseY() - this.realY;
            return true;
        }
        return false;
    }

    @Override
    public void onDragEnd(boolean successful) {
        if (successful) {
            resizer().top(getContext().getAbsMouseY() - this.relativeClickY)
                    .left(getContext().getAbsMouseX() - this.relativeClickX);
            this.movingArea.x = getArea().x;
            this.movingArea.y = getArea().y;
            scheduleResize();
        }
    }

    @Override
    public void onDrag(int mouseButton, double timeSinceLastClick) {
        this.movingArea.x = getContext().getAbsMouseX() - this.relativeClickX;
        this.movingArea.y = getContext().getAbsMouseY() - this.relativeClickY;
    }

    @Override
    public void setMoving(boolean moving) {
        this.moving = moving;
        setEnabled(!moving);
    }

    @Override
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (!isMoving() && isInside(stack, x, y)) {
            widgets.add(this, stack, getAdditionalHoverInfo(stack, x, y));
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (!isMoving() && hasChildren()) {
            IViewport.getChildrenAt(this, stack, widgets, x, y);
        }
    }

    @Override
    public void transform(IViewportStack stack) {
        super.transform(stack);
        if (isMoving()) {
            // remove relative transformation
            stack.translate(-getArea().rx, -getArea().ry);
            // translate to current pos
            stack.translate(-this.realX, -this.realY);
            stack.translate(this.movingArea.x, this.movingArea.y);
        }
    }
}
