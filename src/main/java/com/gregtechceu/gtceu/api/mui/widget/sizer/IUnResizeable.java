package com.gregtechceu.gtceu.api.mui.widget.sizer;

import com.gregtechceu.gtceu.api.mui.base.layout.IResizeable;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiElement;

/**
 * A variation of {@link IResizeable} with default implementations which don't do anything
 */
public interface IUnResizeable extends IResizeable {

    IUnResizeable INSTANCE = new IUnResizeable() {

        @Override
        public boolean resize(IGuiElement guiElement) {
            return true;
        }

        @Override
        public Area getArea() {
            Area.SHARED.set(0, 0, 0, 0);
            return Area.SHARED;
        }
    };

    @Override
    default void initResizing() {}

    @Override
    default boolean postResize(IGuiElement guiElement) {
        return true;
    }

    @Override
    default boolean isXCalculated() {
        return true;
    }

    @Override
    default boolean isYCalculated() {
        return true;
    }

    @Override
    default boolean isWidthCalculated() {
        return true;
    }

    @Override
    default boolean isHeightCalculated() {
        return true;
    }

    @Override
    default void setResized(boolean x, boolean y, boolean w, boolean h) {}

    @Override
    default void setXMarginPaddingApplied(boolean b) {}

    @Override
    default void setYMarginPaddingApplied(boolean b) {}

    @Override
    default boolean isXMarginPaddingApplied() {
        return true;
    }

    @Override
    default boolean isYMarginPaddingApplied() {
        return true;
    }
}
