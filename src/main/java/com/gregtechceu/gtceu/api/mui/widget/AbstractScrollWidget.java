package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.api.mui.base.layout.IViewport;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiAction;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.drawable.Stencil;
import com.gregtechceu.gtceu.api.mui.utils.HoveredWidgetList;
import com.gregtechceu.gtceu.api.mui.widget.scroll.HorizontalScrollData;
import com.gregtechceu.gtceu.api.mui.widget.scroll.ScrollArea;
import com.gregtechceu.gtceu.api.mui.widget.scroll.VerticalScrollData;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A scrollable parent widget. Children can be added
 *
 * @param <I> type of children (in most cases just {@link IWidget})
 * @param <W> type of this widget
 */
public abstract class AbstractScrollWidget<I extends IWidget, W extends AbstractScrollWidget<I, W>>
                                          extends AbstractParentWidget<I, W> implements IViewport, Interactable {

    private final ScrollArea scroll = new ScrollArea();
    private boolean keepScrollBarInArea = false;

    public AbstractScrollWidget(@Nullable HorizontalScrollData x, @Nullable VerticalScrollData y) {
        super();
        this.scroll.setScrollX(x);
        this.scroll.setScrollY(y);
        listenGuiAction((IGuiAction.MouseReleased) (mouseX, mouseY, button) -> {
            this.scroll.mouseReleased(getContext());
            return false;
        });
    }

    @Override
    public Area getArea() {
        return this.scroll;
    }

    public ScrollArea getScrollArea() {
        return this.scroll;
    }

    @Override
    public void transformChildren(IViewportStack stack) {
        stack.translate(-getScrollX(), -getScrollY());
    }

    @Override
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (isInside(stack, x, y)) {
            widgets.add(this, stack.peek());
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (getArea().isInside(x, y) && !getScrollArea().isInsideScrollbarArea(x, y) && hasChildren()) {
            IViewport.getChildrenAt(this, stack, widgets, x, y);
        }
    }

    @Override
    public void onResized() {
        if (this.scroll.getScrollX() != null) {
            this.scroll.getScrollX().clamp(this.scroll);
            if (!this.keepScrollBarInArea) {
                getArea().height += this.scroll.getScrollX().getThickness();
            }
        }
        if (this.scroll.getScrollY() != null) {
            this.scroll.getScrollY().clamp(this.scroll);
            if (!this.keepScrollBarInArea) {
                getArea().width += this.scroll.getScrollY().getThickness();
            }
        }
    }

    @Override
    public boolean canHover() {
        return super.canHover() ||
                this.scroll.isInsideScrollbarArea(getContext().getMouseX(), getContext().getMouseY());
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        ModularGuiContext context = getContext();
        if (this.scroll.mouseClicked(context)) {
            return Result.STOP;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        return this.scroll.mouseScroll(getContext());
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        this.scroll.mouseReleased(getContext());
        return false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.scroll.drag(getContext().getMouseX(), getContext().getMouseY());
    }

    @Override
    public void preDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.applyAtZero(this.scroll, context);
        }
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.remove();
            this.scroll.drawScrollbar(context);
        }
    }

    public int getScrollX() {
        return this.scroll.getScrollX() != null ? this.scroll.getScrollX().getScroll() : 0;
    }

    public int getScrollY() {
        return this.scroll.getScrollY() != null ? this.scroll.getScrollY().getScroll() : 0;
    }

    /**
     * Sets whether the scroll bar should be kept inside the area of this widget, which might cause it to overlap with
     * the content of this widget.
     * By setting the value to false, the size of this widget is expanded by the thickness of the scrollbars after the
     * tree is resized.
     * Default: false
     *
     * @param value if the scroll bar should be kept inside the widgets area
     * @return this
     */
    public W keepScrollBarInArea(boolean value) {
        this.keepScrollBarInArea = value;
        return getThis();
    }

    public W keepScrollBarInArea() {
        return keepScrollBarInArea(true);
    }
}
