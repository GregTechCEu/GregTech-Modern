package com.gregtechceu.gtceu.api.mui.base.widget;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.layout.IResizeable;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.utils.Point;
import com.gregtechceu.gtceu.api.mui.utils.Stencil;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Flex;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.google.common.base.CharMatcher;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * A widget in a GUI
 */
public interface IWidget extends IGuiElement {

    String WIDGET_TRANSLATION_KEY_FORMAT = "widget.%s.name";
    /**
     * This char matcher is used to remove any non-{@code [a-z0-9_.-]} characters in translation keys.
     * In essence, it
     */
    CharMatcher DISALLOWED_TRANSLATION_KEY_CHARS = CharMatcher.inRange('a', 'z')
            .or(CharMatcher.inRange('0', '9'))
            .or(CharMatcher.anyOf("-_."))
            .negate();

    /**
     * Validates and initialises this element.
     * This element now becomes valid
     *
     * @param parent the parent this element belongs to
     * @param late   true if this is called some time after the widget tree of the parent has been initialised
     */
    void initialise(@NotNull IWidget parent, boolean late);

    /**
     * Invalidates this element.
     */
    void dispose();

    /**
     * Determines if this element exist in an active gui.
     *
     * @return if this is in a valid gui
     */
    boolean isValid();

    /**
     * Draws the background of this widget.
     *
     * @param context     gui context
     * @param widgetTheme widget theme of this widget
     */
    void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme);

    /**
     * Draws additional stuff in this widget.
     * x = 0 and y = 0 is now in the top left corner of this widget.
     * Do NOT override this method, it is never called. Use {@link #draw(ModularGuiContext, WidgetThemeEntry)} instead.
     *
     * @param context gui context
     */
    @ApiStatus.NonExtendable
    @Deprecated
    @Override
    default void draw(ModularGuiContext context) {
        draw(context, getWidgetTheme(context.getTheme()));
    }

    /**
     * Draws extra elements of this widget. Called after {@link #drawBackground(ModularGuiContext, WidgetThemeEntry)}
     * and
     * before
     * {@link #drawOverlay(ModularGuiContext, WidgetThemeEntry)}
     *
     * @param context     gui context
     * @param widgetTheme widget theme
     */
    void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme);

    /**
     * Draws the overlay of this widget.
     *
     * @param context     gui context
     * @param widgetTheme widget theme
     */
    void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme);

    /**
     * Draws foreground elements of this widget. For example tooltips.
     * No transformations are applied here.
     *
     * @param context gui context
     */
    void drawForeground(ModularGuiContext context);

    default void transform(IViewportStack stack) {
        stack.translate(getArea().rx, getArea().ry, getArea().getPanelLayer() * 20);
    }

    default Object getAdditionalHoverInfo(IViewportStack viewportStack, int mouseX, int mouseY) {
        return null;
    }

    default WidgetThemeEntry<?> getWidgetTheme(ITheme theme) {
        return theme.getFallback();
    }

    /**
     * Called 20 times per second.
     */
    void onUpdate();

    /**
     * @return the area this widget occupies
     */
    @Override
    Area getArea();

    default String getTranslationId() {
        String className = FormattingUtil.toLowerCaseUnderscore(this.getClass().getSimpleName());
        className = DISALLOWED_TRANSLATION_KEY_CHARS.removeFrom(className);
        return WIDGET_TRANSLATION_KEY_FORMAT.formatted(className);
    }

    /**
     * Calculates if a given pos is inside this widgets area.
     * This should be used over {@link Area#isInside(int, int)}, since this accounts for transformations.
     *
     * @param stack viewport stack
     * @param mx    x pos
     * @param my    y pos
     * @return if pos is inside this widgets area
     */
    default boolean isInside(IViewportStack stack, int mx, int my) {
        return isInside(stack, mx, my, true);
    }

    /**
     * Calculates if a given pos is inside this widgets area.
     * This should be used over {@link Area#isInside(int, int)}, since this accounts for transformations.
     *
     * @param stack    viewport stack
     * @param mx       x pos
     * @param my       y pos
     * @param absolute true if the position is absolute or relative to the current stack transform otherwise
     * @return if pos is inside this widgets area
     */
    default boolean isInside(IViewportStack stack, int mx, int my, boolean absolute) {
        int x = mx;
        int y = my;
        if (absolute) {
            x = stack.unTransformX(mx, my);
            y = stack.unTransformY(mx, my);
        }
        return x >= 0 && x < getArea().w() && y >= 0 && y < getArea().h();
    }

    /**
     * Calculates if a given pos is inside this widgets area.
     * This should be used over {@link Area#isInside(int, int)}, since this accounts for transformations.
     *
     * @param stack viewport stack
     * @param point position
     * @return if pos is inside this widgets area
     */
    default boolean isInside(IViewportStack stack, Point point) {
        return isInside(stack, point.x, point.y);
    }

    /**
     * @return all children of this widget
     */
    @NotNull
    default List<IWidget> getChildren() {
        return Collections.emptyList();
    }

    /**
     * @return if this widget has any children
     */
    default boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    /**
     * @return the panel this widget is in
     */
    @NotNull
    ModularPanel getPanel();

    /**
     * Returns if this element is enabled. Disabled elements are not drawn and can not be interacted with. If this is
     * disabled, the children
     * will be considered disabled to without actually being disabled.
     *
     * @return if this element is enabled
     */
    @Override
    boolean isEnabled();

    void setEnabled(boolean enabled);

    /**
     * Checks if all ancestors are enabled. Only then this widget is visible and interactable.
     *
     * @return if all ancestors are enabled.
     */
    default boolean areAncestorsEnabled() {
        IWidget parent = this;
        do {
            if (!parent.isEnabled()) return false;
            parent = parent.getParent();
        } while (parent.hasParent());
        return true;
    }

    /**
     * If this widget can be seen on the screen even partly. If this returns false it will be culled. This is visually
     * only!
     *
     * @param stack viewport stack
     * @return false if this widget can not be seen currently and should not be drawn
     */
    default boolean canBeSeen(IViewportStack stack) {
        return Stencil.isInsideScissorArea(getArea(), stack);
    }

    /**
     * Determines if this widget can have any hover interaction. Interactions with mouse or keyboard like clicks ignore
     * this.
     * This is useful, when you have a widget which changes its background when hovered or has a tooltip and some
     * decoration child. Normally
     * you can click through the child, but while you hover it the widget will not show its tooltip etc. To change that
     * return false here.
     *
     * @return if this widget can have any hover interaction
     */
    default boolean canHover() {
        return true;
    }

    /**
     * Determines if widgets below this can receive a click callback. This is only called when this widget didn't
     * consume the click.
     *
     * @return if widgets below this should be able to receive a click
     */
    default boolean canClickThrough() {
        return true;
    }

    default boolean canHoverThrough() {
        return false;
    }

    /**
     * Marks tooltip for this widget as dirty.
     */
    void markTooltipDirty();

    /**
     * @return the parent of this widget
     */
    @NotNull
    IWidget getParent();

    @Override
    default boolean hasParent() {
        return isValid();
    }

    /**
     * @return the context of the current screen
     */
    ModularGuiContext getContext();

    /**
     * @return flex of this widget. Creates a new one if it doesn't already have one.
     */
    Flex flex();

    /**
     * Does the same as {@link IPositioned#flex(Consumer)}
     *
     * @param builder function to build flex
     * @return this
     */
    default IWidget flexBuilder(Consumer<Flex> builder) {
        builder.accept(flex());
        return this;
    }

    /**
     * @return resizer of this widget
     */
    @NotNull
    @Override
    IResizeable resizer();

    /**
     * Sets the resizer of this widget.
     *
     * @param resizer resizer
     */
    void resizer(IResizeable resizer);

    /**
     * Called before a widget is resized.
     */
    default void beforeResize(boolean onOpen) {}

    /**
     * Called after a widget is fully resized.
     */
    default void onResized() {}

    /**
     * Called after the full widget tree is resized and the absolute positions are calculated.
     */
    default void postResize() {}

    /**
     * @return flex of this widget
     */
    Flex getFlex();

    default boolean isExpanded() {
        Flex flex = getFlex();
        return flex != null && flex.isExpanded();
    }

    @Nullable
    String getName();

    default boolean isName(String name) {
        return name.equals(getName());
    }

    default boolean isType(Class<? extends IWidget> type) {
        return type.isAssignableFrom(getClass());
    }

    default boolean isNameAndType(String name, Class<? extends IWidget> type) {
        return isName(name) && isType(type);
    }
}
