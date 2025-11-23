package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.layout.IResizeable;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.base.value.IValue;
import com.gregtechceu.gtceu.api.mui.base.widget.*;
import com.gregtechceu.gtceu.api.mui.factory.GuiData;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeKey;
import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.ValueSyncHandler;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Flex;
import com.gregtechceu.gtceu.api.mui.widget.sizer.IUnResizeable;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A very modular implementation of {@link IWidget}. This is the base class for almost all UI elements.
 * This class is perfectly fine for displaying drawables (although {@link IDrawable.DrawableWidget DrawableWidget}
 * is preferred) or even nothing.
 * <p>
 * References to widgets should not be stored after the screen closed. While the screen is open its usually fine to
 * remove and a widget
 * as many times as you want.
 *
 * @param <W> the type of this widget. This is used for proper return types in builder like methodsY
 */
public class Widget<W extends Widget<W>> implements IWidget, IPositioned<W>, ITooltip<W>, ISynced<W> {

    // other
    @Nullable
    @Getter
    private String name;
    /**
     * Returns if this widget is currently enabled. Disabled widgets (and all its children) are not rendered and can't
     * be interacted with.
     */
    @Getter
    private boolean enabled = true;
    private int timeHovered = -1;
    private int timeBelowMouse = -1;
    @Getter
    private boolean excludeAreaInXei = false;
    // gui context
    /**
     * Returns if this widget is currently part of an open panel. Only if this is true information about parent, panel
     * and gui context can
     * be obtained.
     */
    @Getter
    private boolean valid = false;
    private IWidget parent = null;
    private ModularPanel panel = null;
    private ModularGuiContext context = null;
    // sizing
    /**
     * Returns the area of this widget. This contains information such as position, size, relative position to parent,
     * padding and margin.
     * Even tho this is a mutable object, you should refrain from modifying the values.
     */
    @Getter
    private final Area area = new Area();
    /**
     * Returns the flex of this widget. This is responsible for calculating size, pos and relative pos.
     * Originally this was intended to be modular for custom flex class. May come back to this in the future.
     * Same as {@link #flex()}.
     */
    @Getter
    private final Flex flex = new Flex(this);
    private IResizeable resizer = this.flex;

    private BiConsumer<W, IViewportStack> transform;
    private boolean requiresResize = false;
    // syncing
    /**
     * Returns the value handler of this widget. Value handlers can provide and update any kind of objects like numbers
     * and strings.
     * For example text fields uses this get the current set string and updates the string after it is unfocused.
     */
    @Getter
    @Nullable
    private IValue<?> value;
    @Nullable
    private String syncKey;
    /**
     * This is intended to only be used when building the main panel in methods like
     * {@link IUIHolder#buildUI(GuiData, com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager, com.gregtechceu.gtceu.client.mui.screen.UISettings)}
     * since it's called on server and client. Otherwise, this will not work.
     */
    @Setter
    @Nullable
    private SyncHandler syncHandler;
    // rendering
    /**
     * The current set background. This is not an accurate representation of what is actually being displayed currently.
     * Usually background is handled by the theme, which is when this is null.
     * Backgrounds are drawn in {@link IWidget#drawBackground(ModularGuiContext, WidgetThemeEntry)}.
     */
    @Getter
    @Nullable
    private IDrawable background = null;
    /**
     * The current set overlay. This is used when the widget is not hovered or no hovered overlay is set.
     * Overlays are drawn in {@link IWidget#drawOverlay(ModularGuiContext, WidgetThemeEntry)}.
     */
    @Getter
    @Nullable
    private IDrawable overlay = null;
    /**
     * The current set hover background. Usually this is handled by the theme.
     */
    @Getter
    @Nullable
    private IDrawable hoverBackground = null;
    /**
     * The current set hover overlay.
     */
    @Getter
    @Nullable
    private IDrawable hoverOverlay = null;
    @Getter
    @Nullable
    private RichTooltip tooltip;
    @Getter
    @Nullable
    private WidgetThemeKey<?> widgetThemeOverride = null;
    // listener
    @Nullable
    private List<IGuiAction> guiActionListeners; // TODO replace with proper event system
    @Getter
    @Nullable
    private Consumer<W> onUpdateListener;

    // -----------------
    // === Lifecycle ===
    // -----------------

    /**
     * Called when a panel is opened. Use {@link #onInit()} and {@link #afterInit()} for custom logic.
     *
     * @param parent the parent this element belongs to
     * @param late   true if this is called some time after the widget tree of the parent has been initialised
     */
    @ApiStatus.Internal
    @Override
    public void initialise(@NotNull IWidget parent, boolean late) {
        this.timeHovered = -1;
        this.timeBelowMouse = -1;
        if (!(this instanceof ModularPanel)) {
            this.parent = parent;
            this.panel = parent.getPanel();
            this.context = parent.getContext();
            getArea().setPanelLayer(this.panel.getArea().getPanelLayer());
            getArea().z(parent.getArea().z() + 1);
            if (this.guiActionListeners != null) {
                for (IGuiAction action : this.guiActionListeners) {
                    this.context.getScreen().registerGuiActionListener(action);
                }
            }
        }
        if (this.value != null && this.syncKey != null) {
            throw new IllegalStateException(
                    "Widget has a value and a sync key for a synced value. This is not allowed!");
        }
        this.valid = true;
        if (!getScreen().isClientOnly()) {
            initialiseSyncHandler(getScreen().getSyncManager(), late);
        }
        if (isExcludeAreaInXei()) {
            getContext().getXeiSettings().addExclusionArea(this);
        }
        onInit();
        if (hasChildren()) {
            for (IWidget child : getChildren()) {
                child.initialise(this, false);
            }
        }
        afterInit();
        onUpdate();
        this.requiresResize = false;
    }

    /**
     * Called after this widget is initialised and before the children are initialised.
     */
    @ApiStatus.OverrideOnly
    public void onInit() {}

    /**
     * Called after this widget is initialised and after the children are initialised.
     */
    @ApiStatus.OverrideOnly
    public void afterInit() {}

    /**
     * Retrieves, initialises and verifies a linked sync handler.
     * Custom logic should be handled in {@link #isValidSyncHandler(SyncHandler)}.
     */
    @Override
    public void initialiseSyncHandler(ModularSyncManager syncManager, boolean late) {
        if (this.syncKey != null) {
            this.syncHandler = syncManager.getSyncHandler(getPanel().getName(), this.syncKey);
        }
        if ((this.syncKey != null || this.syncHandler != null) && !isValidSyncHandler(this.syncHandler)) {
            String type = this.syncHandler == null ? null : this.syncHandler.getClass().getName();
            this.syncHandler = null;
            throw new IllegalStateException("SyncHandler of type " + type + " is not valid for " +
                    getClass().getName() + ", with key " + this.syncKey);
        }
        if (this.syncHandler instanceof ValueSyncHandler<?> valueSyncHandler &&
                valueSyncHandler.getChangeListener() == null) {
            valueSyncHandler.setChangeListener(this::markTooltipDirty);
        }
    }

    /**
     * Called when this widget is removed from the widget tree or after the panel is closed.
     * Overriding this is fine, but super must be called.
     */
    @MustBeInvokedByOverriders
    @Override
    public void dispose() {
        if (isValid()) {
            if (this.guiActionListeners != null) {
                for (IGuiAction action : this.guiActionListeners) {
                    this.context.getScreen().removeGuiActionListener(action);
                }
            }
            if (isExcludeAreaInXei()) {
                getContext().getXeiSettings().removeExclusionArea(this);
            }
        }
        if (hasChildren()) {
            for (IWidget child : getChildren()) {
                child.dispose();
            }
        }
        if (!(this instanceof ModularPanel)) {
            this.panel = null;
            this.parent = null;
            this.context = null;
        }
        this.timeHovered = -1;
        this.timeBelowMouse = -1;
        this.valid = false;
    }

    // -----------------
    // === Rendering ===
    // -----------------

    /**
     * Called directly before {@link IWidget#draw(ModularGuiContext, WidgetThemeEntry)}. Draws background textures.
     * It is highly recommended to at least replicate this behaviour when overriding.
     * Overriding {@link IWidget#draw(ModularGuiContext, WidgetThemeEntry)} for custom visuals is preferred.
     * If a parent of this widget is disabled, this widget will not be drawn.
     *
     * @param context     gui context
     * @param widgetTheme widget theme of this widget
     */
    @Override
    public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        IDrawable bg = getCurrentBackground(context.getTheme(), widgetTheme);
        if (bg != null) {
            bg.drawAtZero(context, getArea().width, getArea().height, getActiveWidgetTheme(widgetTheme, isHovering()));
        }
    }

    /**
     * Called between {@link IWidget#drawBackground(ModularGuiContext, WidgetThemeEntry)} and
     * {@link IWidget#drawOverlay(ModularGuiContext, WidgetThemeEntry)}.
     * Custom visuals should be drawn here. For example the {@link com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot
     * ItemSlot} draws its item
     * here. If a parent of this widget is disabled, this widget will not be drawn.
     *
     * @param context     gui context
     * @param widgetTheme widget theme
     */
    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {}

    /**
     * Called directly after {@link IWidget#draw(ModularGuiContext, WidgetThemeEntry)}. Draws overlay textures.
     * It is highly recommended to at least replicate this behaviour when overriding.
     * Overriding {@link #draw(ModularGuiContext, WidgetThemeEntry)} for custom visuals is preferred.
     * If a parent of this widget is disabled, this widget will not be drawn.
     *
     * @param context     gui context
     * @param widgetTheme widget theme
     */
    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        IDrawable bg = getCurrentOverlay(context.getTheme(), widgetTheme);
        if (bg != null) {
            bg.drawAtZeroPadded(context, getArea(), getActiveWidgetTheme(widgetTheme, isHovering()));
        }
    }

    /**
     * Called after every widget of every panel and screen has been drawn. This is usually used to draw a tooltip, which
     * is the default
     * behaviour. If a parent of this widget is disabled, this widget will not be drawn.
     *
     * @param context gui context
     */
    @Override
    public void drawForeground(ModularGuiContext context) {
        RichTooltip tooltip = getTooltip();
        if (tooltip != null && isHoveringFor(tooltip.showUpTimer())) {
            tooltip.draw(context);
        }
    }

    /**
     * Returns the actual currently displayed background.
     *
     * @param theme       current theme
     * @param widgetTheme widget theme which is used by this widget
     * @return currently displayed background
     */
    public IDrawable getCurrentBackground(ITheme theme, WidgetThemeEntry<?> widgetTheme) {
        if (isHovering()) {
            IDrawable hoverBackground = getHoverBackground();
            if (hoverBackground == null) hoverBackground = getActiveWidgetTheme(widgetTheme, true).getBackground();
            if (hoverBackground != null && hoverBackground != IDrawable.NONE) return hoverBackground;
        }
        IDrawable background = getBackground();
        return background == null ? getActiveWidgetTheme(widgetTheme, false).getBackground() : background;
    }

    /**
     * Returns the actual currently displayed overlay.
     *
     * @param theme       current theme
     * @param widgetTheme widget theme which is used by this widget
     * @return currently displayed background
     */
    public IDrawable getCurrentOverlay(ITheme theme, WidgetThemeEntry<?> widgetTheme) {
        IDrawable hoverBackground = getHoverOverlay();
        return hoverBackground != null && hoverBackground != IDrawable.NONE && isHovering() ? hoverBackground :
                getOverlay();
    }

    /**
     * @return the tooltip object of this widget and creates a new one if there is currently none.
     */
    @Override
    public @NotNull RichTooltip tooltip() {
        if (this.tooltip == null) {
            this.tooltip = new RichTooltip().parent(this);
        }
        return this.tooltip;
    }

    /**
     * Sets a tooltip object.
     *
     * @param tooltip new tooltip
     * @return this
     */
    @Override
    public W tooltip(RichTooltip tooltip) {
        this.tooltip = tooltip;
        return getThis();
    }

    public W invisible() {
        return background(IDrawable.EMPTY)
                .disableHoverBackground();
    }

    /**
     * Should be called when information which is displayed in the tooltip via
     * {@link ITooltip#tooltipDynamic(Consumer)}.
     * It will invalidate the current tooltip and be caused to rebuild.
     */
    @Override
    public void markTooltipDirty() {
        if (this.tooltip != null) {
            this.tooltip.markDirty();
        }
    }

    /**
     * Returns the widget theme this widget class would like to use. Overriding is fine.
     *
     * @param theme theme to get widget theme from
     * @return widget theme this widget wishes to use
     */
    @ApiStatus.OverrideOnly
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getFallback();
    }

    @ApiStatus.OverrideOnly
    protected WidgetTheme getActiveWidgetTheme(WidgetThemeEntry<?> widgetTheme, boolean hover) {
        return widgetTheme.getTheme(hover);
    }

    /**
     * Returns the actual used widget theme. Uses {@link #widgetTheme(String)} if it has been set, otherwise calls
     * {@link #getWidgetThemeInternal(ITheme)}
     *
     * @param theme theme to get widget theme from
     * @return widget theme this widget will use
     */
    @ApiStatus.NonExtendable
    @Override
    public final WidgetThemeEntry<?> getWidgetTheme(ITheme theme) {
        if (this.widgetThemeOverride != null) {
            return theme.getWidgetTheme(this.widgetThemeOverride);
        }
        return getWidgetThemeInternal(theme);
    }

    /**
     * Returns the actual used widget theme. Uses {@link #widgetTheme(String)} if it has been set, otherwise calls
     * {@link #getWidgetThemeInternal(ITheme)}
     *
     * @param theme        theme to get widget theme from
     * @param expectedType type of the widget theme to expect used for validation
     * @return widget theme this widget will use
     */
    @SuppressWarnings("unchecked")
    @ApiStatus.NonExtendable
    public final <T extends WidgetTheme> WidgetThemeEntry<T> getWidgetTheme(ITheme theme, Class<T> expectedType) {
        WidgetThemeEntry<?> entry = getWidgetTheme(theme);
        if (entry.getKey().isOfType(expectedType)) {
            return (WidgetThemeEntry<T>) entry;
        }
        throw new IllegalStateException(String.format(
                "Got widget theme with invalid type in widget '%s'. Got type '%s'" +
                        ", but expected type '%s'!",
                this, entry.getKey().getWidgetThemeType().getSimpleName(), expectedType.getSimpleName()));
    }

    /**
     * Sets a background override. Ideally this is set in the used theme. Also consider using
     * {@link #overlay(IDrawable...)} instead.
     * Using {@link IDrawable#EMPTY} will make the background invisible while still overriding the widget theme.
     * Background are drawn before the widget and overlays are drawn.
     *
     * @param background background to use.
     * @return this
     */
    public W background(IDrawable... background) {
        this.background = IDrawable.of(background);
        return getThis();
    }

    /**
     * Sets an overlay. Does not interfere with themes. Overlays are drawn after the widget and backgrounds.
     *
     * @param overlay overlay to use.
     * @return this
     */
    public W overlay(IDrawable... overlay) {
        this.overlay = IDrawable.of(overlay);
        return getThis();
    }

    /**
     * Sets a hover background override. Ideally this is set in the used theme. Also consider using
     * {@link #hoverOverlay(IDrawable...)} instead.
     * Using {@link IDrawable#EMPTY} will make the background invisible while still overriding the widget theme.
     * Background are drawn before the widget and overlays are drawn.
     * <p>
     * Following argument special cases should be considered:
     * <ul>
     * <li>{@code null} will fallback to {@link WidgetThemeEntry#getHoverTheme()}</li>
     * <li>{@link IDrawable#EMPTY} will make the hover background invisible</li>
     * <li>{@link IDrawable#NONE} will use the normal background instead (which is also achieved using
     * {@link #disableHoverBackground()})</li>
     * <li>multiple drawables, will result in them being drawn on top of each other in the order they are passed to the
     * method</li>
     * </ul>
     *
     * @param background hover background to use.
     * @return this
     */
    public W hoverBackground(IDrawable... background) {
        this.hoverBackground = IDrawable.of(background);
        return getThis();
    }

    /**
     * Sets a hover overlay.
     * Using {@link IDrawable#EMPTY} will make the background invisible while still overriding the widget theme.
     * Background are drawn before the widget and overlays are drawn.
     * <p>
     * Following argument special cases should be considered:
     * <ul>
     * <li>{@link IDrawable#EMPTY} will make the hover overlay invisible</li>
     * <li>{@code null} and {@link IDrawable#NONE} will use the normal overlay instead (which is also achieved using
     * {@link #disableHoverOverlay()})</li>
     * <li>multiple drawables, will result in them being drawn on top of each other in the order they are passed to the
     * method</li>
     * </ul>
     *
     * @param overlay hover overlay to use.
     * @return this
     */
    public W hoverOverlay(IDrawable... overlay) {
        this.hoverOverlay = IDrawable.of(overlay);
        return getThis();
    }

    /**
     * Forces the hover background to use the normal background instead.
     *
     * @return this
     */
    public W disableHoverBackground() {
        return hoverBackground(IDrawable.NONE);
    }

    /**
     * Forces the hover overlay to use the normal overlay instead.
     *
     * @return this
     */
    public W disableHoverOverlay() {
        return hoverOverlay(IDrawable.NONE);
    }

    /**
     * Sets an override widget theme. This will change of the appearance of this widget according to the widget theme.
     *
     * @param s id of the widget theme (see constants in {@link IThemeApi})
     * @return this
     */
    public W widgetTheme(String s) {
        WidgetThemeKey<?> widgetThemeKey = WidgetThemeKey.getFromFullName(s);
        if (widgetThemeKey == null) {
            throw new IllegalArgumentException("No widget theme for id '" + s + "' exists.");
        }
        return widgetTheme(widgetThemeKey);
    }

    /**
     * Sets an override widget theme. This will change of the appearance of this widget according to the widget theme.
     *
     * @param s id of the widget theme (see constants in {@link IThemeApi})
     * @return this
     */
    public W widgetTheme(WidgetThemeKey<?> s) {
        this.widgetThemeOverride = s;
        return getThis();
    }

    // --------------
    // === Events ===
    // --------------

    /**
     * Called once every tick (20 times per second). Overriding is fine, but super should be called. This will be called
     * even of the widget
     * is not enabled.
     * By default, this will invoke update listeners set via setters.
     */
    @MustBeInvokedByOverriders
    @Override
    public void onUpdate() {
        if (isHovering()) this.timeHovered++;
        if (isBelowMouse()) this.timeBelowMouse++;
        if (this.onUpdateListener != null) {
            this.onUpdateListener.accept(getThis());
        }
    }

    /**
     * Registers a gui action this widget can listen to. Gui action listeners can listen to several mouse and keyboard
     * input events.
     * The listeners are called first, before any widgets are interacted with. The listeners will always be called, even
     * if the widget
     * is disabled or not hovered!
     * <p>
     * Lambdas must be cast to the appropriate functional interface.
     * These actions are automatically unregistered when the widget is removed from the widget tree.
     *
     * @param action gui action to register
     * @return this
     */
    public W listenGuiAction(IGuiAction action) {
        if (this.guiActionListeners == null) {
            this.guiActionListeners = new ArrayList<>();
        }
        this.guiActionListeners.add(action);
        if (isValid()) {
            this.context.getScreen().registerGuiActionListener(action);
        }
        return getThis();
    }

    /**
     * Sets an update listener which is called once every tick even when this widget is disabled.
     *
     * @param listener update listener
     * @return this
     */
    public W onUpdateListener(Consumer<W> listener) {
        return onUpdateListener(listener, false);
    }

    /**
     * Sets an update listener which is called once every tick even when this widget is disabled.
     * If a listener is already set and {@code merge} is true, the listeners will be merged, so that both will be called
     * on tick.
     *
     * @param listener update listener
     * @return this
     */
    public W onUpdateListener(Consumer<W> listener, boolean merge) {
        if (merge && this.onUpdateListener != null) {
            final Consumer<W> oldListener = this.onUpdateListener;
            if (listener != null) {
                this.onUpdateListener = w -> {
                    oldListener.accept(w);
                    listener.accept(w);
                };
            }
        } else {
            this.onUpdateListener = listener;
        }
        return getThis();
    }

    /**
     * Sets a condition for when to enable/disable this widget. This register an update listener which checks the
     * condition every tick.
     * Careful not to overwrite this when calling {@link #onUpdateListener(Consumer)} afterward!
     *
     * @param condition condition when to enable this widget
     * @return this
     */
    public W setEnabledIf(Predicate<W> condition) {
        return onUpdateListener(w -> setEnabled(condition.test(w)), true);
    }

    // ----------------
    // === Resizing ===
    // ----------------

    @Override
    public int getDefaultWidth() {
        return isValid() ? getWidgetTheme(getContext().getTheme()).getTheme().getDefaultWidth() : 18;
    }

    @Override
    public int getDefaultHeight() {
        return isValid() ? getWidgetTheme(getContext().getTheme()).getTheme().getDefaultHeight() : 18;
    }

    @Override
    public void scheduleResize() {
        this.requiresResize = true;
    }

    @Override
    public boolean requiresResize() {
        return this.requiresResize;
    }

    @MustBeInvokedByOverriders
    @Override
    public void onResized() {
        this.requiresResize = false;
    }

    /**
     * Returns the flex of this widget. This is responsible for calculating size, pos and relative pos.
     * Originally this was intended to be modular for custom flex class. May come back to this in the future.
     * Same as {@link #getFlex()}.
     *
     * @return flex of this widget
     */
    @Override
    public Flex flex() {
        return getFlex();
    }

    /**
     * Returns the resizer of this widget. This is actually the field responsible for resizing this widget.
     * Within MUI this is always the same as {@link #flex()}. Custom resizer have not been tested.
     * The relevance of separating flex and resizer is left to be investigated in the future.
     *
     * @return the resizer of this widget
     */
    @NotNull
    @Override
    public IResizeable resizer() {
        return this.resizer;
    }

    /**
     * Sets the resizer of this widget, which is responsible for resizing this widget.
     * Within MUI this setter is never used. Custom resizer have not been tested.
     * The relevance of separating flex and resizer is left to be investigated in the future.
     *
     * @param resizer resizer
     */
    @ApiStatus.Experimental
    @Override
    public void resizer(IResizeable resizer) {
        this.resizer = resizer != null ? resizer : IUnResizeable.INSTANCE;
    }

    @Override
    public void transform(IViewportStack stack) {
        IWidget.super.transform(stack);
        if (this.transform != null) {
            this.transform.accept(getThis(), stack);
        }
    }

    public W transform(BiConsumer<W, IViewportStack> transform) {
        this.transform = transform;
        return getThis();
    }

    // -------------------
    // === Gui context ===
    // -------------------

    /**
     * Returns the screen of the panel of this widget is being opened in.
     *
     * @return the screen of this widget
     * @throws IllegalStateException if {@link #isValid()} returns false
     */
    @Override
    public ModularScreen getScreen() {
        return getPanel().getScreen();
    }

    /**
     * Returns the panel of this widget is being opened in.
     *
     * @return the screen of this widget
     * @throws IllegalStateException if {@link #isValid()} returns false
     */
    @Override
    public @NotNull ModularPanel getPanel() {
        if (!isValid()) {
            throw new IllegalStateException(this + " is not in a valid state!");
        }
        return this.panel;
    }

    /**
     * Returns the parent of this widget. If this is a {@link ModularPanel} this will always return null contrary to the
     * annotation.
     *
     * @return the screen of this widget
     * @throws IllegalStateException if {@link #isValid()} returns false
     */
    @Override
    public @NotNull IWidget getParent() {
        if (!isValid()) {
            throw new IllegalStateException(this + " is not in a valid state!");
        }
        return this.parent;
    }

    /**
     * Returns the gui context of the screen this widget is part of.
     *
     * @return the screen of this widget
     * @throws IllegalStateException if {@link #isValid()} returns false
     */
    @Override
    public ModularGuiContext getContext() {
        if (!isValid()) {
            throw new IllegalStateException(this + " is not in a valid state!");
        }
        return this.context;
    }

    /**
     * Used to set the gui context on panels internally.
     */
    @ApiStatus.Internal
    protected final void setContext(ModularGuiContext context) {
        this.context = context;
    }

    // ---------------
    // === Syncing ===
    // --------------

    /**
     * Returns if this widget has a valid sync handler.
     */
    @Override
    public boolean isSynced() {
        return this.syncHandler != null;
    }

    /**
     * Returns the sync handler of this widget.
     *
     * @throws IllegalStateException if this widget has no sync handler ({@link #isSynced()} returns false)
     */
    @Override
    public @NotNull SyncHandler getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised or not synced!");
        }
        return this.syncHandler;
    }

    /**
     * Sets a sync handler id. A sync handler with the same id must have been registered to the appropriate
     * {@link com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager PanelSyncManager} for this to work.
     * This method is preferred over setting a sync handler directly since this does not require the widget to be
     * defined on both sides.
     *
     * @param name sync handler key name
     * @param id   sync handler key id
     * @return this
     */
    @Override
    public W syncHandler(String name, int id) {
        this.syncKey = ModularSyncManager.makeSyncKey(name, id);
        return getThis();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (this.isValid() && getParent() instanceof INotifyEnabled notifyEnabled) {
                notifyEnabled.onChildChangeEnabled(this, enabled);
            }
        }
    }

    /**
     * Used for widgets to set a value handler. Can also be a sync handler
     */
    protected void setValue(IValue<?> value) {
        this.value = value;
        if (value instanceof SyncHandler syncHandler1) {
            setSyncHandler(syncHandler1);
        }
    }

    // -------------
    // === Other ===
    // -------------

    public W excludeAreaInXei() {
        return excludeAreaInXei(true);
    }

    public W excludeAreaInXei(boolean val) {
        this.excludeAreaInXei = val;
        if (isValid()) {
            getContext().getXeiSettings().addExclusionArea(this);
        }
        return getThis();
    }

    /**
     * Disables the widget from start. Useful inside widget tree creation, where widget references are usually not
     * stored.
     *
     * @return this
     */
    public W disabled() {
        setEnabled(false);
        return getThis();
    }

    @MustBeInvokedByOverriders
    @Override
    public void onMouseStartHover() {
        this.timeHovered = 0;
    }

    @MustBeInvokedByOverriders
    @Override
    public void onMouseEndHover() {
        this.timeHovered = -1;
    }

    @MustBeInvokedByOverriders
    @Override
    public void onMouseEnterArea() {
        this.timeBelowMouse = 0;
    }

    @MustBeInvokedByOverriders
    @Override
    public void onMouseLeaveArea() {
        this.timeBelowMouse = -1;
    }

    @Override
    public boolean isHoveringFor(int ticks) {
        return timeHovered >= ticks;
    }

    @Override
    public boolean isBelowMouseFor(int ticks) {
        return timeBelowMouse >= ticks;
    }

    public int getTicksHovered() {
        return timeHovered;
    }

    public int getTicksBelowMouse() {
        return timeBelowMouse;
    }

    @Override
    public Object getAdditionalHoverInfo(IViewportStack viewportStack, int mouseX, int mouseY) {
        if (this instanceof IDragResizeable dragResizeable) {
            return IDragResizeable.getDragResizeCorner(dragResizeable, getArea(), viewportStack, mouseX, mouseY);
        }
        return null;
    }

    /**
     * @deprecated this got renamed to name
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public W debugName(String name) {
        return name(name);
    }

    /**
     * This can be used to find the widget with various methods from {@link WidgetTree} from a parent.
     * The name is also included in {@link #toString()}.
     *
     * @param name debug name to use
     * @return this
     */
    public W name(String name) {
        this.name = name;
        return getThis();
    }

    /**
     * Returns this widget with proper generic type.
     *
     * @return this
     */
    @SuppressWarnings("unchecked")
    @Override
    public W getThis() {
        return (W) this;
    }

    /**
     * This is only used in {@link #toString()}.
     *
     * @return the simple class name or other fitting name
     */
    protected String getTypeName() {
        return getClass().getSimpleName();
    }

    /**
     * @return the simple class plus the debug name, if set
     */
    @Override
    public String toString() {
        if (getName() != null) {
            return getTypeName() + "#" + getName();
        }
        return getTypeName();
    }
}
