package com.gregtechceu.gtceu.api.mui.base.layout;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.widget.INotifyEnabled;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;

/**
 * This is responsible for laying out widgets. This method responsible for laying out its children
 * in itself. This includes calling {@link IResizeable#setSizeResized(boolean, boolean)} or one of its variants after a
 * size with
 * {@link com.gregtechceu.gtceu.api.mui.widget.sizer.Area#setSize(GuiAxis, int)} or one of its variants on each child.
 * The same goes for
 * position. If this widget also applies margin and padding (this is usually the case), then
 * {@link IResizeable#setMarginPaddingApplied(boolean)}
 * or one of its variants needs to be called to.
 */
public interface ILayoutWidget extends INotifyEnabled {

    /**
     * Called after the children tried to calculate their size.
     * Might be called multiple times.
     *
     * @return true if the layout was successful and no further iteration is needed.
     */
    boolean layoutWidgets();

    /**
     * Called after post calculation of this widget. The last call guarantees, that this widget is fully calculated.
     *
     * @return true if the layout was successful and no further iteration is needed
     */
    default boolean postLayoutWidgets() {
        return true;
    }

    /**
     * Called when determining wrapping size of this widget.
     * If this method returns true, size and margin of the queried child will be ignored for calculation.
     * Typically return true when the child is disabled and you want to collapse it for layout.
     * This method should also be used for layouting children with {@link #layoutWidgets} if it might return true.
     */
    default boolean shouldIgnoreChildSize(IWidget child) {
        return false;
    }

    @Override
    default void onChildChangeEnabled(IWidget child, boolean enabled) {
        layoutWidgets();
        postLayoutWidgets();
    }
}
