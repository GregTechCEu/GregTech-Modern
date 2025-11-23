package com.gregtechceu.gtceu.api.mui.base.drawable;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.drawable.HoverableIcon;
import com.gregtechceu.gtceu.api.mui.drawable.InteractableIcon;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Box;

import org.jetbrains.annotations.Nullable;

/**
 * A {@link IDrawable} with a fixed size.
 */
public interface IIcon extends IDrawable {

    /**
     * @return the drawable this icon wraps or null if it doesn't wrap anything
     */
    @Nullable
    IDrawable getWrappedDrawable();

    /**
     * @return width of this icon or 0 if the width should be dynamic
     */
    int getWidth();

    /**
     * @return height of this icon or 0 of the height should be dynamic
     */
    int getHeight();

    default int getSize(GuiAxis axis) {
        return axis.isHorizontal() ? getWidth() : getHeight();
    }

    @Override
    default int getDefaultWidth() {
        return getWrappedDrawable() != null ? getWrappedDrawable().getDefaultWidth() : 0;
    }

    @Override
    default int getDefaultHeight() {
        return getWrappedDrawable() != null ? getWrappedDrawable().getDefaultHeight() : 0;
    }

    /**
     * @return the margin of this icon. Only used if width or height is 0
     */
    Box getMargin();

    default IDrawable getRootDrawable() {
        IDrawable drawable = this;
        while (drawable instanceof IIcon icon) {
            drawable = icon.getWrappedDrawable();
            if (drawable == null) return icon;
        }
        return drawable;
    }

    /**
     * This returns a hoverable wrapper of this icon. This is only used in
     * {@link com.gregtechceu.gtceu.api.mui.drawable.text.RichText RichText}.
     * This allows this icon to have its own tooltip.
     */
    default HoverableIcon asHoverable() {
        return new HoverableIcon(this);
    }

    /**
     * This returns an interactable wrapper of this icon. This is only used in
     * {@link com.gregtechceu.gtceu.api.mui.drawable.text.RichText RichText}.
     * This allows this icon to be able to listen to clicks and other inputs.
     */
    default InteractableIcon asInteractable() {
        return new InteractableIcon(this);
    }

    IIcon EMPTY_2PX = EMPTY.asIcon().height(2);
}
