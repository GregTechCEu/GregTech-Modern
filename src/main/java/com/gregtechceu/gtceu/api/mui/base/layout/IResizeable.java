package com.gregtechceu.gtceu.api.mui.base.layout;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;

/**
 * An interface that handles resizing of widgets.
 * Usually this interface is not implemented by the users of this library or will even interact with it.
 */
public interface IResizeable extends IResizeParent {

    /**
     * Called once before resizing
     */
    void initResizing(boolean onOpen);

    /**
     * Resizes the given element
     *
     * @param isParentLayout if the parent is a layout widget
     * @return true if element is fully resized
     */
    boolean resize(boolean isParentLayout);

    /**
     * Called if {@link #resize(boolean)} returned false after children have been resized.
     *
     * @return if element is fully resized
     */
    boolean postResize();

    /**
     * Called after all elements in the tree are resized and the absolute positions needs to be calculated from the
     * relative position.
     */
    default void preApplyPos() {}

    /**
     * This converts the relative pos to resizer parent to relative pos to widget parent.
     */
    default void applyPos() {}

    void setChildrenResized(boolean resized);

    void setLayoutDone(boolean done);

    /**
     * Marks position and size as calculated.
     */
    void setResized(boolean x, boolean y, boolean w, boolean h);

    default void setPosResized(boolean x, boolean y) {
        setResized(x, y, isWidthCalculated(), isHeightCalculated());
    }

    default void setSizeResized(boolean w, boolean h) {
        setResized(isXCalculated(), isYCalculated(), w, h);
    }

    default void setXResized(boolean v) {
        setResized(v, isYCalculated(), isWidthCalculated(), isHeightCalculated());
    }

    default void setYResized(boolean v) {
        setResized(isXCalculated(), v, isWidthCalculated(), isHeightCalculated());
    }

    default void setPosResized(GuiAxis axis, boolean v) {
        if (axis.isHorizontal()) {
            setXResized(v);
        } else {
            setYResized(v);
        }
    }

    default void setWidthResized(boolean v) {
        setResized(isXCalculated(), isYCalculated(), v, isHeightCalculated());
    }

    default void setHeightResized(boolean v) {
        setResized(isXCalculated(), isYCalculated(), isWidthCalculated(), v);
    }

    default void setSizeResized(GuiAxis axis, boolean v) {
        if (axis.isHorizontal()) {
            setWidthResized(v);
        } else {
            setHeightResized(v);
        }
    }

    default void setResized(boolean b) {
        setResized(b, b, b, b);
    }

    default void updateResized() {
        setResized(isXCalculated(), isYCalculated(), isWidthCalculated(), isHeightCalculated());
    }

    /**
     * Sets if margin and padding on the x-axis is applied
     *
     * @param b true if margin and padding are applied
     */
    void setXMarginPaddingApplied(boolean b);

    /**
     * Sets if margin and padding on the y-axis is applied
     *
     * @param b true if margin and padding are applied
     */
    void setYMarginPaddingApplied(boolean b);

    default void setMarginPaddingApplied(boolean b) {
        setXMarginPaddingApplied(b);
        setYMarginPaddingApplied(b);
    }

    default void setMarginPaddingApplied(GuiAxis axis, boolean b) {
        if (axis.isHorizontal()) {
            setXMarginPaddingApplied(b);
        } else {
            setYMarginPaddingApplied(b);
        }
    }
}
