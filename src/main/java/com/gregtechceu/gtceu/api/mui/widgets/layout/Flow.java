package com.gregtechceu.gtceu.api.mui.widgets.layout;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.layout.ILayoutWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Box;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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

    public Flow(GuiAxis axis) {
        this.axis = axis;
        sizeRel(1f, 1f);
    }

    @Override
    public boolean layoutWidgets() {
        if (!hasChildren()) return true;
        final boolean hasSize = resizer().isSizeCalculated(this.axis);
        final Box padding = getArea().getPadding();
        final int size = getArea().getSize(axis) - padding.getTotal(this.axis);
        Alignment.MainAxis maa = this.mainAxisAlignment;
        if (!hasSize && maa != Alignment.MainAxis.START) {
            // for anything else than start we need the size to be known
            return false;
        }
        int space = this.childPadding;

        int childrenSize = 0;
        int expandedAmount = 0;
        int amount = 0;

        // calculate total size
        for (IWidget widget : getChildren()) {
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
            for (IWidget widget : getChildren()) {
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

        for (IWidget widget : getChildren()) {
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
        if (!hasChildren()) return true;
        GuiAxis other = this.axis.getOther();
        int width = getArea().getSize(other);
        Box padding = getArea().getPadding();
        boolean hasWidth = resizer().isSizeCalculated(other);
        if (!hasWidth && this.crossAxisAlignment != Alignment.CrossAxis.START) return false;
        for (IWidget widget : getChildren()) {
            // exclude children whose position of main axis is fixed
            if (widget.flex().hasPos(this.axis)) {
                // this is required when the widget has a pos on the main axis, but not on the cross axis
                widget.resizer().updateResized();
                continue;
            }
            Box margin = widget.getArea().getMargin();
            // don't align auto positioned children in cross axis
            if (!widget.flex().hasPos(other) && widget.resizer().isSizeCalculated(other)) {
                int crossAxisPos = margin.getStart(other) + padding.getStart(other);
                if (hasWidth) {
                    if (this.crossAxisAlignment == Alignment.CrossAxis.CENTER) {
                        crossAxisPos = (int) (width / 2f - widget.getArea().getSize(other) / 2f);
                    } else if (this.crossAxisAlignment == Alignment.CrossAxis.END) {
                        crossAxisPos = width - widget.getArea().getSize(other) - margin.getEnd(other) -
                                padding.getStart(other);
                    }
                }
                widget.getArea().setRelativePoint(other, crossAxisPos);
                widget.getArea().setPoint(other, getArea().getPoint(other) + crossAxisPos);
                widget.resizer().setPosResized(other, true);
                widget.resizer().setMarginPaddingApplied(other, true);
            }
            if (isValid()) {
                // we changed relative pos, but we need to calculate the new absolute pos and other stuff
                widget.flex().applyPos(widget);
            }
        }
        return true;
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
     * @param doCollapse true if disabled children should be collapsed.
     * @return this
     */
    public Flow collapseDisabledChild(boolean doCollapse) {
        this.collapseDisabledChild = doCollapse;
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
