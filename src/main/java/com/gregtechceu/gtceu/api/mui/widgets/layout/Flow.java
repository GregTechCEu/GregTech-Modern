package com.gregtechceu.gtceu.api.mui.widgets.layout;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.layout.ILayoutWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Box;
import com.gregtechceu.gtceu.utils.ReversedList;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.function.IntFunction;

@Accessors(fluent = true, chain = true)
public class Flow extends ParentWidget<Flow> implements ILayoutWidget, IExpander {

    public static Flow row() {
        return new Flow(GuiAxis.X);
    }

    public static Flow column() {
        return new Flow(GuiAxis.Y);
    }

    /**
     * The main axis on which to align children.
     */
    @Getter
    private final GuiAxis axis;
    /**
     * How the children should be laid out on the main axis.
     */
    @Setter
    private Alignment.MainAxis mainAxisAlignment = Alignment.MainAxis.START;
    /**
     * How the children should be laid out on the cross axis.
     */
    @Setter
    private Alignment.CrossAxis crossAxisAlignment = Alignment.CrossAxis.CENTER;
    /**
     * Additional space between each child on main axis.
     * Does not work with {@link Alignment.MainAxis#SPACE_BETWEEN} and {@link Alignment.MainAxis#SPACE_AROUND}.
     */
    @Setter
    private int childPadding = 0;
    /**
     * Whether disabled child widgets should be collapsed for display.
     */
    private boolean collapseDisabledChild = false;
    /**
     * Whether the children list should be laid out in reverse order
     */
    private boolean reverseLayout = false;

    public Flow(GuiAxis axis) {
        this.axis = axis;
        sizeRel(1f, 1f);
    }

    @Override
    public int getDefaultWidth() {
        return this.axis.isHorizontal() ? getDefaultMainAxisSize() : getDefaultCrossAxisSize();
    }

    @Override
    public int getDefaultHeight() {
        return this.axis.isHorizontal() ? getDefaultCrossAxisSize() : getDefaultMainAxisSize();
    }

    public int getDefaultMainAxisSize() {
        if (!hasChildren()) return 18;
        GuiAxis axis = this.axis;
        int total = getArea().getPadding().getTotal(axis);
        for (IWidget widget : getChildren()) {
            if (shouldIgnoreChildSize(widget) || widget.flex().hasPos(axis)) continue;
            if (widget.flex().isExpanded() || !widget.resizer().isSizeCalculated(axis)) {
                total += axis.isHorizontal() ? widget.getDefaultWidth() : widget.getDefaultHeight();
            } else {
                total += widget.getArea().getSize(axis);
            }
            total += widget.getArea().getMargin().getTotal(axis);
        }
        return total;
    }

    public int getDefaultCrossAxisSize() {
        if (!hasChildren()) return 18;
        GuiAxis axis = this.axis.getOther();
        int max = 0;
        for (IWidget widget : getChildren()) {
            if (shouldIgnoreChildSize(widget)) continue;
            int s = widget.getArea().getMargin().getTotal(axis);
            if (!widget.resizer().isSizeCalculated(axis)) {
                s += axis.isHorizontal() ? widget.getDefaultWidth() : widget.getDefaultHeight();
            } else {
                s += widget.getArea().getSize(axis);
            }
            max = Math.max(max, s);
        }
        return max + getArea().getPadding().getTotal(axis);
    }

    @Override
    public boolean layoutWidgets() {
        if (!hasChildren()) return true;
        final boolean hasSize = resizer().isSizeCalculated(this.axis);
        final Box padding = getArea().getPadding();
        final int size = getArea().getSize(axis) - padding.getTotal(this.axis);
        Alignment.MainAxis maa = this.mainAxisAlignment;
        if (!hasSize && maa != Alignment.MainAxis.START) {
            if (flex().dependsOnChildren(this.axis)) {
                // if this flow covers the children, we can assume start
                maa = Alignment.MainAxis.START;
            } else {
                // for anything else than start we need the size to be known
                return false;
            }
        }
        List<IWidget> childrenList = this.reverseLayout ? new ReversedList<>(getChildren()) : getChildren();
        int space = this.childPadding;

        int childrenSize = 0;
        int expandedAmount = 0;
        int amount = 0;

        // calculate total size
        for (IWidget widget : childrenList) {
            // ignore disabled child if configured as such
            if (shouldIgnoreChildSize(widget)) {
                widget.resizer().setMarginPaddingApplied(true);
                continue;
            }
            // exclude children whose position of main axis is fixed
            if (widget.flex().hasPos(this.axis)) continue;
            amount++;
            if (widget.flex().isExpanded()) {
                expandedAmount++;
                childrenSize += widget.getArea().getMargin().getTotal(this.axis);
                continue;
            }
            // if the size of a widget is not calculated we can't continue
            if (!widget.resizer().isSizeCalculated(this.axis)) return false;
            childrenSize += widget.getArea().requestedSize(this.axis);
        }

        if (amount <= 1 && (maa == Alignment.MainAxis.SPACE_BETWEEN || maa == Alignment.MainAxis.SPACE_AROUND)) {
            maa = Alignment.MainAxis.CENTER;
        }
        final int spaceCount = Math.max(amount - 1, 0);

        if (maa == Alignment.MainAxis.SPACE_BETWEEN || maa == Alignment.MainAxis.SPACE_AROUND) {
            if (expandedAmount > 0) {
                maa = Alignment.MainAxis.START;
            } else {
                space = 0;
            }
        }
        childrenSize += space * spaceCount;

        if (expandedAmount > 0 && hasSize) {
            int newSize = (size - childrenSize) / expandedAmount;
            for (IWidget widget : childrenList) {
                // ignore disabled child if configured as such
                if (shouldIgnoreChildSize(widget)) continue;
                // exclude children whose position of main axis is fixed
                if (widget.flex().hasPos(this.axis)) continue;
                if (widget.flex().isExpanded()) {
                    widget.getArea().setSize(this.axis, newSize);
                    widget.resizer().setSizeResized(this.axis, true);
                }
            }
        }

        // calculate start pos
        int lastP = padding.getStart(this.axis);
        if (hasSize) {
            if (maa == Alignment.MainAxis.CENTER) {
                lastP += (int) (size / 2f - childrenSize / 2f);
            } else if (maa == Alignment.MainAxis.END) {
                lastP += size - childrenSize;
            }
        }

        for (IWidget widget : childrenList) {
            // ignore disabled child if configured as such
            if (shouldIgnoreChildSize(widget)) {
                widget.resizer().updateResized();
                widget.resizer().setMarginPaddingApplied(true);
                continue;
            }
            // exclude children whose position of main axis is fixed
            if (widget.flex().hasPos(this.axis)) continue;
            Box margin = widget.getArea().getMargin();

            // set calculated relative main axis pos and set end margin for next widget
            widget.getArea().setRelativePoint(this.axis, lastP + margin.getStart(this.axis));
            widget.resizer().setPosResized(this.axis, true);
            widget.resizer().setMarginPaddingApplied(this.axis, true);

            lastP += widget.getArea().requestedSize(this.axis) + space;
            if (hasSize && maa == Alignment.MainAxis.SPACE_BETWEEN) {
                lastP += (size - childrenSize) / spaceCount;
            }
        }
        return true;
    }

    @Override
    public boolean postLayoutWidgets() {
        return Flow.layoutCrossAxisListLike(this, this.axis, this.crossAxisAlignment, this.reverseLayout);
    }

    public static boolean layoutCrossAxisListLike(IWidget parent, GuiAxis axis,
                                                  Alignment.CrossAxis crossAxisAlignment, boolean reverseLayout) {
        if (!parent.hasChildren()) return true;
        GuiAxis other = axis.getOther();
        int width = parent.getArea().getSize(other);
        Box padding = parent.getArea().getPadding();
        boolean hasWidth = parent.resizer().isSizeCalculated(other);
        if (!hasWidth && crossAxisAlignment != Alignment.CrossAxis.START) return false;

        List<IWidget> childrenList = reverseLayout ? new ReversedList<>(parent.getChildren()) : parent.getChildren();

        for (IWidget widget : childrenList) {
            // exclude children whose position of main axis is fixed
            if (widget.flex().hasPos(axis)) {
                // this is required when the widget has a pos on the main axis, but not on the cross axis
                widget.resizer().updateResized();
                continue;
            }
            Box margin = widget.getArea().getMargin();
            // don't align auto positioned children in cross axis
            if (!widget.flex().hasPos(other) && widget.resizer().isSizeCalculated(other)) {
                int crossAxisPos = margin.getStart(other) + padding.getStart(other);
                if (hasWidth) {
                    if (crossAxisAlignment == Alignment.CrossAxis.CENTER) {
                        crossAxisPos = (int) (width / 2f - widget.getArea().getSize(other) / 2f);
                    } else if (crossAxisAlignment == Alignment.CrossAxis.END) {
                        crossAxisPos = width - widget.getArea().getSize(other) - margin.getEnd(other) -
                                padding.getStart(other);
                    }
                }
                widget.getArea().setRelativePoint(other, crossAxisPos);
                widget.getArea().setPoint(other, parent.getArea().getPoint(other) + crossAxisPos);
                widget.resizer().setPosResized(other, true);
                widget.resizer().setMarginPaddingApplied(other, true);
            }
            if (parent.isValid()) {
                // we changed relative pos, but we need to calculate the new absolute pos and other stuff
                widget.flex().applyPos(widget);
            }
        }
        return true;
    }

    @Override
    public boolean canCoverByDefaultSize(GuiAxis axis) {
        return axis.getOther() == this.axis;
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

    public Flow children(Iterable<IWidget> widgets) {
        for (IWidget widget : widgets) {
            child(widget);
        }
        return getThis();
    }

    public Flow children(int amount, IntFunction<IWidget> widgetCreator) {
        for (int i = 0; i < amount; i++) {
            child(widgetCreator.apply(i));
        }
        return getThis();
    }

    /**
     * Sets if disabled children should be collapsed.
     */
    public Flow collapseDisabledChild() {
        this.collapseDisabledChild = true;
        return this;
    }

    /**
     * Sets if disabled children should be collapsed. This means that if a child changes enabled state, this widget
     * gets notified and re-layouts its children. Children which are disabled will not be considered during layout,
     * so that the flow will not appear to have empty spots. This is disabled by default on Flow.
     *
     * @param collapse true if disabled children should be collapsed.
     * @return this
     */
    public Flow collapseDisabledChild(boolean collapse) {
        this.collapseDisabledChild = collapse;
        return this;
    }

    /**
     * Sets if the children list should be laid out in reversed or not (Default is false).
     * 
     * @param reverseLayout true if the children list should be laid out in reverse
     * @return this
     */
    public Flow reverseLayout(boolean reverseLayout) {
        this.reverseLayout = reverseLayout;
        return this;
    }

    @Override
    public GuiAxis getExpandAxis() {
        return this.axis;
    }

    @Override
    protected String getTypeName() {
        return this.axis.isHorizontal() ? "Row" : "Column";
    }
}
