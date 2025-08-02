package com.gregtechceu.gtceu.api.mui.base.drawable;

import com.gregtechceu.gtceu.api.mui.drawable.HoverableIcon;
import com.gregtechceu.gtceu.api.mui.drawable.InteractableIcon;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Box;

/**
 * A {@link IDrawable} with a fixed size.
 */
public interface IIcon extends IDrawable {

    /**
     * @return width of this icon or 0 if the width should be dynamic
     */
    int getWidth();

    /**
     * @return height of this icon or 0 of the height should be dynamic
     */
    int getHeight();

    /**
     * @return the margin of this icon. Only used if width or height is 0
     */
    Box getMargin();

    default HoverableIcon asHoverable() {
        return new HoverableIcon(this);
    }

    default InteractableIcon asInteractable() {
        return new InteractableIcon(this);
    }

    IIcon EMPTY_2PX = EMPTY.asIcon().height(2);
}
