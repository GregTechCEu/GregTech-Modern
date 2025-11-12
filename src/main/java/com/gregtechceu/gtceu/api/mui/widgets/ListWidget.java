package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.base.layout.ILayoutWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IParentWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.AbstractScrollWidget;
import com.gregtechceu.gtceu.api.mui.widget.scroll.ScrollData;
import com.gregtechceu.gtceu.api.mui.widget.scroll.VerticalScrollData;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Unit;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;

import java.util.function.DoubleSupplier;
import java.util.function.Function;
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
    private boolean collapseDisabledChild = true;
    private boolean wrapTight = false;
    private Alignment.CrossAxis crossAxisAlignment = Alignment.CrossAxis.CENTER;
    private Unit mainAxisMaxSize;

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
    public void beforeResize(boolean onOpen) {
        super.beforeResize(onOpen);
        if (this.mainAxisMaxSize != null) {
            flex().setUnit(this.mainAxisMaxSize, getAxis(), Unit.State.SIZE);
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (this.childSeparator == null || this.separatorPositions.isEmpty()) return;
        GuiAxis axis = this.scrollData.getAxis();
        int x = getArea().getPadding().left(), y = getArea().getPadding().top(), w, h;
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
            this.childSeparator.draw(context, x, y, w, h, getActiveWidgetTheme(widgetTheme, isHovering()));
        }
    }

    @Override
    public boolean layoutWidgets() {
        if (!hasChildren()) return true;
        if (this.wrapTight && !resizer().isSizeCalculated(getAxis())) {
            return false;
        }
        this.separatorPositions.clear();
        GuiAxis axis = getAxis();
        int separatorSize = getSeparatorSize();
        int p = getArea().getPadding().getStart(axis);
        for (IWidget widget : getChildren()) {
            if (shouldIgnoreChildSize(widget)) {
                widget.resizer().updateResized();
                continue;
            }
            if (widget.flex().hasPos(axis)) {
                // this is required when the widget has a pos on the main axis, but not on the cross axis
                widget.resizer().updateResized();
                continue;
            }
            if (!widget.resizer().isSizeCalculated(axis)) return false;

            p += widget.getArea().getMargin().getStart(axis);
            widget.getArea().setRelativePoint(axis, p);
            p += widget.getArea().getSize(axis) + widget.getArea().getMargin().getEnd(axis);
            widget.resizer().setPosResized(axis, true);
            widget.resizer().setMarginPaddingApplied(true);
            this.separatorPositions.add(p);
            p += separatorSize;
            if (isValid()) {
                widget.flex().applyPos(widget);
            }
        }
        int size = p + getArea().getPadding().getEnd(axis);
        getScrollData().setScrollSize(size);
        int widgetSize = getArea().getSize(axis);
        if (this.wrapTight && size < widgetSize) {
            getArea().setSize(getAxis(), size);
            resizer().setSizeResized(axis, true);
            if (resizer().isPosCalculated(axis)) {
                // if the position is defined with right(), bottom() or is relative, then the position is very likely
                // invalid now let mui recalculate position
                resizer().setPosResized(axis, false);
            }
        }
        return true;
    }

    @Override
    public boolean postLayoutWidgets() {
        return Flow.layoutCrossAxisListLike(this, getAxis(), this.crossAxisAlignment);
    }

    @Override
    public boolean canCoverByDefaultSize(GuiAxis axis) {
        return axis.getOther() == getAxis();
    }

    @Override
    public boolean shouldIgnoreChildSize(IWidget child) {
        return this.collapseDisabledChild && !child.isEnabled();
    }

    @Override
    public void onChildChangeEnabled(IWidget child, boolean enabled) {
        if (this.collapseDisabledChild) {
            ILayoutWidget.super.onChildChangeEnabled(child, enabled);
        }
    }

    @Override
    public boolean addChild(I child, int index) {
        return super.addChild(child, index);
    }

    @Override
    public boolean remove(I child) {
        return super.remove(child);
    }

    @Override
    public boolean remove(int index) {
        return super.remove(index);
    }

    @Override
    protected void onChildAdd(I child) {
        super.onChildAdd(child);
        if (isValid()) {
            scheduleResize();
            this.scrollData.clamp(getScrollArea());
        }
    }

    @Override
    protected boolean removeAll() {
        return super.removeAll();
    }

    @Override
    protected void onChildRemove(I child) {
        super.onChildRemove(child);
        if (isValid()) {
            scheduleResize();
            this.scrollData.clamp(getScrollArea());
        }
    }

    public int getSeparatorSize() {
        if (this.childSeparator == null) return 0;
        return getAxis().isHorizontal() ? this.childSeparator.getWidth() :
                this.childSeparator.getHeight();
    }

    public GuiAxis getAxis() {
        return this.scrollData.getAxis();
    }

    private W maxSize(float v, int offset, Unit.Measure measure) {
        if (this.mainAxisMaxSize == null) this.mainAxisMaxSize = new Unit();
        this.mainAxisMaxSize.setValue(v);
        this.mainAxisMaxSize.setOffset(offset);
        this.mainAxisMaxSize.setMeasure(measure);
        return wrapTight();
    }

    private W maxSize(DoubleSupplier v, int offset, Unit.Measure measure) {
        if (this.mainAxisMaxSize == null) this.mainAxisMaxSize = new Unit();
        this.mainAxisMaxSize.setValue(v);
        this.mainAxisMaxSize.setOffset(offset);
        this.mainAxisMaxSize.setMeasure(measure);
        return wrapTight();
    }

    public W maxSize(int v) {
        return maxSize(v, 0, Unit.Measure.PIXEL);
    }

    public W maxSizeRel(float v) {
        return maxSize(v, 0, Unit.Measure.RELATIVE);
    }

    public W maxSizeRelOffset(float v, int offset) {
        return maxSize(v, offset, Unit.Measure.RELATIVE);
    }

    public W maxSize(DoubleSupplier v) {
        return maxSize(v, 0, Unit.Measure.PIXEL);
    }

    public W maxSizeRel(DoubleSupplier v) {
        return maxSize(v, 0, Unit.Measure.RELATIVE);
    }

    public W maxSizeRelOffset(DoubleSupplier v, int offset) {
        return maxSize(v, offset, Unit.Measure.RELATIVE);
    }

    public W wrapTight() {
        this.wrapTight = true;
        return getThis();
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

    public <T> W children(Iterable<T> it, Function<T, I> widgetCreator) {
        for (T t : it) {
            child(widgetCreator.apply(t));
        }
        return getThis();
    }

    /**
     * Sets if disabled children should be collapsed.
     */
    public W collapseDisabledChild() {
        this.collapseDisabledChild = true;
        return getThis();
    }

    /**
     * Sets if disabled children should be collapsed. This means that if a child changes enabled state, this widget gets
     * notified and
     * re-layouts its children. Children which are disabled will not be considered during layout, so that the list will
     * not appear to have
     * empty spots. This is enabled by default on lists.
     *
     * @param doCollapse true if disabled children should be collapsed.
     * @return this
     */
    public W collapseDisabledChild(boolean doCollapse) {
        this.collapseDisabledChild = doCollapse;
        return getThis();
    }

    public W crossAxisAlignment(Alignment.CrossAxis caa) {
        this.crossAxisAlignment = caa;
        return getThis();
    }
}
