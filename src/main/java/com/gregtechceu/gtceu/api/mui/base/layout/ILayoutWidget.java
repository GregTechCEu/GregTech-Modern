package com.gregtechceu.gtceu.api.mui.base.layout;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;

/**
 * This is responsible for laying out widgets.
 */
public interface ILayoutWidget {

    /**
     * Called after the children tried to calculate their size.
     * Might be called multiple times.
     */
    void layoutWidgets();

    /**
     * Called after post calculation of this widget.
     * Might be called multiple times.
     * The last call guarantees, that this widget is fully calculated.
     */
    default void postLayoutWidgets() {}

    /**
     * Called when determining wrapping size of this widget.
     * If this method returns true, size and margin of the queried child will be ignored for calculation.
     * Typically return true when the child is disabled and you want to collapse it for layout.
     * This method should also be used for layouting children with {@link #layoutWidgets} if it might return true.
     */
    default boolean shouldIgnoreChildSize(IWidget child) {
        return false;
    }
}
