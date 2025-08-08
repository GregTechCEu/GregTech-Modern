package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.base.layout.ILayoutWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IParentWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.widget.AbstractScrollWidget;
import com.gregtechceu.gtceu.api.mui.widget.scroll.ScrollData;
import com.gregtechceu.gtceu.api.mui.widget.scroll.VerticalScrollData;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;

import java.util.function.IntFunction;

/**
 * A widget which can hold any amount of children.
 *
 * @param <I> type of children (in most cases just {@link IWidget})
 * @param <W> type of this widget
 */
public class ListWidget<I extends IWidget, W extends ListWidget<I, W>> extends AbstractScrollWidget<I, W>
                       implements ILayoutWidget, IParentWidget<I, W> {

    @Getter
    private ScrollData scrollData;
    private IIcon childSeparator;
    private final IntList separatorPositions = new IntArrayList();
    private boolean collapseDisabledChild = false;

    public ListWidget() {
        super(null, null);
    }

    @Override
    public void onInit() {
        if (this.scrollData == null) {
            scrollDirection(new VerticalScrollData());
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (this.childSeparator == null || this.separatorPositions.isEmpty()) return;
        GuiAxis axis = this.scrollData.getAxis();
        int x = getArea().getPadding().left, y = getArea().getPadding().top, w, h;
        if (axis.isHorizontal()) {
            w = this.childSeparator.getWidth();
            h = getArea().h() - getArea().getPadding().vertical();
        } else {
            w = getArea().w() - getArea().getPadding().horizontal();
            h = this.childSeparator.getHeight();
        }
        for (int p : this.separatorPositions) {
            if (axis.isHorizontal()) {
                x = p;
            } else {
                y = p;
            }
            this.childSeparator.draw(context, x, y, w, h, widgetTheme);
        }
    }

    @Override
    public void layoutWidgets() {
        this.separatorPositions.clear();
        GuiAxis axis = this.scrollData.getAxis();
        int separatorSize = getSeparatorSize();
        int p = getArea().getPadding().getStart(axis);
        for (IWidget widget : getChildren()) {
            if (shouldIgnoreChildSize(widget)) continue;
            if (axis.isVertical() ?
                    widget.getFlex().hasYPos() || !widget.resizer().isHeightCalculated() :
                    widget.getFlex().hasXPos() || !widget.resizer().isWidthCalculated()) {
                continue;
            }
            p += widget.getArea().getMargin().getStart(axis);
            widget.getArea().setRelativePoint(axis, p);
            p += widget.getArea().getSize(axis) + widget.getArea().getMargin().getEnd(axis);
            if (axis.isHorizontal()) {
                widget.resizer().setXResized(true);
            } else {
                widget.resizer().setYResized(true);
            }
            this.separatorPositions.add(p);
            p += separatorSize;
        }
        getScrollData().setScrollSize(p + getArea().getPadding().getEnd(axis));
    }

    @Override
    public boolean shouldIgnoreChildSize(IWidget child) {
        return this.collapseDisabledChild && !child.isEnabled();
    }

    @Override
    public boolean addChild(I child, int index) {
        return super.addChild(child, index);
    }

    @Override
    public void onChildAdd(I child) {
        super.onChildAdd(child);
        if (isValid()) {
            this.scrollData.clamp(getScrollArea());
        }
    }

    @Override
    public void onChildRemove(I child) {
        super.onChildRemove(child);
        if (isValid()) {
            this.scrollData.clamp(getScrollArea());
        }
    }

    public int getSeparatorSize() {
        if (this.childSeparator == null) return 0;
        return this.scrollData.getAxis().isHorizontal() ? this.childSeparator.getWidth() :
                this.childSeparator.getHeight();
    }

    public W scrollDirection(GuiAxis axis) {
        return scrollDirection(ScrollData.of(axis));
    }

    public W scrollDirection(ScrollData data) {
        this.scrollData = data;
        getScrollArea().removeScrollData();
        getScrollArea().setScrollData(this.scrollData);
        return getThis();
    }

    public W childSeparator(IIcon separator) {
        this.childSeparator = separator;
        return getThis();
    }

    public W children(Iterable<I> widgets) {
        for (I widget : widgets) {
            child(widget);
        }
        return getThis();
    }

    public W children(int amount, IntFunction<I> widgetCreator) {
        for (int i = 0; i < amount; i++) {
            child(widgetCreator.apply(i));
        }
        return getThis();
    }

    /**
     * Configures this widget to collapse disabled child widgets.
     */
    public W collapseDisabledChild() {
        this.collapseDisabledChild = true;
        return getThis();
    }
}
