package com.gregtechceu.gtceu.api.mui.base.widget;

import com.gregtechceu.gtceu.api.mui.base.layout.IResizeable;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

/**
 * Base interface for gui elements. For example widgets.
 */
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

    IResizeable resizer();

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
     * Draws this element
     *
     * @param context gui context
     */
    void draw(ModularGuiContext context);

    /**
     * Called when the mouse enters the area of this element
     */
    default void onMouseStartHover() {}

    /**
     * Called when the mouse leaves the area of this element
     */
    default void onMouseEndHover() {}

    /**
     * @return if this widget is currently right below the mouse
     */
    default boolean isHovering() {
        return getScreen().getContext().isHovered(this);
    }

    /**
     *
     * @param ticks time in ticks
     * @return if this element is right below the mouse for a certain amount of time
     */
    default boolean isHoveringFor(int ticks) {
        return getScreen().getContext().isHoveredFor(this, ticks);
    }

    default boolean isBelowMouse() {
        IGuiElement hovered = getScreen().getContext().getHovered();
        if (hovered == null) return false;
        while (!(hovered instanceof ModularPanel)) {
            if (hovered == this) return true;
            hovered = hovered.getParent();
        }
        return hovered == this;
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
