package com.gregtechceu.gtceu.api.mui.base.widget;

import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.sizer.*;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * Helper interface for position and size builder methods for widgets.
 *
 * @param <W> widget type
 */
@SuppressWarnings({ "unused", "UnusedReturnValue" })
public interface IPositioned<W extends IPositioned<W>> {

    StandardResizer resizer();

    Area getArea();

    void scheduleResize();

    boolean requiresResize();

    @SuppressWarnings("unchecked")
    default W getThis() {
        return (W) this;
    }

    default W coverChildrenWidth() {
        resizer().coverChildrenWidth();
        return getThis();
    }

    default W coverChildrenHeight() {
        resizer().coverChildrenHeight();
        return getThis();
    }

    default W coverChildren() {
        return coverChildrenWidth().coverChildrenHeight();
    }

    default W expanded() {
        resizer().expanded();
        return getThis();
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    default W relative(IGuiElement guiElement) {
        return relative(guiElement.getArea());
    }

    @Deprecated
    default W relative(Area area) {
        return relative(new AreaResizer(area));
    }

    default W relative(ResizeNode resizeNode) {
        resizer().relative(resizeNode);
        return getThis();
    }

    default W relative(IWidget widget) {
        return relative(widget.resizer());
    }

    default W relativeToScreen() {
        resizer().relativeToScreen();
        return getThis();
    }

    default W relativeToParent() {
        resizer().relativeToParent();
        return getThis();
    }

    default W left(int val) {
        resizer().left(val, 0, 0, Unit.Measure.PIXEL, true);
        return getThis();
    }

    default W leftRel(float val) {
        resizer().left(val, 0, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W leftRelOffset(float val, int offset) {
        resizer().left(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W leftRelAnchor(float val, float anchor) {
        resizer().left(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W leftRel(float val, int offset, float anchor) {
        resizer().left(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W left(float val, int offset, float anchor, Unit.Measure measure) {
        resizer().left(val, offset, anchor, measure, false);
        return getThis();
    }

    default W left(DoubleSupplier val, Unit.Measure measure) {
        resizer().left(val, 0, 0, measure, true);
        return getThis();
    }

    default W leftRelOffset(DoubleSupplier val, int offset) {
        resizer().left(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W leftRelAnchor(DoubleSupplier val, float anchor) {
        resizer().left(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W leftRel(DoubleSupplier val, int offset, float anchor) {
        resizer().left(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W right(int val) {
        resizer().right(val, 0, 0, Unit.Measure.PIXEL, true);
        return getThis();
    }

    default W rightRel(float val) {
        resizer().right(val, 0, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W rightRelOffset(float val, int offset) {
        resizer().right(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W rightRelAnchor(float val, float anchor) {
        resizer().right(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W rightRel(float val, int offset, float anchor) {
        resizer().right(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W right(float val, int offset, float anchor, Unit.Measure measure) {
        resizer().right(val, offset, anchor, measure, false);
        return getThis();
    }

    default W right(DoubleSupplier val, Unit.Measure measure) {
        resizer().right(val, 0, 0, measure, true);
        return getThis();
    }

    default W rightRelOffset(DoubleSupplier val, int offset) {
        resizer().right(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W rightRelAnchor(DoubleSupplier val, float anchor) {
        resizer().right(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W rightRel(DoubleSupplier val, int offset, float anchor) {
        resizer().right(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W top(int val) {
        resizer().top(val, 0, 0, Unit.Measure.PIXEL, true);
        return getThis();
    }

    default W topRel(float val) {
        resizer().top(val, 0, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W topRelOffset(float val, int offset) {
        resizer().top(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W topRelAnchor(float val, float anchor) {
        resizer().top(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W topRel(float val, int offset, float anchor) {
        resizer().top(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W top(float val, int offset, float anchor, Unit.Measure measure) {
        resizer().top(val, offset, anchor, measure, false);
        return getThis();
    }

    default W top(DoubleSupplier val, Unit.Measure measure) {
        resizer().top(val, 0, 0, measure, true);
        return getThis();
    }

    default W topRelOffset(DoubleSupplier val, int offset) {
        resizer().top(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W topRelAnchor(DoubleSupplier val, float anchor) {
        resizer().top(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W topRel(DoubleSupplier val, int offset, float anchor) {
        resizer().top(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W bottom(int val) {
        resizer().bottom(val, 0, 0, Unit.Measure.PIXEL, true);
        return getThis();
    }

    default W bottomRel(float val) {
        resizer().bottom(val, 0, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W bottomRelOffset(float val, int offset) {
        resizer().bottom(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W bottomRelAnchor(float val, float anchor) {
        resizer().bottom(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W bottomRel(float val, int offset, float anchor) {
        resizer().bottom(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W bottom(float val, int offset, float anchor, Unit.Measure measure) {
        resizer().bottom(val, offset, anchor, measure, false);
        return getThis();
    }

    default W bottom(DoubleSupplier val, Unit.Measure measure) {
        resizer().bottom(val, 0, 0, measure, true);
        return getThis();
    }

    default W bottomRelOffset(DoubleSupplier val, int offset) {
        resizer().bottom(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    default W bottomRelAnchor(DoubleSupplier val, float anchor) {
        resizer().bottom(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W bottomRel(DoubleSupplier val, int offset, float anchor) {
        resizer().bottom(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    default W width(int val) {
        resizer().width(val, 0, Unit.Measure.PIXEL);
        return getThis();
    }

    default W widthRel(float val) {
        resizer().width(val, 0, Unit.Measure.RELATIVE);
        return getThis();
    }

    default W widthRelOffset(float val, int offset) {
        resizer().width(val, offset, Unit.Measure.RELATIVE);
        return getThis();
    }

    default W width(float val, Unit.Measure measure) {
        resizer().width(val, 0, measure);
        return getThis();
    }

    default W width(DoubleSupplier val, Unit.Measure measure) {
        resizer().width(val, 0, measure);
        return getThis();
    }

    default W height(int val) {
        resizer().height(val, 0, Unit.Measure.PIXEL);
        return getThis();
    }

    default W heightRel(float val) {
        resizer().height(val, 0, Unit.Measure.RELATIVE);
        return getThis();
    }

    default W height(float val, Unit.Measure measure) {
        resizer().height(val, 0, measure);
        return getThis();
    }

    default W heightRelOffset(DoubleSupplier val, int offset) {
        resizer().height(val, offset, Unit.Measure.RELATIVE);
        return getThis();
    }

    default W height(DoubleSupplier val, Unit.Measure measure) {
        resizer().height(val, 0, measure);
        return getThis();
    }

    default W pos(int x, int y) {
        left(x).top(y);
        return getThis();
    }

    default W posRel(float x, float y) {
        leftRel(x).topRel(y);
        return getThis();
    }

    default W size(int w, int h) {
        width(w).height(h);
        return getThis();
    }

    default W sizeRel(float w, float h) {
        widthRel(w).heightRel(h);
        return getThis();
    }

    default W size(int val) {
        return width(val).height(val);
    }

    default W sizeRel(float val) {
        return widthRel(val).heightRel(val);
    }

    default W fullWidth() {
        return widthRel(1f);
    }

    default W fullHeight() {
        return heightRel(1f);
    }

    default W full() {
        return widthRel(1f).heightRel(1f);
    }

    default W anchorLeft(float val) {
        resizer().anchorLeft(val);
        return getThis();
    }

    default W anchorRight(float val) {
        resizer().anchorRight(val);
        return getThis();
    }

    default W anchorTop(float val) {
        resizer().anchorTop(val);
        return getThis();
    }

    default W anchorBottom(float val) {
        resizer().anchorBottom(val);
        return getThis();
    }

    default W anchor(Alignment alignment) {
        resizer().anchor(alignment);
        return getThis();
    }

    default W alignX(float val) {
        leftRel(val).anchorLeft(val);
        return getThis();
    }

    default W alignX(Alignment alignment) {
        return alignX(alignment.x);
    }

    default W alignY(float val) {
        topRel(val).anchorTop(val);
        return getThis();
    }

    default W alignY(Alignment alignment) {
        return alignY(alignment.y);
    }

    default W align(Alignment alignment) {
        return alignX(alignment).alignY(alignment);
    }

    default W horizontalCenter() {
        return alignX(Alignment.CENTER);
    }

    default W verticalCenter() {
        return alignY(Alignment.CENTER);
    }

    default W center() {
        return align(Alignment.Center);
    }

    default W resizer(Consumer<StandardResizer> resizerConsumer) {
        resizerConsumer.accept(resizer());
        return getThis();
    }

    default W padding(int left, int right, int top, int bottom) {
        getArea().getPadding().all(left, right, top, bottom);
        scheduleResize();
        return getThis();
    }

    default W padding(int horizontal, int vertical) {
        getArea().getPadding().all(horizontal, vertical);
        scheduleResize();
        return getThis();
    }

    default W padding(int all) {
        getArea().getPadding().all(all);
        scheduleResize();
        return getThis();
    }

    default W paddingLeft(int val) {
        getArea().getPadding().left(val);
        scheduleResize();
        return getThis();
    }

    default W paddingRight(int val) {
        getArea().getPadding().right(val);
        scheduleResize();
        return getThis();
    }

    default W paddingTop(int val) {
        getArea().getPadding().top(val);
        scheduleResize();
        return getThis();
    }

    default W paddingBottom(int val) {
        getArea().getPadding().bottom(val);
        scheduleResize();
        return getThis();
    }

    default W margin(int left, int right, int top, int bottom) {
        getArea().getMargin().all(left, right, top, bottom);
        scheduleResize();
        return getThis();
    }

    default W margin(int horizontal, int vertical) {
        getArea().getMargin().all(horizontal, vertical);
        scheduleResize();
        return getThis();
    }

    default W margin(int all) {
        getArea().getMargin().all(all);
        scheduleResize();
        return getThis();
    }

    default W marginLeft(int val) {
        getArea().getMargin().left(val);
        scheduleResize();
        return getThis();
    }

    default W marginRight(int val) {
        getArea().getMargin().right(val);
        scheduleResize();
        return getThis();
    }

    default W marginTop(int val) {
        getArea().getMargin().top(val);
        scheduleResize();
        return getThis();
    }

    default W marginBottom(int val) {
        getArea().getMargin().bottom(val);
        scheduleResize();
        return getThis();
    }
}
