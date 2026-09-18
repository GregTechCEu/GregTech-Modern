package brachy.modularui.widget.sizer;

import brachy.modularui.GuiError;
import brachy.modularui.ModularUI;
import brachy.modularui.ModularUIConfig;
import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import com.mojang.serialization.Codec;

import lombok.AccessLevel;
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

    public static final MutableObjectCodec<DimensionSizer> CODEC = MutableObjectCodec.builder(DimensionSizer.class)
            .baseCopy(sizer -> new DimensionSizer(sizer.resizer, sizer.axis))
            .addOpt("coverChildrenMinSize", DimensionSizer::setCoverChildrenMinSize, DimensionSizer::getCoverChildrenMinSize, Codec.INT, -1)
            .addOpt("start", DimensionSizer::setStart, DimensionSizer::getStart, Unit.CODEC, Unit.ZERO).neverWriteDefault()
            .addOpt("end", DimensionSizer::setEnd, DimensionSizer::getEnd, Unit.CODEC, Unit.ZERO).neverWriteDefault()
            .addOpt("size", DimensionSizer::setSize, DimensionSizer::getSize, Unit.CODEC, Unit.ZERO).neverWriteDefault()
            .build();

    private final ResizeNode resizer;
    private final GuiAxis axis;

    private final Unit p1 = new Unit(), p2 = new Unit();
    @Getter(AccessLevel.PRIVATE) private Unit start, end, size;
    private Unit next = p1;

    @Getter
    private int coverChildrenMinSize = -1;
    @Setter
    private boolean expanded = false;

    @Getter
    private boolean posCalculated = false, sizeCalculated = false;
    @Getter
    @Setter
    private boolean marginPaddingApplied = false;
    private boolean canRelayout = false;

    public DimensionSizer(ResizeNode resizer, GuiAxis axis) {
        this.resizer = resizer;
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

    public void setCoverChildren(int minSize, IWidget widget) {
        if (minSize >= 0) getSize(widget);
        this.coverChildrenMinSize = minSize;
    }

    private void setCoverChildrenMinSize(int minSize) {
        setCoverChildren(minSize, null);
    }

    public void setUnit(Unit unit, Unit.State pos) {
        switch (pos) {
            case START -> getStart(null).copyPropertiesOf(unit);
            case END -> getEnd(null).copyPropertiesOf(unit);
            case SIZE -> getSize(null).copyPropertiesOf(unit);
        }
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

    public boolean hasFixedPixelSize() {
        return this.size != null && this.size.getMeasure() != Unit.Measure.RELATIVE && !dependsOnChildren();
    }

    public int getFixedPixelSize() {
        return hasFixedPixelSize() ? (int) this.size.getValue() : -1;
    }

    public boolean hasSize() {
        return this.size != null;
    }

    public boolean isFullSize() {
        if (hasSize()) {
            return this.size.isRelative() && this.size.getValue() >= 0.99f && this.size.getAbsOffset() < 5;
        }
        if (hasStart() && hasEnd()) {
            return this.start.isCloseToZero() && this.end.isCloseToZero();
        }
        return false;
    }

    public boolean canRelayout() {
        return this.canRelayout;
    }

    public boolean dependsOnChildren() {
        return this.coverChildrenMinSize >= 0;
    }

    public boolean dependsOnParent() {
        return posDependsOnParent() || sizeDependsOnParent();
    }

    public boolean sizeDependsOnParent() {
        return this.coverChildrenMinSize < 0 && this.size != null && this.size.isRelative();
    }

    public boolean posDependsOnParent() {
        return this.end != null || (this.start != null && this.start.isRelative());
    }

    public void setResized(boolean all) {
        setResized(all, all);
    }

    public void setResized(boolean pos, boolean size) {
        this.posCalculated = pos;
        this.sizeCalculated = size;
        this.canRelayout &= !(pos && size);
    }

    private boolean needsSize(Unit unit) {
        return unit.isRelative() && unit.getAnchor() != 0;
    }

    public void apply(ResizeNode resizer, ResizeNode relativeTo, IntSupplier defaultSize) {
        boolean sizeCalculated = isSizeCalculated();
        boolean posCalculated = isPosCalculated();
        if (sizeCalculated && posCalculated) return;
        int p, s;
        int parentSize = relativeTo.getArea().getSize(this.axis);
        boolean calcParent = relativeTo.isSizeCalculated(this.axis);
        Box padding = resizer.isDecoration() ? Box.ZERO : relativeTo.getArea().getPadding();
        Area area = resizer.getArea();

        if (sizeCalculated) { // pos not calculated
            // size was calculated before
            s = area.getSize(this.axis);
            if (this.start != null) {
                p = calcPoint(this.start, s, parentSize, calcParent);
            } else if (this.end != null) {
                p = calcPoint(this.end, s, parentSize, calcParent) - s;
            } else {
                p = 0;
                this.posCalculated = true;
            }
        } else if (posCalculated) {
            // pos was calculated before
            p = area.getRelativePoint(this.axis);
            if (this.size != null) {
                s = calcSize(this.size, padding, parentSize, calcParent);
            } else {
                s = defaultSize.getAsInt();
                this.sizeCalculated = s > 0;
            }
        } else { // pos and size not calculated
            // calc start, end and size
            if (this.start == null && this.end == null) {
                p = 0;
                if (this.size == null) {
                    s = defaultSize.getAsInt();
                    this.sizeCalculated = s > 0 && !this.expanded && this.coverChildrenMinSize < 0;
                } else {
                    s = calcSize(this.size, padding, parentSize, calcParent);
                }
                this.posCalculated = true;
                // children in layout widgets have no position, but if the size can only be calculated later than we
                // need to tell the parent layout that this position is not final
                this.canRelayout = true;
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
                    s = calcSize(this.size, padding, parentSize, calcParent);
                    p = calcPoint(this.start, s, parentSize, calcParent);
                    this.posCalculated &= (this.sizeCalculated || !needsSize(this.start));
                } else {
                    s = calcSize(this.size, padding, parentSize, calcParent);
                    p = calcPoint(this.end, s, parentSize, calcParent) - s;
                    this.posCalculated &= this.sizeCalculated;
                }
            }
        }

        // TODO find a better place to apply the margin, is it needed at all?
        // apply padding and margin to size
        if (this.sizeCalculated && calcParent && ((this.size != null && this.size.isRelative()) ||
                (this.start != null && this.end != null && (this.start.isRelative() || this.end.isRelative())))) {
            Box margin = area.getMargin();
            // padding is applied in calcSize()
            s = Math.min(s, parentSize /*- padding.getTotal(this.axis)*/ - margin.getTotal(this.axis));
        }
        area.setRelativePoint(this.axis, p);
        area.setPoint(this.axis, p + relativeTo.getArea().getPoint(this.axis)); // temporary
        area.setSize(this.axis, s);
    }

    public int postApply(ResizeNode resizer, ResizeNode relativeTo, int p0, int p1) {
        // only called when the widget cover its children
        int moveAmount = 0;
        // calculate width and recalculate x based on the new width
        int s = p1 - p0, p;
        Area area = resizer.getArea();
        area.setSize(this.axis, s);
        this.sizeCalculated = true;
        if (!isPosCalculated()) {
            Area relativeArea = relativeTo.getArea();
            Box padding = resizer.isDecoration() ? Box.ZERO : relativeArea.getPadding();
            int parentSize = relativeArea.getSize(this.axis);
            boolean parentCalculated = relativeTo.isSizeCalculated(this.axis);
            Unit point = getPoint();
            if (point == null || parentCalculated || !pointRequiresParentSize(point)) {
                if (this.start != null) {
                    p = calcPoint(this.start, s, parentSize, parentCalculated);
                } else if (this.end != null) {
                    p = calcPoint(this.end, s, parentSize, parentCalculated) - s;
                } else {
                    p = area.getRelativePoint(this.axis) + p0/* + area.getMargin().getStart(this.axis)*/;
                    moveAmount = -p0;
                }
                area.setRelativePoint(this.axis, p);
                this.posCalculated = true;
            }
        }
        return moveAmount;
    }

    public void coverChildrenForEmpty(ResizeNode resizer, Area relativeTo) {
        int s = this.coverChildrenMinSize;
        Area area = resizer.getArea();
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

    public void applyMarginAndPaddingToPos(IWidget parent, Area area, Area relativeTo) {
        // apply self margin and parent padding if not done yet
        if (isMarginPaddingApplied()) return;
        setMarginPaddingApplied(true);
        int start = area.getMargin().getStart(this.axis) + relativeTo.getPadding().getStart(this.axis);
        int end = area.getMargin().getEnd(this.axis) + relativeTo.getPadding().getEnd(this.axis);
        if (start > 0 && ((this.start != null && !this.start.isRelative()) ||
                (this.end != null && !this.end.isRelative() && (this.size == null || !this.size.isRelative())))) {
            start = 0;
        }
        if (end > 0 && ((this.end != null && !this.end.isRelative()) ||
                (this.start != null && !this.start.isRelative() && (this.size == null || !this.size.isRelative())))) {
            end = 0;
        }
        if (start == 0 && end == 0) return;
        int parentS = relativeTo.getSize(this.axis);
        int s = area.getSize(this.axis);
        int rp = area.getRelativePoint(this.axis); // relative pos
        if (start > 0) {
            if (end > 0) {
                if (start + end + s > parentS) {
                    // widget and margin + padding is larger than available space
                    area.setRelativePoint(this.axis, start);
                    GuiError.throwNew(parent, GuiError.Type.SIZING,
                            "Margin/padding is set on both sides on axis " + this.axis +
                                    ", but total size exceeds parent size.");
                    return;
                }
                if (end > parentS - s - rp) area.setRelativePoint(this.axis, parentS - end - s);
                else if (start > rp) area.setRelativePoint(this.axis, start);
                return;
            }
            if (start > rp) area.setRelativePoint(this.axis, start);
        } else if (end > 0) {
            if (end > parentS - s - rp) area.setRelativePoint(this.axis, parentS - end - s);
        }
    }

    private int calcSize(Unit s, Box padding, int parentSize, boolean parentSizeCalculated) {
        // placeholder value, size is calculated externally
        if (this.coverChildrenMinSize >= 0 || this.expanded) return 18;
        float val = s.getValue();
        if (s.isRelative()) {
            if (!parentSizeCalculated) return (int) val;
            val *= parentSize - padding.getTotal(this.axis);
        }
        val += s.getOffset();
        this.sizeCalculated = true;
        return (int) val;
    }

    public int calcPoint(Unit p, int width, int parentSize, boolean parentSizeCalculated) {
        float val = p.getValue();
        if (!parentSizeCalculated && pointRequiresParentSize(p)) return (int) val;
        if (p.isRelative()) {
            val *= parentSize;
            float anchor = p.getAnchor();
            if (width > 0 && anchor != 0) {
                val -= width * anchor;
            }
        }
        if (p == this.end) {
            val = parentSize - val;
        }
        val += p.getOffset();
        this.posCalculated = true;
        return (int) val;
    }

    public boolean pointRequiresParentSize(Unit p) {
        return p == this.end || p.isRelative();
    }

    protected Unit getPoint() {
        return this.start != null ? this.start : this.end;
    }

    public void detectConflictingConfiguration() {
        if (this.expanded && this.coverChildrenMinSize >= 0) {
            ModularUI.LOGGER.warn("Resizer '{}' has expanded() and coverChildren() on {} axis. This conflicts and may cause layout issues.", this.resizer, this.axis);
        }
        // TODO detect when this depends and all siblings depend on parent and parent depends on all children
    }

    /**
     * Tries to find a unit for start, end or size. If p1 and p2 are already used, the first one will be overwritten.
     *
     * @param widget   widget this sizer belongs to. Used for logging
     * @param newState the new unit type for the found unit
     * @return a used or unused unit.
     */
    private Unit getNext(IWidget widget, Unit.State newState) {
        Unit ret = this.next;
        Unit other = ret == this.p1 ? this.p2 : this.p1;
        if (ret.state != Unit.State.UNUSED) {
            if (ret.state == newState) return ret;
            if (other.state == newState) return other;
            if (ret == this.start) this.start = null;
            if (ret == this.end) this.end = null;
            if (ret == this.size) this.size = null;
            if (ModularUIConfig.Dev.debugUI() && ModularUI.isClientThread()) {
                // only log on client in debug mode since its sometimes intentional
                ModularUI.LOGGER.info("unit {} of widget {} was already used and will be overwritten with unit {}",
                        ret.state.getText(this.axis), widget, newState.getText(this.axis));
            }
        }
        ret.reset();
        ret.state = newState;
        this.next = other;
        return ret;
    }

    protected Unit getStart(IWidget widget) {
        if (this.start == null) {
            this.start = getNext(widget, Unit.State.START);
        }
        return this.start;
    }

    protected Unit getEnd(IWidget widget) {
        if (this.end == null) {
            this.end = getNext(widget, Unit.State.END);
        }
        return this.end;
    }

    protected Unit getSize(IWidget widget) {
        if (this.size == null) {
            this.size = getNext(widget, Unit.State.SIZE);
        }
        return this.size;
    }

    private void setStart(Unit unit) {
        if (unit == null) {
            if (this.start != null) {
                this.start.reset();
                if (this.next != this.start && !this.next.isUnused()) {
                    this.next = this.start;
                }
                this.start = null;
            }
            return;
        }
        if (this.start != unit) getStart(null).copyPropertiesOf(unit);
    }

    private void setEnd(Unit unit) {
        if (unit == null) {
            if (this.end != null) {
                this.end.reset();
                if (this.next != this.end && !this.next.isUnused()) {
                    this.next = this.end;
                }
                this.end = null;
            }
            return;
        }
        if (this.end != unit) getEnd(null).copyPropertiesOf(unit);
    }

    private void setSize(Unit unit) {
        if (unit == null) {
            if (this.size != null) {
                this.size.reset();
                if (this.next != this.size && !this.next.isUnused()) {
                    this.next = this.size;
                }
                this.size = null;
            }
            return;
        }
        if (this.size != unit) getSize(null).copyPropertiesOf(unit);
    }

    public void copyPropertiesOf(DimensionSizer sizer) {
        reset();
        if (sizer.start != null) getStart(null).copyPropertiesOf(sizer.start);
        if (sizer.end != null) getEnd(null).copyPropertiesOf(sizer.end);
        if (sizer.size != null) getSize(null).copyPropertiesOf(sizer.size);
        this.coverChildrenMinSize = sizer.coverChildrenMinSize;
    }

    public boolean isEqual(DimensionSizer o) {
        return o != null &&
                this.axis == o.axis &&
                Unit.areEqual(this.start, o.start) &&
                Unit.areEqual(this.end, o.end) &&
                Unit.areEqual(this.size, o.size) &&
                this.coverChildrenMinSize == o.coverChildrenMinSize;
    }

    public static boolean areEqual(DimensionSizer a, DimensionSizer b) {
        return a == null ? b == null : a.isEqual(b);
    }
}
