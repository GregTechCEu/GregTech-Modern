package com.gregtechceu.gtceu.api.mui.base.widget;

import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widget.sizer.ResizeNode;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;

import org.jetbrains.annotations.ApiStatus;

/**
 * Base interface for gui elements. For example widgets.
 */
@ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
@Deprecated
public interface IGuiElement {

    /**
     * @return the screen this element is in
     */
    ModularScreen getScreen();

    /**
     * @return the parent of this element
     */
    IGuiElement getParent();

    /**
     * Returns if this element has a parent. This is the case when the widget is valid, but never if it's root widget.
     */
    boolean hasParent();

    ResizeNode resizer();

    /**
     * @return the area this element occupies
     */
    Area getArea();

    /**
     * Shortcut to get the area of the parent
     *
     * @return parent area
     */
    default Area getParentArea() {
        return getParent().getArea();
    }

    /**
     * Called when the mouse hovers this element. This means this element is directly below the mouse or there are
     * widgets in between which all allow to pass hover through. This is not called when the element is at any point
     * below the mouse.
     */
    default void onMouseStartHover() {}

    /**
     * Called when the mouse no longer hovers this element. This widget can still be below the mouse on some level.
     */
    default void onMouseEndHover() {}

    /**
     * Called when the mouse enters this element's area with any amount of widgets above it from the current panel.
     */
    default void onMouseEnterArea() {}

    /**
     * Called when the mouse leaves the area, or it started hovering a different panel.
     */
    default void onMouseLeaveArea() {}

    /**
     * @return if this widget is currently right below the mouse
     */
    default boolean isHovering() {
        return isHoveringFor(0);
    }

    /**
     *
     * @param ticks time in ticks
     * @return if this element is right below the mouse for a certain amount of time
     */
    default boolean isHoveringFor(int ticks) {
        return false;
    }

    default boolean isBelowMouse() {
        return isBelowMouseFor(0);
    }

    default boolean isBelowMouseFor(int ticks) {
        return false;
    }

    /**
     * Returns if this element is enabled. Disabled elements are not drawn and can not be interacted with.
     */
    boolean isEnabled();

    /**
     * @return default width if it can't be calculated
     */
    default int getDefaultWidth() {
        return 18;
    }

    /**
     * @return default height if it can't be calculated
     */
    default int getDefaultHeight() {
        return 18;
    }

    void scheduleResize();

    boolean requiresResize();
}
