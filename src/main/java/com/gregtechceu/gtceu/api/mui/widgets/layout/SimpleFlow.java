package com.gregtechceu.gtceu.api.mui.widgets.layout;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Box;

import java.util.ArrayList;
import java.util.List;

public class SimpleFlow {

    public final List<IWidget> widgets = new ArrayList<>();
    public int size, expanderCount, crossSize;
    public boolean crossSizeCalculated;

    public void layout(GuiAxis axis, int availableSize, Box padding, Alignment.MainAxis maa, int childPadding) {
        int amount = widgets.size();
        // special cases
        if (expanderCount > 0) {
            maa = Alignment.MainAxis.START;
        } else if (amount <= 1 && (maa == Alignment.MainAxis.SPACE_BETWEEN || maa == Alignment.MainAxis.SPACE_AROUND)) {
            maa = Alignment.MainAxis.CENTER;
        }

        // calculate space between children
        int space = childPadding;
        if (maa == Alignment.MainAxis.SPACE_BETWEEN || maa == Alignment.MainAxis.SPACE_AROUND) {
            if (maa == Alignment.MainAxis.SPACE_BETWEEN) {
                space = (availableSize - size) / (amount - 1);
            } else {
                space = (availableSize - size) / amount;
            }
        }

        // calculate size of expanded
        if (expanderCount > 0 && availableSize > 0) {
            int newSize = (availableSize - size) / expanderCount;
            for (IWidget widget : widgets) {
                if (widget.resizer().isExpanded()) {
                    widget.getArea().setSize(axis, newSize);
                    widget.resizer().setSizeResized(axis, true);
                }
            }
        }

        // calculate start pos
        int lastP = padding.getStart(axis);
        if (availableSize > 0) {
            if (maa == Alignment.MainAxis.CENTER) {
                lastP += (int) (availableSize / 2f - size / 2f);
            } else if (maa == Alignment.MainAxis.END) {
                lastP += availableSize - size;
            } else if (maa == Alignment.MainAxis.SPACE_AROUND) {
                lastP += space / 2;
            }
        }

        // apply pos
        for (IWidget widget : widgets) {
            Box margin = widget.getArea().getMargin();
            // set calculated relative main axis pos and set end margin for next widget
            widget.getArea().setRelativePoint(axis, lastP + margin.getStart(axis));
            widget.resizer().setPosResized(axis, true);
            widget.resizer().setMarginPaddingApplied(axis, true);
            lastP += widget.getArea().requestedSize(axis) + space;
        }
    }

    public void calculateCrossAxisSize(GuiAxis axis) {
        // calculates maximum size along the cross axis
        GuiAxis other = axis.getOther();
        if (!this.crossSizeCalculated) {
            this.crossSizeCalculated = true;
            this.crossSize = 0;
            for (IWidget widget : widgets) {
                // exclude children whose position of main axis or cross axis is fixed
                if (widget.resizer().hasXPos() || widget.resizer().hasYPos()) continue;
                if (!widget.resizer().isSizeCalculated(other)) {
                    this.crossSizeCalculated = false;
                    continue;
                }
                this.crossSize = Math.max(widget.getArea().requestedSize(other), this.crossSize);
            }
        }
    }

    public boolean layoutCrossAxis(IWidget parent, GuiAxis axis, Alignment.CrossAxis caa, int availableSize, int p,
                                   Box padding) {
        GuiAxis other = axis.getOther();
        if (availableSize < 0 && caa != Alignment.CrossAxis.START) return false;
        for (IWidget widget : this.widgets) {
            // exclude children whose position of main axis is fixed
            if (widget.resizer().hasPos(axis)) continue;
            Box margin = widget.getArea().getMargin();
            // don't align auto positioned children in cross axis
            if (!widget.resizer().hasPos(other) && widget.resizer().isSizeCalculated(other)) {
                int start = margin.getStart(other) + padding.getStart(other);
                int crossAxisPos = 0;
                if (caa == Alignment.CrossAxis.START) {
                    crossAxisPos = start;
                } else {
                    int end = margin.getEnd(other) + padding.getEnd(other);
                    int s = widget.getArea().getSize(other);
                    if (caa == Alignment.CrossAxis.END) {
                        crossAxisPos = availableSize - s - end;
                    } else if (caa == Alignment.CrossAxis.CENTER) {
                        crossAxisPos = (int) (availableSize / 2f - widget.getArea().getSize(other) / 2f);
                        if (availableSize < s + start + end) {
                            GTCEu.LOGGER.warn(
                                    "Widget {} is larger with padding on axis {} than parent {}. Padding can't be applied correctly!",
                                    widget, other, parent);
                        } else {
                            if (crossAxisPos < start) crossAxisPos = start;
                            else if (crossAxisPos > availableSize - end - s) crossAxisPos = availableSize - end - s;
                        }
                    }
                }
                widget.getArea().setRelativePoint(other, crossAxisPos + p);
                widget.getArea().setPoint(other, parent.getArea().getPoint(other) + crossAxisPos + p);
                widget.resizer().setPosResized(other, true);
                widget.resizer().setMarginPaddingApplied(other, true);
            }
        }
        return true;
    }
}
