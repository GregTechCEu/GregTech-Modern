package com.gregtechceu.gtceu.api.mui.widget.sizer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.layout.IResizeable;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiElement;
import com.gregtechceu.gtceu.config.ConfigHolder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.IntSupplier;

/**
 * Handles calculating size and position in one dimension (x or y).
 * Two of these can fully calculate a widget size and pos.
 */
@ApiStatus.Internal
public class DimensionSizer {

    private final GuiAxis axis;

    private final Unit p1 = new Unit(), p2 = new Unit();
    private Unit start, end, size;
    private Unit next = p1;

    @Setter
    private boolean coverChildren = false, expanded = false;
    @Setter
    private boolean cancelAutoMovement = false;

    @Getter
    private boolean posCalculated = false, sizeCalculated = false;
    @Getter
    @Setter
    private boolean marginPaddingApplied = false;

    public DimensionSizer(GuiAxis axis) {
        this.axis = axis;
    }

    public void reset() {
        this.p1.reset();
        this.p2.reset();
        this.start = null;
        this.end = null;
        this.size = null;
        this.next = this.p1;
    }

    public void resetPosition() {
        if (this.start != null) {
            this.start.reset();
            this.start = null;
        }
        if (this.end != null) {
            this.end.reset();
            this.end = null;
        }
        if (this.p1.isUnused()) {
            this.next = this.p1;
        } else if (this.p2.isUnused()) {
            this.next = this.p2;
        }
    }

    public void resetSize() {
        if (this.size != null) {
            this.size.reset();
            this.size = null;
        }
        if (this.p1.isUnused()) {
            this.next = this.p1;
        } else if (this.p2.isUnused()) {
            this.next = this.p2;
        }
    }

    public void setCoverChildren(boolean coverChildren, IGuiElement widget) {
        getSize(widget);
        this.coverChildren = coverChildren;
    }

    public boolean hasStart() {
        return this.start != null;
    }

    public boolean hasEnd() {
        return this.end != null;
    }

    public boolean hasPos() {
        return this.start != null || this.end != null;
    }

    public boolean hasFixedSize() {
        return this.start == null || this.end == null;
    }

    public boolean hasSize() {
        return this.size != null;
    }

    public boolean dependsOnChildren() {
        return this.coverChildren;
    }

    public boolean dependsOnParent() {
        return this.end != null ||
                (this.start != null && this.start.isRelative()) ||
                (this.size != null && this.size.isRelative());
    }

    public void setResized(boolean all) {
        setResized(all, all);
    }

    public void setResized(boolean pos, boolean size) {
        this.posCalculated = pos;
        this.sizeCalculated = size;
    }

    private boolean needsSize(Unit unit) {
        return unit.isRelative() && unit.getAnchor() != 0;
    }

    public void apply(Area area, IResizeable relativeTo, IntSupplier defaultSize) {
        // is already calculated
        if (this.sizeCalculated && this.posCalculated) return;
        int p, s;
        int parentSize = relativeTo.getArea().getSize(this.axis);
        boolean calcParent = relativeTo.isSizeCalculated(this.axis);

        if (this.sizeCalculated && !this.posCalculated) {
            // size was calculated before
            s = area.getSize(this.axis);
            if (this.start != null) {
                p = calcPoint(this.start, s, parentSize, calcParent);
            } else if (this.end != null) {
                p = calcPoint(this.end, s, parentSize, calcParent) - s;
            } else {
                throw new IllegalStateException();
            }
        } else if (!this.sizeCalculated && this.posCalculated) {
            // pos was calculated before
            p = area.getRelativePoint(this.axis);
            if (this.size != null) {
                s = this.coverChildren ? 18 : calcSize(this.size, parentSize, calcParent);
            } else {
                s = defaultSize.getAsInt();
                this.sizeCalculated = s > 0;
            }
        } else {
            // calc start, end and size
            if (this.start == null && this.end == null) {
                p = 0;
                if (this.size == null) {
                    s = defaultSize.getAsInt();
                    this.sizeCalculated = s > 0 && !this.expanded;
                } else {
                    s = calcSize(this.size, parentSize, calcParent);
                }
                this.posCalculated = true;
            } else {
                if (this.size == null) {
                    if (this.start != null && this.end != null) {
                        p = calcPoint(this.start, -1, parentSize, calcParent);
                        boolean b = this.posCalculated;
                        this.posCalculated = false;
                        int p2 = calcPoint(this.end, -1, parentSize, calcParent);
                        s = Math.abs(p2 - p);
                        this.posCalculated &= b;
                        this.sizeCalculated |= this.posCalculated;
                    } else {
                        s = defaultSize.getAsInt();
                        this.sizeCalculated = s > 0 && !this.expanded;
                        if (this.start == null) {
                            p = calcPoint(this.end, s, parentSize, calcParent);
                            p -= s;
                            this.posCalculated &= this.sizeCalculated;
                        } else {
                            p = calcPoint(this.start, s, parentSize, calcParent);
                            this.posCalculated &= (this.sizeCalculated || !needsSize(this.start));
                        }
                    }
                } else if (this.start != null) {
                    s = calcSize(this.size, parentSize, calcParent);
                    p = calcPoint(this.start, s, parentSize, calcParent);
                    this.posCalculated &= (this.sizeCalculated || !needsSize(this.start));
                } else {
                    s = calcSize(this.size, parentSize, calcParent);
                    p = calcPoint(this.end, s, parentSize, calcParent) - s;
                    this.posCalculated &= this.sizeCalculated;
                }
            }
        }

        // apply padding and margin to size
        if (this.sizeCalculated && calcParent && ((this.size != null && this.size.isRelative()) ||
                (this.start != null && this.end != null && (this.start.isRelative() || this.end.isRelative())))) {
            Box padding = relativeTo.getArea().getPadding();
            Box margin = area.getMargin();
            s = Math.min(s, parentSize - padding.getTotal(this.axis) - margin.getTotal(this.axis));
        }
        area.setRelativePoint(this.axis, p);
        area.setPoint(this.axis, p + relativeTo.getArea().x); // temporary
        area.setSize(this.axis, s);
    }

    public int postApply(Area area, Area relativeTo, int p0, int p1) {
        // only called when the widget cover its children
        int moveAmount = 0;
        // calculate width and recalculate x based on the new width
        int s = p1 - p0, p;
        area.setSize(this.axis, s);
        this.sizeCalculated = true;
        if (!isPosCalculated()) {
            if (this.start != null) {
                p = calcPoint(this.start, s, relativeTo.getSize(this.axis), true);
            } else if (this.end != null) {
                p = calcPoint(this.end, s, relativeTo.getSize(this.axis), true) - s;
            } else {
                p = area.getRelativePoint(this.axis) + p0/* + area.getMargin().getStart(this.axis) */;
                if (!this.cancelAutoMovement) {
                    moveAmount = -p0;
                }
            }
            area.setRelativePoint(this.axis, p);
            this.posCalculated = true;
        }
        return moveAmount;
    }

    public void coverChildrenForEmpty(Area area, Area relativeTo) {
        int s = 0;
        area.setSize(this.axis, s);
        this.sizeCalculated = true;
        if (!isPosCalculated()) {
            int p;
            if (this.start != null) {
                p = calcPoint(this.start, s, relativeTo.getSize(this.axis), true);
            } else if (this.end != null) {
                p = calcPoint(this.end, s, relativeTo.getSize(this.axis), true) - s;
            } else {
                p = area.getRelativePoint(this.axis);
            }
            area.setRelativePoint(this.axis, p);
            this.posCalculated = true;
        }
    }

    public void applyMarginAndPaddingToPos(Area area, Area relativeTo) {
        // apply self margin and parent padding if not done yet
        if (isMarginPaddingApplied()) return;
        setMarginPaddingApplied(true);
        int o = area.getMargin().getStart(this.axis) + relativeTo.getPadding().getStart(this.axis);
        if (o == 0) return;
        if (this.start != null && !this.start.isRelative()) return;
        if (this.end != null && !this.end.isRelative() && (this.size == null || !this.size.isRelative())) return;
        area.setRelativePoint(this.axis, area.getRelativePoint(this.axis) + o);
    }

    private int calcSize(Unit s, int parentSize, boolean parentSizeCalculated) {
        if (this.coverChildren) return 18;
        float val = s.getValue();
        if (s.isRelative()) {
            if (!parentSizeCalculated) return (int) val;
            val *= parentSize;
        }
        this.sizeCalculated = true;
        return (int) val;
    }

    public int calcPoint(Unit p, int width, int parentSize, boolean parentSizeCalculated) {
        float val = p.getValue();
        if (!parentSizeCalculated && (p == this.end || p.isRelative())) return (int) val;
        if (p.isRelative()) {
            val = parentSize * val;
            float anchor = p.getAnchor();
            if (width > 0 && anchor != 0) {
                val -= width * anchor;
            }
            if (p.getOffset() != 0) {
                val += p.getOffset();
            }
        }
        if (p == this.end) {
            val = parentSize - val;
        }
        this.posCalculated = true;
        return (int) val;
    }

    /**
     * Tries to find a unit for start, end or size. If p1 and p2 are already used, the first one will be overwritten.
     *
     * @param widget   widget this sizer belongs to. Used for logging
     * @param newState the new unit type for the found unit
     * @return a used or unused unit.
     */
    private Unit getNext(IGuiElement widget, Unit.State newState) {
        Unit ret = this.next;
        Unit other = ret == this.p1 ? this.p2 : this.p1;
        if (ret.state != Unit.State.UNUSED) {
            if (ret.state == newState) return ret;
            if (other.state == newState) return other;
            if (ret == this.start) this.start = null;
            if (ret == this.end) this.end = null;
            if (ret == this.size) this.size = null;
            if (ConfigHolder.INSTANCE.dev.debugUI && GTCEu.isClientThread()) {
                // only log on client in debug mode since its sometimes intentional
                GTCEu.LOGGER.info("unit {} of widget {} was already used and will be overwritten with unit {}",
                        ret.state.getText(this.axis), widget, newState.getText(this.axis));
            }
        }
        ret.reset();
        ret.state = newState;
        this.next = other;
        return ret;
    }

    protected Unit getStart(IGuiElement widget) {
        if (this.start == null) {
            this.start = getNext(widget, Unit.State.START);
        }
        return this.start;
    }

    protected Unit getEnd(IGuiElement widget) {
        if (this.end == null) {
            this.end = getNext(widget, Unit.State.END);
        }
        return this.end;
    }

    protected Unit getSize(IGuiElement widget) {
        if (this.size == null) {
            this.size = getNext(widget, Unit.State.SIZE);
        }
        return this.size;
    }
}
