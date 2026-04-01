package com.gregtechceu.gtceu.api.mui.base.widget;

import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.sizer.*;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * Helper interface for position and size builder methods for widgets.
 *
 * @param <W> widget type
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface IPositioned<W extends IPositioned<W>> {

    /**
     * @return the resizer that handles resizing with the set properties
     */
    StandardResizer resizer();

    /**
     * @return the area on which the resizing is applied to
     */
    Area getArea();

    /**
     * Marks this widget to resize on next render frame.
     */
    void scheduleResize();

    /**
     * @return if this widget has a resize scheduled
     */
    boolean requiresResize();

    @SuppressWarnings("unchecked")
    default W getThis() {
        return (W) this;
    }

    /**
     * Calculate the width by covering all children of this widget with a default min size of 8.
     *
     * @see #coverChildrenWidth(int)
     */
    default W coverChildrenWidth() {
        return coverChildrenWidth(8);
    }

    /**
     * Calculate the height by covering all children of this widget with a default min size of 8.
     *
     * @see #coverChildrenHeight(int)
     */
    default W coverChildrenHeight() {
        return coverChildrenHeight(8);
    }

    /**
     * Calculate the width by covering all children of this widget. For this calculation the widget children are used NOT the resizer
     * children. If a child position/size depends on this widget, its position/size is ignored and calculated after this widgets size
     * is calculated. If all children depend on this widgets size, the min size is used.
     *
     * @param minWidth minimum width this widget can have, negative values disables cover children
     * @return this
     */
    default W coverChildrenWidth(int minWidth) {
        resizer().coverChildrenWidth(minWidth);
        return getThis();
    }

    /**
     * Calculate the height by covering all children of this widget. For this calculation the widget children are used NOT the resizer
     * children. If a child position/size depends on this widget, its position/size is ignored and calculated after this widgets size
     * is calculated. If all children depend on this widgets size, the min size is used.
     *
     * @param minHeight minimum height this widget can have, negative values disables cover children
     * @return this
     */
    default W coverChildrenHeight(int minHeight) {
        resizer().coverChildrenHeight(minHeight);
        return getThis();
    }

    /**
     * Calculate the width and height by covering all children of this widget with a default min size of 8.
     *
     * @see #coverChildren(int)
     */
    default W coverChildren() {
        return coverChildrenWidth().coverChildrenHeight();
    }

    /**
     * Calculate the width and height by covering all children of this widget. For this calculation the widget children are used NOT the resizer
     * children. If a child position/size depends on this widget, its position/size is ignored and calculated after this widgets size
     * is calculated. If all children depend on this widgets size, the min size is used.
     *
     * @param minSize minimum width and height this widget can have, negative values disables cover children
     * @return this
     */
    default W coverChildren(int minSize) {
        return coverChildren(minSize, minSize);
    }

    /**
     * Calculate the width and height by covering all children of this widget. For this calculation the widget children are used NOT the resizer
     * children. If a child position/size depends on this widget, its position/size is ignored and calculated after this widgets size
     * is calculated. If all children depend on this widgets size, the min size is used.
     *
     * @param minWidth  minimum width this widget can have, negative values disables cover children
     * @param minHeight minimum height this widget can have, negative values disables cover children
     * @return this
     */
    default W coverChildren(int minWidth, int minHeight) {
        return coverChildrenWidth(minWidth).coverChildrenHeight(minHeight);
    }

    /**
     * Disables calculating width by covering the children.
     *
     * @return this
     */
    default W disableCoverChildrenWidth() {
        return coverChildrenWidth(-1);
    }

    /**
     * Disables calculating height by covering the children.
     *
     * @return this
     */
    default W disableCoverChildrenHeight() {
        return coverChildrenWidth(-1);
    }

    /**
     * Disables calculating width and height by covering the children.
     *
     * @return this
     */
    default W disableCoverChildren() {
        return disableCoverChildrenWidth().disableCoverChildrenHeight();
    }

    /**
     * Sets if this resizer is decoration. Decoration will be ignored during coverChildren and margin/padding calculations.
     *
     * @param decoration true if this resizer is decoration
     * @return this
     */
    default W decoration(boolean decoration) {
        resizer().decoration(decoration);
        return getThis();
    }

    /**
     * @see #decoration(boolean)
     */
    default W decoration() {
        return decoration(true);
    }

    /**
     * Sets whether this widget should expand inside a {@link com.gregtechceu.gtceu.api.mui.widgets.layout.Flow Flow} widget. Expanded means it takes
     * as much space as possible on the main axis without overlapping with other children in the flow.
     *
     * @return this
     */
    default W expanded(boolean expanded) {
        resizer().expanded(expanded);
        return getThis();
    }

    /**
     * @see #expanded(boolean)
     */
    default W expanded() {
        return expanded(true);
    }

    @Deprecated
    default W relative(Area area) {
        return relative(new AreaResizer(area));
    }

    /**
     * Sets the resizer this widget should be relative to. That means all left, top, right, bottom, width and height properties will be
     * calculated based of the new resizer. By default, this is the widgets parent. Every resize node tree configuration is allowed as long
     * as there are no circular dependencies.
     *
     * @param resizeNode resizer parent override
     * @return this
     */
    default W relative(ResizeNode resizeNode) {
        resizer().relative(resizeNode);
        return getThis();
    }

    /**
     * @see #relative(ResizeNode)
     */
    default W relative(IWidget widget) {
        return relative(widget.resizer());
    }

    /**
     * @see #relative(ResizeNode)
     */
    default W relativeToScreen() {
        resizer().relativeToScreen();
        return getThis();
    }

    /**
     * Sets the parent resizer to the parent widget. This is the default behavior.
     *
     * @see #relative(ResizeNode)
     */
    default W relativeToParent() {
        resizer().relativeToParent();
        return getThis();
    }

    /**
     * Sets the distance from this widgets left edge to the resizers widget left edge in pixel.
     *
     * @param val left edge to parent left edge distance in pixel
     * @return this
     */
    default W left(int val) {
        resizer().left(val, 0, 0, Unit.Measure.PIXEL, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets left edge to the resizers widget left edge (usually between 0.0 and 1.0).
     * 0.0 means the left edge touches the parent left edge. 1.0 means the right edge touches the parent right edge.
     *
     * @param val relative left edge to parent left edge distance
     * @return this
     */
    default W leftRel(float val) {
        resizer().left(val, 0, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets left edge to the resizers widget left edge (usually between 0.0 and 1.0) with an offset.
     * 0.0 for the relative value means the left edge touches the parent left edge. 1.0 means the right edge touches the parent right edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     *
     * @param val    relative left edge to parent left edge distance
     * @param offset additional distance offset in pixel
     * @return this
     */
    default W leftRelOffset(float val, int offset) {
        resizer().left(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets left edge to the resizers widget left edge (usually between 0.0 and 1.0) with an anchor.
     * 0.0 for the relative value means the left edge touches the parent left edge. 1.0 means the right edge touches the parent right edge.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative left edge to parent left edge distance
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W leftRelAnchor(float val, float anchor) {
        resizer().left(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets left edge to the resizers widget left edge (usually between 0.0 and 1.0) with an offset
     * and an anchor.
     * 0.0 for the relative value means the left edge touches the parent left edge. 1.0 means the right edge touches the parent right edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative left edge to parent left edge distance
     * @param offset additional distance offset in pixel
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W leftRel(float val, int offset, float anchor) {
        resizer().left(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets left edge to the resizers widget left edge (usually between 0.0 and 1.0) with an offset
     * and an anchor.
     * 0.0 for the relative value means the left edge touches the parent left edge. 1.0 means the right edge touches the parent right edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val     left edge to parent left edge distance
     * @param offset  additional distance offset in pixel
     * @param anchor  determines the relative position which is used to calculate the actual position with the relative value
     * @param measure determines if the <code>val</code> param should be a relative value or in pixel, the anchor does nothing in pixel
     * @return this
     */
    default W left(float val, int offset, float anchor, Unit.Measure measure) {
        resizer().left(val, offset, anchor, measure, false);
        return getThis();
    }

    /**
     * Sets the distance from this widgets left edge to the resizers widget left edge in pixel or as a relative value.
     *
     * @param val     left edge to parent left edge distance function which is called on every resize
     * @param measure determines if the <code>val</code> param should be a relative value or in pixel
     * @return this
     */
    default W left(DoubleSupplier val, Unit.Measure measure) {
        resizer().left(val, 0, 0, measure, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets left edge to the resizers widget left edge (usually between 0.0 and 1.0) with an offset.
     * 0.0 for the relative value means the left edge touches the parent left edge. 1.0 means the right edge touches the parent right edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     *
     * @param val    relative left edge to parent left edge distance function which is called on every resize
     * @param offset additional distance offset in pixel
     * @return this
     */
    default W leftRelOffset(DoubleSupplier val, int offset) {
        resizer().left(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets left edge to the resizers widget left edge (usually between 0.0 and 1.0) with an anchor.
     * 0.0 for the relative value means the left edge touches the parent left edge. 1.0 means the right edge touches the parent right edge.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative left edge to parent left edge distance function which is called on every resize
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W leftRelAnchor(DoubleSupplier val, float anchor) {
        resizer().left(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets left edge to the resizers widget left edge (usually between 0.0 and 1.0) with an offset
     * and an anchor.
     * 0.0 for the relative value means the left edge touches the parent left edge. 1.0 means the right edge touches the parent right edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative left edge to parent left edge distance
     * @param offset additional distance offset in pixel
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W leftRel(DoubleSupplier val, int offset, float anchor) {
        resizer().left(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the distance from this widgets right edge to the resizers widget right edge in pixel.
     *
     * @param val right edge to parent right edge distance in pixel
     * @return this
     */
    default W right(int val) {
        resizer().right(val, 0, 0, Unit.Measure.PIXEL, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets right edge to the resizers widget right edge (usually between 0.0 and 1.0).
     * 0.0 means the right edge touches the parent right edge. 1.0 means the left edge touches the parent left edge.
     *
     * @param val relative right edge to parent right edge distance
     * @return this
     */
    default W rightRel(float val) {
        resizer().right(val, 0, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets right edge to the resizers widget right edge (usually between 0.0 and 1.0) with an offset.
     * 0.0 for the relative value means the right edge touches the parent right edge. 1.0 means the left edge touches the parent left edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     *
     * @param val    relative right edge to parent right edge distance
     * @param offset additional distance offset in pixel
     * @return this
     */
    default W rightRelOffset(float val, int offset) {
        resizer().right(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets right edge to the resizers widget right edge (usually between 0.0 and 1.0) with an anchor.
     * 0.0 for the relative value means the right edge touches the parent right edge. 1.0 means the left edge touches the parent left edge.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative right edge to parent right edge distance
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W rightRelAnchor(float val, float anchor) {
        resizer().right(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets right edge to the resizers widget right edge (usually between 0.0 and 1.0) with an offset
     * and an anchor.
     * 0.0 for the relative value means the right edge touches the parent right edge. 1.0 means the left edge touches the parent left edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative right edge to parent right edge distance
     * @param offset additional distance offset in pixel
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W rightRel(float val, int offset, float anchor) {
        resizer().right(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets right edge to the resizers widget right edge (usually between 0.0 and 1.0) with an offset
     * and an anchor.
     * 0.0 for the relative value means the right edge touches the parent right edge. 1.0 means the left edge touches the parent left edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val     right edge to parent right edge distance
     * @param offset  additional distance offset in pixel
     * @param anchor  determines the relative position which is used to calculate the actual position with the relative value
     * @param measure determines if the <code>val</code> param should be a relative value or in pixel, the anchor does nothing in pixel
     * @return this
     */
    default W right(float val, int offset, float anchor, Unit.Measure measure) {
        resizer().right(val, offset, anchor, measure, false);
        return getThis();
    }

    /**
     * Sets the distance from this widgets right edge to the resizers widget right edge in pixel or as a relative value.
     *
     * @param val     right edge to parent right edge distance function which is called on every resize
     * @param measure determines if the <code>val</code> param should be a relative value or in pixel
     * @return this
     */
    default W right(DoubleSupplier val, Unit.Measure measure) {
        resizer().right(val, 0, 0, measure, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets right edge to the resizers widget right edge (usually between 0.0 and 1.0) with an offset.
     * 0.0 for the relative value means the right edge touches the parent right edge. 1.0 means the left edge touches the parent left edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     *
     * @param val    relative right edge to parent right edge distance function which is called on every resize
     * @param offset additional distance offset in pixel
     * @return this
     */
    default W rightRelOffset(DoubleSupplier val, int offset) {
        resizer().right(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets right edge to the resizers widget right edge (usually between 0.0 and 1.0) with an anchor.
     * 0.0 for the relative value means the right edge touches the parent right edge. 1.0 means the left edge touches the parent left edge.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative right edge to parent right edge distance function which is called on every resize
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W rightRelAnchor(DoubleSupplier val, float anchor) {
        resizer().right(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets right edge to the resizers widget right edge (usually between 0.0 and 1.0) with an offset
     * and an anchor.
     * 0.0 for the relative value means the right edge touches the parent right edge. 1.0 means the left edge touches the parent left edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative right edge to parent right edge distance
     * @param offset additional distance offset in pixel
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W rightRel(DoubleSupplier val, int offset, float anchor) {
        resizer().right(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the distance from this widgets top edge to the resizers widget top edge in pixel.
     *
     * @param val top edge to parent top edge distance in pixel
     * @return this
     */
    default W top(int val) {
        resizer().top(val, 0, 0, Unit.Measure.PIXEL, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets top edge to the resizers widget top edge (usually between 0.0 and 1.0).
     * 0.0 means the top edge touches the parent top edge. 1.0 means the bottom edge touches the parent bottom edge.
     *
     * @param val relative top edge to parent top edge distance
     * @return this
     */
    default W topRel(float val) {
        resizer().top(val, 0, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets top edge to the resizers widget top edge (usually between 0.0 and 1.0) with an offset.
     * 0.0 for the relative value means the top edge touches the parent top edge. 1.0 means the bottom edge touches the parent bottom edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     *
     * @param val    relative top edge to parent top edge distance
     * @param offset additional distance offset in pixel
     * @return this
     */
    default W topRelOffset(float val, int offset) {
        resizer().top(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets top edge to the resizers widget top edge (usually between 0.0 and 1.0) with an anchor.
     * 0.0 for the relative value means the top edge touches the parent top edge. 1.0 means the bottom edge touches the parent bottom edge.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative top edge to parent top edge distance
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W topRelAnchor(float val, float anchor) {
        resizer().top(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets top edge to the resizers widget top edge (usually between 0.0 and 1.0) with an offset
     * and an anchor.
     * 0.0 for the relative value means the top edge touches the parent top edge. 1.0 means the bottom edge touches the parent bottom edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative top edge to parent top edge distance
     * @param offset additional distance offset in pixel
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W topRel(float val, int offset, float anchor) {
        resizer().top(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets top edge to the resizers widget top edge (usually between 0.0 and 1.0) with an offset
     * and an anchor.
     * 0.0 for the relative value means the top edge touches the parent top edge. 1.0 means the bottom edge touches the parent bottom edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val     top edge to parent top edge distance
     * @param offset  additional distance offset in pixel
     * @param anchor  determines the relative position which is used to calculate the actual position with the relative value
     * @param measure determines if the <code>val</code> param should be a relative value or in pixel, the anchor does nothing in pixel
     * @return this
     */
    default W top(float val, int offset, float anchor, Unit.Measure measure) {
        resizer().top(val, offset, anchor, measure, false);
        return getThis();
    }

    /**
     * Sets the distance from this widgets top edge to the resizers widget top edge in pixel or as a relative value.
     *
     * @param val     top edge to parent top edge distance function which is called on every resize
     * @param measure determines if the <code>val</code> param should be a relative value or in pixel
     * @return this
     */
    default W top(DoubleSupplier val, Unit.Measure measure) {
        resizer().top(val, 0, 0, measure, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets top edge to the resizers widget top edge (usually between 0.0 and 1.0) with an offset.
     * 0.0 for the relative value means the top edge touches the parent top edge. 1.0 means the bottom edge touches the parent bottom edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     *
     * @param val    relative top edge to parent top edge distance function which is called on every resize
     * @param offset additional distance offset in pixel
     * @return this
     */
    default W topRelOffset(DoubleSupplier val, int offset) {
        resizer().top(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets top edge to the resizers widget top edge (usually between 0.0 and 1.0) with an anchor.
     * 0.0 for the relative value means the top edge touches the parent top edge. 1.0 means the bottom edge touches the parent bottom edge.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative top edge to parent top edge distance function which is called on every resize
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W topRelAnchor(DoubleSupplier val, float anchor) {
        resizer().top(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets top edge to the resizers widget top edge (usually between 0.0 and 1.0) with an offset
     * and an anchor.
     * 0.0 for the relative value means the top edge touches the parent top edge. 1.0 means the bottom edge touches the parent bottom edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative top edge to parent top edge distance
     * @param offset additional distance offset in pixel
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W topRel(DoubleSupplier val, int offset, float anchor) {
        resizer().top(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the distance from this widgets bottom edge to the resizers widget bottom edge in pixel.
     *
     * @param val bottom edge to parent bottom edge distance in pixel
     * @return this
     */
    default W bottom(int val) {
        resizer().bottom(val, 0, 0, Unit.Measure.PIXEL, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets bottom edge to the resizers widget bottom edge (usually between 0.0 and 1.0).
     * 0.0 means the bottom edge touches the parent bottom edge. 1.0 means the top edge touches the parent top edge.
     *
     * @param val relative bottom edge to parent bottom edge distance
     * @return this
     */
    default W bottomRel(float val) {
        resizer().bottom(val, 0, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets bottom edge to the resizers widget bottom edge (usually between 0.0 and 1.0) with an offset.
     * 0.0 for the relative value means the bottom edge touches the parent bottom edge. 1.0 means the top edge touches the parent top edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     *
     * @param val    relative bottom edge to parent bottom edge distance
     * @param offset additional distance offset in pixel
     * @return this
     */
    default W bottomRelOffset(float val, int offset) {
        resizer().bottom(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets bottom edge to the resizers widget bottom edge (usually between 0.0 and 1.0) with an anchor.
     * 0.0 for the relative value means the bottom edge touches the parent bottom edge. 1.0 means the top edge touches the parent top edge.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative bottom edge to parent bottom edge distance
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W bottomRelAnchor(float val, float anchor) {
        resizer().bottom(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets bottom edge to the resizers widget bottom edge (usually between 0.0 and 1.0) with an offset
     * and an anchor.
     * 0.0 for the relative value means the bottom edge touches the parent bottom edge. 1.0 means the top edge touches the parent top edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative bottom edge to parent bottom edge distance
     * @param offset additional distance offset in pixel
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W bottomRel(float val, int offset, float anchor) {
        resizer().bottom(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets bottom edge to the resizers widget bottom edge (usually between 0.0 and 1.0) with an offset
     * and an anchor.
     * 0.0 for the relative value means the bottom edge touches the parent bottom edge. 1.0 means the top edge touches the parent top edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val     bottom edge to parent bottom edge distance
     * @param offset  additional distance offset in pixel
     * @param anchor  determines the relative position which is used to calculate the actual position with the relative value
     * @param measure determines if the <code>val</code> param should be a relative value or in pixel, the anchor does nothing in pixel
     * @return this
     */
    default W bottom(float val, int offset, float anchor, Unit.Measure measure) {
        resizer().bottom(val, offset, anchor, measure, false);
        return getThis();
    }

    /**
     * Sets the distance from this widgets bottom edge to the resizers widget bottom edge in pixel or as a relative value.
     *
     * @param val     bottom edge to parent bottom edge distance function which is called on every resize
     * @param measure determines if the <code>val</code> param should be a relative value or in pixel
     * @return this
     */
    default W bottom(DoubleSupplier val, Unit.Measure measure) {
        resizer().bottom(val, 0, 0, measure, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets bottom edge to the resizers widget bottom edge (usually between 0.0 and 1.0) with an offset.
     * 0.0 for the relative value means the left edge touches the parent left edge. 1.0 means the top edge touches the parent top edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     *
     * @param val    relative bottom edge to parent bottom edge distance function which is called on every resize
     * @param offset additional distance offset in pixel
     * @return this
     */
    default W bottomRelOffset(DoubleSupplier val, int offset) {
        resizer().bottom(val, offset, 0, Unit.Measure.RELATIVE, true);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets bottom edge to the resizers widget bottom edge (usually between 0.0 and 1.0) with an anchor.
     * 0.0 for the relative value means the bottom edge touches the parent left bottom. 1.0 means the top edge touches the parent top edge.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative bottom edge to parent bottom edge distance function which is called on every resize
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W bottomRelAnchor(DoubleSupplier val, float anchor) {
        resizer().bottom(val, 0, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the relative distance from this widgets bottom edge to the resizers widget bottom edge (usually between 0.0 and 1.0) with an offset
     * and an anchor.
     * 0.0 for the relative value means the bottom edge touches the parent bottom edge. 1.0 means the top edge touches the parent top edge.
     * The offset is in pixel and is added after the position with the relative value is calculated. The offset can be negative.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget. By default,
     * the anchor is equal to the relative value. The formular for calculating the position is
     * <code>parentSize * relativeValue - selfSize * anchor</code>.
     *
     * @param val    relative bottom edge to parent bottom edge distance
     * @param offset additional distance offset in pixel
     * @param anchor determines the relative position which is used to calculate the actual position with the relative value
     * @return this
     */
    default W bottomRel(DoubleSupplier val, int offset, float anchor) {
        resizer().bottom(val, offset, anchor, Unit.Measure.RELATIVE, false);
        return getThis();
    }

    /**
     * Sets the width of this widget in pixel.
     *
     * @param val width in pixel
     * @return this
     */
    default W width(int val) {
        resizer().width(val, 0, Unit.Measure.PIXEL);
        return getThis();
    }

    /**
     * Sets the relative width of this widget. 1.0 is the same size as the resizer parent.
     *
     * @param val relative width
     * @return this
     */
    default W widthRel(float val) {
        resizer().width(val, 0, Unit.Measure.RELATIVE);
        return getThis();
    }

    /**
     * Sets the relative width of this widget with an offset. 1.0 is the same size as the resizer parent.
     * The offset is in pixel and is added after the size with the relative value is calculated. The offset can be negative.
     *
     * @param val    relative width
     * @param offset additional size offset in pixel
     * @return this
     */
    default W widthRelOffset(float val, int offset) {
        resizer().width(val, offset, Unit.Measure.RELATIVE);
        return getThis();
    }

    /**
     * Sets the width of this widget in pixel or as a relative value.
     *
     * @param val     width
     * @param measure determines if the <code>val</code> param should be a relative value or in pixel
     * @return this
     */
    default W width(float val, Unit.Measure measure) {
        resizer().width(val, 0, measure);
        return getThis();
    }

    /**
     * Sets the width of this widget in pixel or as a relative value with an offset.
     * The offset is in pixel and is added after the size with the relative value is calculated. The offset can be negative.
     *
     * @param val    relative width function which is called on every resize
     * @param offset additional size offset in pixel
     * @return this
     */
    default W widthRelOffset(DoubleSupplier val, int offset) {
        resizer().width(val, offset, Unit.Measure.RELATIVE);
        return getThis();
    }

    /**
     * Sets the width of this widget in pixel or as a relative value.
     *
     * @param val     width function which is called on every resize
     * @param measure determines if the <code>val</code> param should be a relative value or in pixel
     * @return this
     */
    default W width(DoubleSupplier val, Unit.Measure measure) {
        resizer().width(val, 0, measure);
        return getThis();
    }

    /**
     * Sets the height of this widget in pixel.
     *
     * @param val height in pixel
     * @return this
     */
    default W height(int val) {
        resizer().height(val, 0, Unit.Measure.PIXEL);
        return getThis();
    }

    /**
     * Sets the relative height of this widget. 1.0 is the same size as the resizer parent.
     *
     * @param val relative height
     * @return this
     */
    default W heightRel(float val) {
        resizer().height(val, 0, Unit.Measure.RELATIVE);
        return getThis();
    }

    /**
     * Sets the width of this widget in pixel or as a relative value.
     *
     * @param val     height
     * @param measure determines if the <code>val</code> param should be a relative value or in pixel
     * @return this
     */
    default W height(float val, Unit.Measure measure) {
        resizer().height(val, 0, measure);
        return getThis();
    }

    /**
     * Sets the relative height of this widget with an offset. 1.0 is the same size as the resizer parent.
     * The offset is in pixel and is added after the size with the relative value is calculated. The offset can be negative.
     *
     * @param val    relative height
     * @param offset additional size offset in pixel
     * @return this
     */
    default W heightRelOffset(float val, int offset) {
        resizer().height(val, offset, Unit.Measure.RELATIVE);
        return getThis();
    }

    /**
     * Sets the width of this widget in pixel or as a relative value with an offset.
     * The offset is in pixel and is added after the size with the relative value is calculated. The offset can be negative.
     *
     * @param val    relative width function which is called on every resize
     * @param offset additional size offset in pixel
     * @return this
     */
    default W heightRelOffset(DoubleSupplier val, int offset) {
        resizer().height(val, offset, Unit.Measure.RELATIVE);
        return getThis();
    }

    /**
     * Sets the width of this widget in pixel or as a relative value.
     *
     * @param val     width function which is called on every resize
     * @param measure determines if the <code>val</code> param should be a relative value or in pixel
     * @return this
     */
    default W height(DoubleSupplier val, Unit.Measure measure) {
        resizer().height(val, 0, measure);
        return getThis();
    }

    /**
     * Sets the position in pixel. Combines {@link #left(int)} and {@link #top(int)}.
     *
     * @param x left edge to parent left edge distance in pixel
     * @param y top edge to parent top edge distance in pixel
     * @return this
     */
    default W pos(int x, int y) {
        left(x).top(y);
        return getThis();
    }

    /**
     * Sets the relative position. Combines {@link #leftRel(float)} and {@link #topRel(float)}.
     *
     * @param x relative left edge to parent left edge distance
     * @param y relative top edge to parent top edge distance
     * @return this
     */
    default W posRel(float x, float y) {
        leftRel(x).topRel(y);
        return getThis();
    }

    /**
     * Sets the relative position with an {@link Alignment}. Combines {@link #leftRel(float)} and {@link #topRel(float)}.
     *
     * @param alignment relative position
     * @return this
     */
    default W posRel(Alignment alignment) {
        leftRel(alignment.x).topRel(alignment.y);
        return getThis();
    }

    /**
     * Sets the width and height of this widget in pixel. Combines {@link #width(int)} and {@link #height(int)}.
     *
     * @param w width in pixel
     * @param h height in pixel
     * @return this
     */
    default W size(int w, int h) {
        width(w).height(h);
        return getThis();
    }

    /**
     * Sets the relative width and height of this widget. Combines {@link #widthRel(float)} and {@link #heightRel(float)}.
     *
     * @param w relative width
     * @param h relative height
     * @return this
     */
    default W sizeRel(float w, float h) {
        widthRel(w).heightRel(h);
        return getThis();
    }

    /**
     * Sets the width and height of this widget in pixel. Combines {@link #width(int)} and {@link #height(int)}.
     *
     * @param val width and height in pixel
     * @return this
     */
    default W size(int val) {
        return width(val).height(val);
    }

    /**
     * Sets the relative width and height of this widget. Combines {@link #widthRel(float)} and {@link #heightRel(float)}.
     *
     * @param val relative width and height
     * @return this
     */
    default W sizeRel(float val) {
        return widthRel(val).heightRel(val);
    }

    /**
     * Sets width to fill the parent.
     *
     * @return this
     */
    default W fullWidth() {
        return widthRel(1f);
    }

    /**
     * Sets height to fill the parent.
     *
     * @return this
     */
    default W fullHeight() {
        return heightRel(1f);
    }

    /**
     * Sets width and height to fill the parent.
     *
     * @return this
     */
    default W full() {
        return widthRel(1f).heightRel(1f);
    }

    /**
     * Sets the anchor on the left side of the widget.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget.
     *
     * @param val anchor on the left side
     * @return this
     */
    default W anchorLeft(float val) {
        resizer().anchorLeft(val);
        return getThis();
    }

    /**
     * Sets the anchor on the right side of the widget.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget.
     *
     * @param val anchor on the right side
     * @return this
     */
    default W anchorRight(float val) {
        resizer().anchorRight(val);
        return getThis();
    }

    /**
     * Sets the anchor on the top side of the widget.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget.
     *
     * @param val anchor on the top side
     * @return this
     */
    default W anchorTop(float val) {
        resizer().anchorTop(val);
        return getThis();
    }

    /**
     * Sets the anchor on the bottom side of the widget.
     * The anchor determines what relative position inside this widget is used to calculate the relative position of the widget.
     *
     * @param val anchor on the bottom side
     * @return this
     */
    default W anchorBottom(float val) {
        resizer().anchorBottom(val);
        return getThis();
    }

    /**
     * Sets the relative position to center this widget on the x-axis inside the resizers parent.
     *
     * @return this
     */
    default W horizontalCenter() {
        return leftRel(0.5f);
    }

    /**
     * Sets the relative position to center this widget on the y-axis inside the resizers parent.
     *
     * @return this
     */
    default W verticalCenter() {
        return topRel(0.5f);
    }

    /**
     * Sets the relative position to center this widget inside the resizers parent.
     *
     * @return this
     */
    default W center() {
        return horizontalCenter().verticalCenter();
    }

    /**
     * This method allows for more dynamic customization while still being inside the builder.
     *
     * @param resizerConsumer a function that is immediately called exactly once with this resizer
     * @return this
     */
    default W resizer(Consumer<StandardResizer> resizerConsumer) {
        resizerConsumer.accept(resizer());
        return getThis();
    }

    /**
     * Sets a padding on all edges individually in pixel.
     * Padding is the space between this widgets edge and the parents widget edge.
     *
     * @param left   left edge padding in pixel
     * @param right  right edge padding in pixel
     * @param top    top edge padding in pixel
     * @param bottom bottom edge padding in pixel
     * @return this
     */
    default W padding(int left, int right, int top, int bottom) {
        getArea().getPadding().all(left, right, top, bottom);
        scheduleResize();
        return getThis();
    }

    /**
     * Sets a padding on all edges in pixel.
     * Padding is the space between this widgets edge and the parents widget edge.
     *
     * @param horizontal left and right edge padding in pixel
     * @param vertical   top and bottom edge padding in pixel
     * @return this
     */
    default W padding(int horizontal, int vertical) {
        getArea().getPadding().all(horizontal, vertical);
        scheduleResize();
        return getThis();
    }

    /**
     * Sets a padding on all edges in pixel.
     * Padding is the space between this widgets edge and the parents widget edge.
     *
     * @param all left, right, top and bottom edge padding in pixel
     * @return this
     */
    default W padding(int all) {
        getArea().getPadding().all(all);
        scheduleResize();
        return getThis();
    }

    /**
     * Sets a padding on the left edge in pixel.
     * Padding is the space between this widgets edge and the parents widget edge.
     *
     * @param val left edge padding in pixel
     * @return this
     */
    default W paddingLeft(int val) {
        getArea().getPadding().left(val);
        scheduleResize();
        return getThis();
    }

    /**
     * Sets a padding on the right edge in pixel.
     * Padding is the space between this widgets edge and the parents widget edge.
     *
     * @param val right edge padding in pixel
     * @return this
     */
    default W paddingRight(int val) {
        getArea().getPadding().right(val);
        scheduleResize();
        return getThis();
    }

    /**
     * Sets a padding on the top edge in pixel.
     * Padding is the space between this widgets edge and the parents widget edge.
     *
     * @param val top edge padding in pixel
     * @return this
     */
    default W paddingTop(int val) {
        getArea().getPadding().top(val);
        scheduleResize();
        return getThis();
    }

    /**
     * Sets a padding on the bottom edge in pixel.
     * Padding is the space between this widgets edge and the parents widget edge.
     *
     * @param val bottom edge padding in pixel
     * @return this
     */
    default W paddingBottom(int val) {
        getArea().getPadding().bottom(val);
        scheduleResize();
        return getThis();
    }

    /**
     * Sets a margin on all edges individually in pixel.
     * Margin is the space between this widgets edge and a children widget edge.
     *
     * @param left   left edge padding in pixel
     * @param right  right edge padding in pixel
     * @param top    top edge padding in pixel
     * @param bottom bottom edge padding in pixel
     * @return this
     */
    default W margin(int left, int right, int top, int bottom) {
        getArea().getMargin().all(left, right, top, bottom);
        scheduleResize();
        return getThis();
    }

    /**
     * Sets a margin on all edges in pixel.
     * Margin is the space between this widgets edge and a children widget edge.
     *
     * @param horizontal left and right edge margin in pixel
     * @param vertical   top and bottom edge margin in pixel
     * @return this
     */
    default W margin(int horizontal, int vertical) {
        getArea().getMargin().all(horizontal, vertical);
        scheduleResize();
        return getThis();
    }

    /**
     * Sets a margin on all edges in pixel.
     * Margin is the space between this widgets edge and a children widget edge.
     *
     * @param all left, right, top and bottom edge margin in pixel
     * @return this
     */
    default W margin(int all) {
        getArea().getMargin().all(all);
        scheduleResize();
        return getThis();
    }

    /**
     * Sets a margin on the left edge in pixel.
     * Margin is the space between this widgets edge and a children widget edge.
     *
     * @param val left edge margin in pixel
     * @return this
     */
    default W marginLeft(int val) {
        getArea().getMargin().left(val);
        scheduleResize();
        return getThis();
    }

    /**
     * Sets a margin on the right edge in pixel.
     * Margin is the space between this widgets edge and a children widget edge.
     *
     * @param val right edge margin in pixel
     * @return this
     */
    default W marginRight(int val) {
        getArea().getMargin().right(val);
        scheduleResize();
        return getThis();
    }

    /**
     * Sets a margin on the top edge in pixel.
     * Margin is the space between this widgets edge and a children widget edge.
     *
     * @param val top edge margin in pixel
     * @return this
     */
    default W marginTop(int val) {
        getArea().getMargin().top(val);
        scheduleResize();
        return getThis();
    }

    /**
     * Sets a margin on the bottom edge in pixel.
     * Margin is the space between this widgets edge and a children widget edge.
     *
     * @param val bottom edge margin in pixel
     * @return this
     */
    default W marginBottom(int val) {
        getArea().getMargin().bottom(val);
        scheduleResize();
        return getThis();
    }
}