package com.gregtechceu.gtceu.api.mui.widget.sizer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.GuiError;
import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.layout.ILayoutWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.*;
import com.gregtechceu.gtceu.api.mui.utils.TreeUtil;
import com.gregtechceu.gtceu.api.mui.widgets.layout.IExpander;
import com.gregtechceu.gtceu.core.mixins.client.SlotAccessor;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.DoubleSupplier;

/**
 * This class handles resizing and positioning of widgets.
 */
public class StandardResizer extends WidgetResizeNode implements IPositioned<StandardResizer> {

    private final DimensionSizer x;
    private final DimensionSizer y;
    @Getter
    private boolean expanded = false;

    private boolean childrenResized = false;
    private boolean layoutResized = false;
    private boolean relativeToScreen = false;

    @Getter
    private boolean decoration = false;

    public StandardResizer(IWidget widget) {
        super(widget);
        this.x = createDimensionSizer(GuiAxis.X);
        this.y = createDimensionSizer(GuiAxis.Y);
    }

    protected DimensionSizer createDimensionSizer(GuiAxis axis) {
        return new DimensionSizer(this, axis);
    }

    @Override
    public void initialize(ResizeNode defaultParent, ResizeNode root) {
        super.initialize(defaultParent, root);
        if (this.relativeToScreen) {
            setParentOverride(root);
        }
    }

    @Override
    public void initResizing(boolean onOpen) {
        setMarginPaddingApplied(false);
        setResized(false);
        this.childrenResized = false;
        this.layoutResized = false;
        super.initResizing(onOpen);
        if (onOpen) {
            detectConflictingConfiguration();
        }
    }

    @Override
    public void reset() {
        this.x.reset();
        this.y.reset();
    }

    public void resetPosition() {
        this.x.resetPosition();
        this.y.resetPosition();
    }

    public void detectConflictingConfiguration() {
        if (!GTCEu.isDev()) return;
        if (this.expanded && !(getParent() instanceof IExpander)) {
            GTCEu.LOGGER.warn("Resizer '{}' has expanded() but the parent is not a Flow!", this);
        }
        this.x.detectConflictingConfiguration();
        this.y.detectConflictingConfiguration();
    }

    @Override
    public boolean isXCalculated() {
        return this.x.isPosCalculated();
    }

    @Override
    public boolean isYCalculated() {
        return this.y.isPosCalculated();
    }

    @Override
    public boolean isWidthCalculated() {
        return this.x.isSizeCalculated();
    }

    @Override
    public boolean isHeightCalculated() {
        return this.y.isSizeCalculated();
    }

    @Override
    public boolean areChildrenCalculated() {
        return this.childrenResized;
    }

    @Override
    public boolean isLayoutDone() {
        return this.layoutResized;
    }

    @Override
    public boolean canRelayout(boolean isParentLayout) {
        return isParentLayout && (this.x.canRelayout() || this.y.canRelayout());
    }

    @Override
    public boolean isXMarginPaddingApplied() {
        return this.x.isMarginPaddingApplied();
    }

    @Override
    public boolean isYMarginPaddingApplied() {
        return this.y.isMarginPaddingApplied();
    }

    @Override
    public StandardResizer resizer() {
        return this;
    }

    @Override
    public void scheduleResize() {
        markDirty();
    }

    @ApiStatus.Internal
    @Override
    public void checkExpanded(@Nullable GuiAxis axis) {
        this.x.setExpanded(false);
        this.y.setExpanded(false);
        if (this.expanded && axis != null) {
            if (axis.isHorizontal()) this.x.setExpanded(true);
            else this.y.setExpanded(true);
        }
    }

    @Override
    public boolean resize(boolean isParentLayout) {
        ResizeNode relativeTo = getParent();
        // calculate x, y, width and height if possible
        this.x.apply(this, relativeTo, () -> getWidget().getDefaultWidth());
        this.y.apply(this, relativeTo, () -> getWidget().getDefaultHeight());
        return isSelfFullyCalculated(isParentLayout);
    }

    @Override
    public boolean postResize() {
        boolean coverWidth = this.x.dependsOnChildren();
        boolean coverHeight = this.y.dependsOnChildren();
        if (!coverWidth && !coverHeight) return isSelfFullyCalculated();
        IWidget widget = getWidget();
        List<IWidget> children = widget.getChildren(); // TODO cover resizer children instead?
        int decoCount = 0;
        if (!children.isEmpty()) {
            for (IWidget child : widget.getChildren()) {
                if (child.resizer().isDecoration()) {
                    decoCount++;
                }
            }
        }
        if (decoCount == children.size()) {
            coverChildrenForEmpty();
            return isSelfFullyCalculated();
        }
        if (getWidget() instanceof ILayoutWidget layout) {
            // layout widgets handle widget layout's themselves, so we only need to fit the right and bottom border
            coverChildrenForLayout(layout, widget, children);
            return isSelfFullyCalculated();
        }
        return doCoverChildren(widget, children, coverWidth, coverHeight) && isSelfFullyCalculated();
    }

    protected boolean doCoverChildren(IWidget widget, List<IWidget> children, boolean coverWidth, boolean coverHeight) {
        // non layout widgets can have their children in any position
        // we try to wrap all edges as close as possible to all widgets
        // this means for each edge there is at least one widget that touches it (plus padding and margin)

        // children are now calculated and now this area can be calculated if it requires children's area
        int moveChildrenX = 0, moveChildrenY = 0;

        Box padding = getWidget().getArea().getPadding();
        // first calculate the area the children span
        int x0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, y0 = Integer.MAX_VALUE, y1 = Integer.MIN_VALUE;
        int w = 0, h = 0;
        boolean hasIndependentChildX = false;
        boolean hasIndependentChildY = false;
        for (IWidget child : children) {
            StandardResizer resizer = child.resizer();
            if (resizer.isDecoration()) continue;
            Box margin = child.getArea().getMargin();
            Area area = child.getArea();
            if (coverWidth && !resizer.widthDependsOnParent()) {
                hasIndependentChildX = true;
                if (!resizer.isWidthCalculated()) return true;
                w = Math.max(w, area.requestedWidth() + padding.horizontal());
                if (!resizer.xDependsOnParent()) {
                    if (!resizer.isXCalculated()) return true;
                    x0 = Math.min(x0, area.rx - padding.left() - margin.left());
                    x1 = Math.max(x1, area.rx + area.width + padding.right() + margin.right());
                }
            }

            if (coverHeight && !resizer.heightDependsOnParent()) {
                hasIndependentChildY = true;
                if (!resizer.isHeightCalculated()) return true;
                h = Math.max(h, area.requestedHeight() + padding.vertical());
                if (!resizer.yDependsOnParent()) {
                    if (!resizer.isYCalculated()) return true;
                    y0 = Math.min(y0, area.ry - padding.top() - margin.top());
                    y1 = Math.max(y1, area.ry + area.height + padding.bottom() + margin.bottom());
                }
            }
        }

        if (coverWidth && !hasIndependentChildX) {
            if (this.x.getCoverChildrenMinSize() > 0) {
                // if all children sizes depend on their parent, just use the min size as default
                w = this.x.getCoverChildrenMinSize();
            } else {
                GuiError.throwNew(getWidget(), GuiError.Type.SIZING,
                        "Can't cover children width when all children depend on their parent and min size is 0!");
                return false;
            }
        }
        if (coverHeight && !hasIndependentChildY) {
            if (this.y.getCoverChildrenMinSize() > 0) {
                // if all children sizes depend on their parent, just use the min size as default
                h = this.y.getCoverChildrenMinSize();
            } else {
                GuiError.throwNew(getWidget(), GuiError.Type.SIZING,
                        "Can't cover children height when all children depend on their parent and min size is 0!");
                return false;
            }
        }

        if (x1 == Integer.MIN_VALUE) x1 = 0;
        if (y1 == Integer.MIN_VALUE) y1 = 0;
        if (x0 == Integer.MAX_VALUE) x0 = 0;
        if (y0 == Integer.MAX_VALUE) y0 = 0;
        if (w > x1 - x0) x1 = x0 + w; // we found at least one widget which was wider than what was calculated by start
        // and end pos
        if (h > y1 - y0) y1 = y0 + h;

        // now calculate new x, y, width and height based on the children area
        ResizeNode relativeTo = getParent();
        if (coverWidth) {
            int minSize = this.x.getCoverChildrenMinSize();
            int diff = minSize - x1 + x0;
            if (diff > 0) {
                x0 -= diff / 2;
                x1 += diff / 2;
            }
            // apply the size to this widget
            // the return value is the amount of pixels we need to move the children
            moveChildrenX = this.x.postApply(this, relativeTo, x0, x1);
        }
        if (coverHeight) {
            int minSize = this.y.getCoverChildrenMinSize();
            int diff = minSize - y1 + y0;
            if (diff > 0) {
                y0 -= diff / 2;
                y1 += diff / 2;
            }
            moveChildrenY = this.y.postApply(this, relativeTo, y0, y1);
        }
        // since the edges might have been moved closer to the widgets, the widgets should move back into it's original
        // (absolute) position
        if (moveChildrenX != 0 || moveChildrenY != 0) {
            for (IWidget child : children) {
                StandardResizer resizeable = child.resizer();
                if (resizeable.isDecoration()) continue;
                Area area = child.getArea();
                if (resizeable.isXCalculated()) area.rx += moveChildrenX;
                if (resizeable.isYCalculated()) area.ry += moveChildrenY;
            }
        }
        return true;
    }

    protected void coverChildrenForLayout(ILayoutWidget layout, IWidget widget, List<IWidget> children) {
        Box padding = getWidget().getArea().getPadding();
        // first calculate the area the children span
        int x1 = Integer.MIN_VALUE, y1 = Integer.MIN_VALUE;
        int w = 0, h = 0;
        int withDefaultW = 0, withDefaultH = 0;
        boolean coverWidth = this.x.dependsOnChildren();
        boolean coverHeight = this.y.dependsOnChildren();
        boolean hasIndependentChildX = false;
        boolean hasIndependentChildY = false;
        boolean coverByDefaultSizeX = coverWidth && layout.canCoverByDefaultSize(GuiAxis.X);
        boolean coverByDefaultSizeY = coverHeight && layout.canCoverByDefaultSize(GuiAxis.Y);
        for (IWidget child : children) {
            if (layout.shouldIgnoreChildSize(child)) continue;
            StandardResizer resizer = child.resizer();
            if (resizer.isDecoration()) continue;
            Area area = child.getArea();
            Box margin = area.getMargin();
            if (coverWidth) {
                if (!resizer.widthDependsOnParent()) {
                    hasIndependentChildX = true;
                    if (!resizer.isWidthCalculated()) return;
                    int s = area.requestedWidth() + padding.horizontal();
                    w = Math.max(w, s);
                    withDefaultW = Math.max(withDefaultW, s);
                    if (!resizer.xDependsOnParent()) {
                        if (!resizer.isXCalculated()) return;
                        x1 = Math.max(x1, area.rx + area.width + padding.right() + margin.right());
                    }
                } else if (coverByDefaultSizeX) {
                    withDefaultW = Math.max(withDefaultW,
                            child.getDefaultWidth() + margin.horizontal() + padding.horizontal());
                }
            }

            if (coverHeight) {
                if (!resizer.heightDependsOnParent()) {
                    hasIndependentChildY = true;
                    if (!resizer.isHeightCalculated()) return;
                    int s = area.requestedHeight() + padding.vertical();
                    h = Math.max(h, s);
                    withDefaultH = Math.max(withDefaultH, s);
                    if (!resizer.yDependsOnParent()) {
                        if (!resizer.isYCalculated()) return;
                        y1 = Math.max(y1, area.ry + area.height + padding.bottom() + margin.bottom());
                    }
                } else if (coverByDefaultSizeY) {
                    withDefaultH = Math.max(withDefaultH,
                            child.getDefaultHeight() + margin.vertical() + padding.vertical());
                }
            }
        }

        if (coverWidth && !hasIndependentChildX && !coverByDefaultSizeX) {
            if (this.x.getCoverChildrenMinSize() > 0) {
                // if all children sizes depend on their parent, just use the min size as default
                w = this.x.getCoverChildrenMinSize();
            } else {
                GuiError.throwNew(getWidget(), GuiError.Type.SIZING,
                        "Can't cover children width when all children depend on their parent and min size is 0!");
                return;
            }
        }
        if (coverHeight && !hasIndependentChildY && !coverByDefaultSizeY) {
            if (this.y.getCoverChildrenMinSize() > 0) {
                // if all children sizes depend on their parent, just use the min size as default
                h = this.y.getCoverChildrenMinSize();
            } else {
                GuiError.throwNew(getWidget(), GuiError.Type.SIZING,
                        "Can't cover children height when all children depend on their parent and min size is 0!");
                return;
            }
        }

        if (w == 0) w = withDefaultW; // only use default sizes, if no size is defined
        if (h == 0) h = withDefaultH;
        if (x1 == Integer.MIN_VALUE) x1 = 0;
        if (y1 == Integer.MIN_VALUE) y1 = 0;
        if (w > x1) x1 = w;
        if (h > y1) y1 = h;

        ResizeNode relativeTo = getParent();
        if (coverWidth) {
            int minSize = this.x.getCoverChildrenMinSize();
            this.x.postApply(this, relativeTo, 0, Math.max(x1, minSize));
        }
        if (coverHeight) {
            int minSize = this.y.getCoverChildrenMinSize();
            this.y.postApply(this, relativeTo, 0, Math.max(y1, minSize));
        }
    }

    protected void coverChildrenForEmpty() {
        if (this.x.dependsOnChildren()) {
            this.x.coverChildrenForEmpty(this, getParent().getArea());
        }
        if (this.y.dependsOnChildren()) {
            this.y.coverChildrenForEmpty(this, getParent().getArea());
        }
    }

    @Override
    public void preApplyPos() {
        IWidget widget = getWidget();
        Area relativeTo = getParent().getArea();
        Area area = widget.getArea();
        if (isDecoration()) {
            setMarginPaddingApplied(true);
        } else {
            // apply margin and padding if not done yet
            this.x.applyMarginAndPaddingToPos(widget, area, relativeTo);
            this.y.applyMarginAndPaddingToPos(widget, area, relativeTo);
        }
        // after all widgets x, y, width and height have been calculated we can now calculate the absolute position
        area.applyPos(relativeTo.x, relativeTo.y);
    }

    @Override
    public void applyPos() {
        IWidget widget = getWidget();
        if (widget instanceof IDelegatingWidget dw && dw.getDelegate() != null) {
            super.postFullResize();
            return;
        }
        Area area = widget.getArea();
        // update rx and ry to be relative to the widget parent not the resize node parent
        Area parentArea = widget.getParentArea();
        area.rx = area.x - parentArea.x;
        area.ry = area.y - parentArea.y;
        if (widget instanceof IVanillaSlot vanillaSlot && vanillaSlot.handleAsVanillaSlot()) {
            // special treatment for minecraft slots
            SlotAccessor slot = (SlotAccessor) vanillaSlot.getVanillaSlot();
            Area mainArea = widget.getScreen().getMainPanel().getArea();
            // in vanilla uis the position is relative to the gui area and size is 16 x 16
            // since our slots are 18 x 18 we need to offset by 1
            slot.gtceu$setX(widget.getArea().x - mainArea.x + 1);
            slot.gtceu$setY(widget.getArea().y - mainArea.y + 1);
        }
    }

    @Override
    public void setChildrenResized(boolean resized) {
        this.childrenResized = resized;
    }

    @Override
    public void setLayoutDone(boolean done) {
        this.layoutResized = done;
    }

    @Override
    public void setXAxisResized(boolean pos, boolean size) {
        this.x.setResized(pos, size);
    }

    @Override
    public void setYAxisResized(boolean pos, boolean size) {
        this.y.setResized(pos, size);
    }

    @Override
    public void setXMarginPaddingApplied(boolean b) {
        this.x.setMarginPaddingApplied(b);
    }

    @Override
    public void setYMarginPaddingApplied(boolean b) {
        this.y.setMarginPaddingApplied(b);
    }

    @Override
    public boolean hasYPos() {
        return this.y.hasPos();
    }

    @Override
    public boolean hasXPos() {
        return this.x.hasPos();
    }

    @Override
    public boolean hasHeight() {
        return this.y.hasSize();
    }

    @Override
    public boolean hasWidth() {
        return this.x.hasSize();
    }

    @Override
    public boolean hasStartPos(GuiAxis axis) {
        return axis.isHorizontal() ? this.x.hasStart() : this.y.hasStart();
    }

    @Override
    public boolean hasEndPos(GuiAxis axis) {
        return axis.isHorizontal() ? this.x.hasEnd() : this.y.hasEnd();
    }

    @Override
    public boolean widthDependsOnParent() {
        return this.x.sizeDependsOnParent();
    }

    @Override
    public boolean heightDependsOnParent() {
        return this.y.sizeDependsOnParent();
    }

    @Override
    public boolean xDependsOnParent() {
        return this.x.posDependsOnParent();
    }

    @Override
    public boolean yDependsOnParent() {
        return this.y.posDependsOnParent();
    }

    @Override
    public boolean dependsOnChildrenX() {
        return this.x.dependsOnChildren();
    }

    @Override
    public boolean dependsOnChildrenY() {
        return this.y.dependsOnChildren();
    }

    public StandardResizer expanded() {
        return expanded(true);
    }

    @Override
    public StandardResizer expanded(boolean expanded) {
        this.expanded = expanded;
        scheduleResize();
        return this;
    }

    @Override
    public boolean hasFixedSize() {
        return this.x.hasFixedSize() && this.y.hasFixedSize();
    }

    @Override
    public boolean isFullSize() {
        if (!hasHeight() || !hasWidth()) return false;
        return this.x.isFullSize() && this.y.isFullSize();
    }

    @Override
    public StandardResizer relative(ResizeNode resizeNode) {
        setParentOverride(resizeNode);
        this.relativeToScreen = false;
        return this;
    }

    @Override
    public StandardResizer relativeToParent() {
        setParentOverride(null);
        this.relativeToScreen = false;
        return this;
    }

    @Override
    public StandardResizer relativeToScreen() {
        this.relativeToScreen = true;
        if (getParent() != null) {
            // if this is currently part of a tree, try to find the root and attach ourselves to it
            ScreenResizeNode root = TreeUtil.findParent(this, ScreenResizeNode.class);
            if (root != null) {
                setParentOverride(root);
            }
        }
        return this;
    }

    @Override
    public StandardResizer coverChildrenWidth(int minWidth) {
        this.x.setCoverChildren(minWidth, getWidget());
        return this;
    }

    @Override
    public StandardResizer coverChildrenHeight(int minHeight) {
        this.y.setCoverChildren(minHeight, getWidget());
        return this;
    }

    public StandardResizer decoration(boolean decoration) {
        this.decoration = decoration;
        return this;
    }

    @ApiStatus.Internal
    public StandardResizer left(float x, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getLeft(), x, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public StandardResizer left(DoubleSupplier x, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getLeft(), x, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public StandardResizer right(float x, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getRight(), x, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public StandardResizer right(DoubleSupplier x, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getRight(), x, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public StandardResizer top(float y, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getTop(), y, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public StandardResizer top(DoubleSupplier y, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getTop(), y, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public StandardResizer bottom(float y, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getBottom(), y, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public StandardResizer bottom(DoubleSupplier y, int offset, float anchor, Unit.Measure measure,
                                  boolean autoAnchor) {
        return unit(getBottom(), y, offset, anchor, measure, autoAnchor);
    }

    private StandardResizer unit(Unit u, float val, int offset, float anchor, Unit.Measure measure,
                                 boolean autoAnchor) {
        u.setValue(val);
        u.setMeasure(measure);
        u.setOffset(offset);
        u.setAnchor(anchor);
        u.setAutoAnchor(autoAnchor);
        scheduleResize();
        return this;
    }

    private StandardResizer unit(Unit u, DoubleSupplier val, int offset, float anchor, Unit.Measure measure,
                                 boolean autoAnchor) {
        u.setValue(val);
        u.setMeasure(measure);
        u.setOffset(offset);
        u.setAnchor(anchor);
        u.setAutoAnchor(autoAnchor);
        scheduleResize();
        return this;
    }

    @ApiStatus.Internal
    public StandardResizer width(float val, int offset, Unit.Measure measure) {
        return unitSize(getWidth(), val, offset, measure);
    }

    @ApiStatus.Internal
    public StandardResizer width(DoubleSupplier val, int offset, Unit.Measure measure) {
        return unitSize(getWidth(), val, offset, measure);
    }

    @ApiStatus.Internal
    public StandardResizer height(float val, int offset, Unit.Measure measure) {
        return unitSize(getHeight(), val, offset, measure);
    }

    @ApiStatus.Internal
    public StandardResizer height(DoubleSupplier val, int offset, Unit.Measure measure) {
        return unitSize(getHeight(), val, offset, measure);
    }

    private StandardResizer unitSize(Unit u, float val, int offset, Unit.Measure measure) {
        u.setValue(val);
        u.setMeasure(measure);
        u.setOffset(offset);
        scheduleResize();
        return this;
    }

    private StandardResizer unitSize(Unit u, DoubleSupplier val, int offset, Unit.Measure measure) {
        u.setValue(val);
        u.setMeasure(measure);
        u.setOffset(offset);
        scheduleResize();
        return this;
    }

    @Override
    public StandardResizer anchorLeft(float val) {
        getLeft().setMeasure(Unit.Measure.RELATIVE);
        getLeft().setAnchor(val);
        getLeft().setAutoAnchor(false);
        scheduleResize();
        return this;
    }

    @Override
    public StandardResizer anchorRight(float val) {
        getRight().setMeasure(Unit.Measure.RELATIVE);
        getRight().setAnchor(1 - val);
        getRight().setAutoAnchor(false);
        scheduleResize();
        return this;
    }

    @Override
    public StandardResizer anchorTop(float val) {
        getTop().setMeasure(Unit.Measure.RELATIVE);
        getTop().setAnchor(val);
        getTop().setAutoAnchor(false);
        scheduleResize();
        return this;
    }

    @Override
    public StandardResizer anchorBottom(float val) {
        getBottom().setMeasure(Unit.Measure.RELATIVE);
        getBottom().setAnchor(1 - val);
        getBottom().setAutoAnchor(false);
        scheduleResize();
        return this;
    }

    public void setUnit(Unit unit, GuiAxis axis, Unit.State pos) {
        (axis.isHorizontal() ? this.x : this.y).setUnit(unit, pos);
    }

    private Unit getLeft() {
        return this.x.getStart(getWidget());
    }

    private Unit getRight() {
        return this.x.getEnd(getWidget());
    }

    private Unit getTop() {
        return this.y.getStart(getWidget());
    }

    private Unit getBottom() {
        return this.y.getEnd(getWidget());
    }

    private Unit getWidth() {
        return this.x.getSize(getWidget());
    }

    private Unit getHeight() {
        return this.y.getSize(getWidget());
    }
}
