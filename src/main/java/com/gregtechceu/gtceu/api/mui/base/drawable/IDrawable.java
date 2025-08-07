package com.gregtechceu.gtceu.api.mui.base.drawable;

import com.gregtechceu.gtceu.api.mui.drawable.DrawableStack;
import com.gregtechceu.gtceu.api.mui.drawable.Icon;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

/**
 * An object which can be drawn. This is mainly used for backgrounds and overlays in
 * {@link com.gregtechceu.gtceu.api.mui.base.widget.IWidget}.
 */
public interface IDrawable {

    static IDrawable of(IDrawable... drawables) {
        if (drawables == null || drawables.length == 0) {
            return null;
        } else if (drawables.length == 1) {
            return drawables[0];
        } else {
            return new DrawableStack(drawables);
        }
    }

    /**
     * Draws this drawable at the given position with the given size.
     *
     * @param context     current context to draw with
     * @param x           x position
     * @param y           y position
     * @param width       draw width
     * @param height      draw height
     * @param widgetTheme current theme
     */
    @OnlyIn(Dist.CLIENT)
    void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme);

    /**
     * Draws this drawable at the current (0|0) with the given size.
     *
     * @param context     gui context
     * @param width       draw width
     * @param height      draw height
     * @param widgetTheme current theme
     */
    @OnlyIn(Dist.CLIENT)
    default void drawAtZero(GuiContext context, int width, int height, WidgetTheme widgetTheme) {
        draw(context, 0, 0, width, height, widgetTheme);
    }

    /**
     * Draws this drawable in a given area.
     *
     * @param context     current context to draw with
     * @param area        draw area
     * @param widgetTheme current theme
     */
    @OnlyIn(Dist.CLIENT)
    default void draw(GuiContext context, Area area, WidgetTheme widgetTheme) {
        draw(context, area.x + area.getPadding().left, area.y + area.getPadding().top, area.paddedWidth(),
                area.paddedHeight(), widgetTheme);
    }

    /**
     * Draws this drawable at the current (0|0) with the given area's size.
     *
     * @param context     gui context
     * @param area        draw area
     * @param widgetTheme current theme
     */
    @OnlyIn(Dist.CLIENT)
    default void drawAtZero(GuiContext context, Area area, WidgetTheme widgetTheme) {
        draw(context, 0, 0, area.paddedWidth(), area.paddedHeight(), widgetTheme);
    }

    /**
     * @return if theme color can be applied on this drawable
     */
    default boolean canApplyTheme() {
        return false;
    }

    /**
     * @return a widget with this drawable as a background
     */
    default Widget<?> asWidget() {
        return new DrawableWidget(this);
    }

    /**
     * @return this drawable as an icon
     */
    default Icon asIcon() {
        return new Icon(this);
    }

    /**
     * An empty drawable. Does nothing.
     */
    IDrawable EMPTY = (context, x, y, width, height, widgetTheme) -> {};

    /**
     * An empty drawable used to mark hover textures as "should not be used"!
     */
    IDrawable NONE = (context, x, y, width, height, widgetTheme) -> {};

    static boolean isVisible(@Nullable IDrawable drawable) {
        if (drawable == null || drawable == EMPTY || drawable == NONE) return false;
        if (drawable instanceof DrawableStack array) {
            return array.getDrawables().length > 0;
        }
        return true;
    }

    /**
     * A widget wrapping a drawable. The drawable is drawn between the background and the overlay.
     */
    class DrawableWidget extends Widget<DrawableWidget> {

        private final IDrawable drawable;

        public DrawableWidget(IDrawable drawable) {
            this.drawable = drawable;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
            this.drawable.drawAtZero(context, getArea(), widgetTheme);
        }
    }
}
