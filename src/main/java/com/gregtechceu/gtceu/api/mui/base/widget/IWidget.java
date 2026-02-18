package com.gregtechceu.gtceu.api.mui.base.widget;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.ITreeNode;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.utils.Stencil;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widget.sizer.StandardResizer;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.google.common.base.CharMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * A widget in a GUI.
 */
public interface IWidget extends ITreeNode<IWidget> {

    String WIDGET_TRANSLATION_KEY_FORMAT = "widget.%s.name";
    /**
     * This char matcher is used to remove any non-{@code [a-z0-9_.-]} characters in translation keys.
     * In essence, it
     */
    CharMatcher DISALLOWED_TRANSLATION_KEY_CHARS = CharMatcher.inRange('a', 'z')
            .or(CharMatcher.inRange('0', '9'))
            .or(CharMatcher.anyOf("-_."))
            .negate();

    default String getTranslationId() {
        String className = FormattingUtil.toLowerCaseUnderscore(this.getClass().getSimpleName());
        className = DISALLOWED_TRANSLATION_KEY_CHARS.removeFrom(className);
        return WIDGET_TRANSLATION_KEY_FORMAT.formatted(className);
    }

    /**
     * @return the screen this element is in
     */
    ModularScreen getScreen();

    /**
     * @return the parent of this widget
     */
    @NotNull
    @Override
    IWidget getParent();

    @Override
    default boolean hasParent() {
        return isValid();
    }

    /**
     * @return the context the current screen
     */
    ModularGuiContext getContext();

    /**
     * @return the panel this widget is in
     */
    @NotNull
    ModularPanel getPanel();

    /**
     * @return the area this widget occupies
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
     * Called when the mouse hovers this element. This means this element is directly below the mouse or there are
     * widgets in between which
     * all allow to pass hover through. This is not called when the element is at any point below the mouse.
     */
    default void onMouseStartHover() {}

    /**
     * Called when the mouse no longer hovers this element. This widget can still be below the mouse on some level.
     */
    default void onMouseEndHover() {}

    /**
     * Called when the mouse enters this elements area with any amount of widgets above it from the current panel.
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
     * Returns if this element is right blow the mouse for a certain amount of time
     *
     * @param ticks time in ticks
     * @return if this element is right blow the mouse for a certain amount of time
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
     * Called 20 times per second.
     */
    default void onUpdate() {}

    /**
     * Draws the background of this widget.
     *
     * @param context     gui context
     * @param widgetTheme widget theme of this widget
     */
    default void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {}

    /**
     * Draws extra elements of this widget. Called after {@link #drawBackground(ModularGuiContext, WidgetThemeEntry)}
     * and before
     * {@link #drawOverlay(ModularGuiContext, WidgetThemeEntry)}
     *
     * @param context     gui context
     * @param widgetTheme widget theme
     */
    default void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {}

    /**
     * Draws the overlay of this theme.
     *
     * @param context     gui context
     * @param widgetTheme widget theme
     */
    default void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {}

    /**
     * Draws foreground elements of this widget. For example tooltips.
     * No transformations are applied here.
     *
     * @param context gui context
     */
    default void drawForeground(ModularGuiContext context) {}

    default void transform(IViewportStack stack) {
        stack.translate(getArea().rx, getArea().ry, 0);
    }

    default Object getAdditionalHoverInfo(IViewportStack viewportStack, int mouseX, int mouseY) {
        return null;
    }

    default WidgetThemeEntry<?> getWidgetTheme(ITheme theme) {
        return theme.getFallback();
    }

    /**
     * @return all children of this widget
     */
    @NotNull
    @Override
    default List<IWidget> getChildren() {
        return Collections.emptyList();
    }

    /**
     * @return if this widget has any children
     */
    @Override
    default boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    void scheduleResize();

    boolean requiresResize();

    /**
     * @return resizer of this widget
     */
    @NotNull
    StandardResizer resizer();

    default IWidget resizerBuilder(Consumer<StandardResizer> builder) {
        builder.accept(resizer());
        return this;
    }

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
     * Returns if this element is enabled. Disabled elements are not drawn and can not be interacted with. If this is
     * disabled, the children
     * will be considered disabled to without actually being disabled.
     *
     * @return if this element is enabled
     */
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
