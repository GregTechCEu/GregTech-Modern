package brachy.modularui.api.drawable;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.drawable.HoverableIcon;
import brachy.modularui.drawable.InteractableIcon;
import brachy.modularui.drawable.text.RichText;
import brachy.modularui.widget.sizer.Box;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

import org.jetbrains.annotations.Nullable;

/**
 * A {@link IDrawable} with a fixed size.
 */
public interface IIcon extends IDrawable, TooltipComponent {

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
     * {@link RichText}.
     * This allows this icon to have its own tooltip.
     */
    default HoverableIcon asHoverable() {
        return new HoverableIcon(this);
    }

    /**
     * This returns an interactable wrapper of this icon. This is only used in
     * {@link RichText}.
     * This allows this icon to be able to listen to clicks and other inputs.
     */
    default InteractableIcon asInteractable() {
        return new InteractableIcon(this);
    }

    IIcon EMPTY_2PX = EMPTY.asIcon().height(2);
}
