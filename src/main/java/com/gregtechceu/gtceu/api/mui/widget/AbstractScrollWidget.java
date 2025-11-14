package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.api.mui.base.layout.IViewport;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiAction;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.utils.HoveredWidgetList;
import com.gregtechceu.gtceu.api.mui.widget.scroll.HorizontalScrollData;
import com.gregtechceu.gtceu.api.mui.widget.scroll.ScrollArea;
import com.gregtechceu.gtceu.api.mui.widget.scroll.ScrollData;
import com.gregtechceu.gtceu.api.mui.widget.scroll.VerticalScrollData;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A scrollable parent widget. Children can be added.
 *
 * @param <I> type of children (in most cases just {@link IWidget})
 * @param <W> type of this widget
 */
public abstract class AbstractScrollWidget<I extends IWidget, W extends AbstractScrollWidget<I, W>>
                                          extends AbstractParentWidget<I, W> implements IViewport, Interactable {

    private final ScrollArea scroll = new ScrollArea();
    private boolean scrollXActive, scrollYActive;

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
            widgets.add(this, stack.peek(), getAdditionalHoverInfo(stack, x, y));
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (getArea().isInside(x, y) && !getScrollArea().isInsideScrollbarArea(x, y) && hasChildren()) {
            IViewport.getChildrenAt(this, stack, widgets, x, y);
        }
    }

    public void beforeResize(boolean onOpen) {
        super.beforeResize(onOpen);
        this.scroll.applyWidgetTheme(getContext().getTheme().getScrollbarTheme().getTheme(isHovering()));
        if (onOpen) checkScrollbarActive(true);
        getScrollArea().getScrollPadding().scrollPaddingAll(0);
        applyAdditionalOffset(this.scroll.getScrollX());
        applyAdditionalOffset(this.scroll.getScrollY());
    }

    private void checkScrollbarActive(boolean onOpen) {
        boolean scrollYActive = this.scroll.getScrollY() != null &&
                this.scroll.getScrollY().isScrollBarActive(getScrollArea());
        boolean scrollXActive = this.scroll.getScrollX() != null &&
                this.scroll.getScrollX().isScrollBarActive(getScrollArea(), this.scrollYActive);
        if (!onOpen && (scrollYActive != this.scrollYActive || scrollXActive != this.scrollXActive)) {
            scheduleResize();
        }
        this.scrollXActive = scrollXActive;
        this.scrollYActive = scrollYActive;
    }

    private void applyAdditionalOffset(ScrollData data) {
        if (data != null && data.isScrollBarActive(getScrollArea())) {
            getScrollArea().getScrollPadding().scrollPadding(data.getAxis().getOther(), data.isAxisStart(),
                    data.getThickness());
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
            return Result.SUCCESS;
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
    public void onMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        checkScrollbarActive(false);
        this.scroll.drag(getContext().getMouseX(), getContext().getMouseY());
    }

    @Override
    public void preDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            context.getStencil().pushAtZero(this.scroll);
        }
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            context.getStencil().pop();
            WidgetThemeEntry<WidgetTheme> scrollbarTheme = context.getTheme().getScrollbarTheme();
            this.scroll.drawScrollbar(context, scrollbarTheme.getTheme(isHovering()),
                    scrollbarTheme.getTheme().getBackground());
        }
    }

    public int getScrollX() {
        return this.scroll.getScrollX() != null ? this.scroll.getScrollX().getScroll() : 0;
    }

    public int getScrollY() {
        return this.scroll.getScrollY() != null ? this.scroll.getScrollY().getScroll() : 0;
    }
}
