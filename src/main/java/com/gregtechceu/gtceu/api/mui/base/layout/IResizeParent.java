package com.gregtechceu.gtceu.api.mui.base.layout;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;

public interface IResizeParent {

    /**
     * @return area of the element
     */
    Area getArea();

    /**
     * @return true if the relative x position is calculated
     */
    boolean isXCalculated();

    /**
     * @return true if the relative y position is calculated
     */
    boolean isYCalculated();

    /**
     * @return true if the width is calculated
     */
    boolean isWidthCalculated();

    /**
     * @return true if the height is calculated
     */
    boolean isHeightCalculated();

    boolean areChildrenCalculated();

    boolean isLayoutDone();

    boolean canRelayout(boolean isParentLayout);

    default boolean isSizeCalculated(GuiAxis axis) {
        return axis.isHorizontal() ? isWidthCalculated() : isHeightCalculated();
    }

    default boolean isPosCalculated(GuiAxis axis) {
        return axis.isHorizontal() ? isXCalculated() : isYCalculated();
    }

    /**
     * @return true if the relative position and size are fully calculated
     */
    default boolean isSelfFullyCalculated(boolean isParentLayout) {
        return isSelfFullyCalculated() && !canRelayout(isParentLayout);
    }

    default boolean isSelfFullyCalculated() {
        return isXCalculated() && isYCalculated() && isWidthCalculated() && isHeightCalculated();
    }

    default boolean isFullyCalculated() {
        return isSelfFullyCalculated() && areChildrenCalculated() && isLayoutDone();
    }

    default boolean isFullyCalculated(boolean isParentLayout) {
        return isFullyCalculated() && !canRelayout(isParentLayout);
    }

    /**
     * @return true if margin and padding are applied on the x-axis
     */
    boolean isXMarginPaddingApplied();

    /**
     * @return true if margin and padding are applied on the y-axis
     */
    boolean isYMarginPaddingApplied();
}
