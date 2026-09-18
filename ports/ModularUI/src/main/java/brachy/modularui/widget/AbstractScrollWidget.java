package brachy.modularui.widget;

import brachy.modularui.api.layout.IViewport;
import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.api.widget.IGuiAction;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.HoveredWidgetList;
import brachy.modularui.widget.scroll.HorizontalScrollData;
import brachy.modularui.widget.scroll.ScrollArea;
import brachy.modularui.widget.scroll.ScrollData;
import brachy.modularui.widget.scroll.VerticalScrollData;
import brachy.modularui.widget.sizer.Area;

import lombok.Getter;
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

    @Getter private boolean showScrollShadows = true;

    public AbstractScrollWidget(@Nullable HorizontalScrollData x, @Nullable VerticalScrollData y) {
        super();
        this.scroll.setScrollX(x);
        this.scroll.setScrollY(y);
        listenGuiAction((IGuiAction.MouseReleased) (context, button) -> {
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
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        // if 'widgets.peek() == this' is true, only then this widget is hovered
        // we should require this since a stencil is applied to this widget
        if (widgets.peek() == this && !getScrollArea().isInsideScrollbarArea(x, y)) {
            IViewport.super.getWidgetsAt(stack, widgets, x, y);
        }
    }

    public void beforeResize(boolean onOpen) {
        super.beforeResize(onOpen);
        this.scroll.applyWidgetTheme(getPanel().getTheme().getScrollbarTheme().getTheme(isHovering()));
        checkScrollbarActive(false);
        getScrollArea().getScrollPadding().scrollPaddingAll(0);
        applyAdditionalOffset(this.scroll.getScrollX());
        applyAdditionalOffset(this.scroll.getScrollY());
    }

    protected void checkScrollbarActive(boolean resizeOnChange) {
        boolean scrollYActive = this.scroll.getScrollY() != null && this.scroll.getScrollY().isScrollBarActive(getScrollArea());
        boolean scrollXActive = this.scroll.getScrollX() != null && this.scroll.getScrollX().isScrollBarActive(getScrollArea(), scrollYActive);
        if (resizeOnChange && (scrollYActive != this.scrollYActive || scrollXActive != this.scrollXActive)) {
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
    public @NotNull Result onMousePressed(int button) {
        ModularGuiContext context = getContext();
        if (this.scroll.mouseClicked(context)) {
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY) {
        return this.scroll.mouseScroll(getContext());
    }

    @Override
    public boolean onMouseReleased(int button) {
        this.scroll.mouseReleased(getContext());
        return false;
    }

    @Override
    public void onMouseDrag(int button, double dragX, double dragY) {
        checkScrollbarActive(false);
        this.scroll.drag(getContext().getMouseX(), getContext().getMouseY());
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        checkScrollbarActive(true);
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
            WidgetThemeEntry<WidgetTheme> scrollbarTheme = getPanel().getTheme().getScrollbarTheme();
            this.scroll.drawScrollbar(context, scrollbarTheme.getTheme(isHovering()), scrollbarTheme.theme().getBackground());
            if (this.showScrollShadows) this.scroll.drawScrollShadow(context);
        }
    }

    public int getScrollX() {
        return this.scroll.getScrollX() != null ? this.scroll.getScrollX().getScroll() : 0;
    }

    public int getScrollY() {
        return this.scroll.getScrollY() != null ? this.scroll.getScrollY().getScroll() : 0;
    }

    public W showScrollShadows(boolean showScrollShadows) {
        this.showScrollShadows = showScrollShadows;
        return getThis();
    }
}
