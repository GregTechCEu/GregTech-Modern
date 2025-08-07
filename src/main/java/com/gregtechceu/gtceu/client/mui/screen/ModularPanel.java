package com.gregtechceu.gtceu.client.mui.screen;

import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewport;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.base.widget.IFocusedWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Animator;
import com.gregtechceu.gtceu.api.mui.utils.HoveredWidgetList;
import com.gregtechceu.gtceu.api.mui.utils.Interpolation;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiViewportStack;
import com.gregtechceu.gtceu.client.mui.screen.viewport.LocatedWidget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraft.Util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * This class is like a window in windows. It can hold any amount of widgets. It may also be draggable.
 * To open another panel on top of the main panel you must use
 * {@link IPanelHandler#simple(ModularPanel, SecondaryPanel.IPanelBuilder, boolean)}
 * or {@link PanelSyncManager#panel(String, PanelSyncHandler.IPanelBuilder, boolean)} if the panel should be synced.
 */
public class ModularPanel extends ParentWidget<ModularPanel> implements IViewport {

    public static ModularPanel defaultPanel(@NotNull String name) {
        return defaultPanel(name, 176, 166);
    }

    public static ModularPanel defaultPanel(@NotNull String name, int width, int height) {
        return new ModularPanel(name).size(width, height);
    }

    private static final int tapTime = 200;

    @Getter
    private final @NotNull String name;
    private ModularScreen screen;
    @Setter
    private IPanelHandler panelHandler;
    @Getter
    private State state = State.IDLE;
    private boolean cantDisposeNow = false;
    @Getter
    private final @NotNull ObjectArrayList<LocatedWidget> hovering = new ObjectArrayList<>();
    private final Input keyboard = new Input();
    private final Input mouse = new Input();

    private final List<IPanelHandler> clientSubPanels = new ArrayList<>();
    private boolean invisible = false;
    private Animator animator;
    @Getter
    private float scale = 1f;
    @Getter
    private float alpha = 1f;

    public ModularPanel(@NotNull String name) {
        this.name = Objects.requireNonNull(name, "A panels name must not be null and should be unique!");
        center();
    }

    @Override
    public @NotNull ModularPanel getPanel() {
        return this;
    }

    @Override
    public Area getParentArea() {
        return getScreen().getScreenArea();
    }

    @Override
    public void onInit() {
        getScreen().registerFrameUpdateListener(this, this::findHoveredWidgets, false);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof IPanelHandler;
    }

    /**
     * @return true if this panel is currently open on a screen
     */
    public boolean isOpen() {
        return this.state == State.OPEN;
    }

    /**
     * If this panel is open it will be closed.
     * If animating is enabled and an animation is already playing this method will do nothing.
     *
     * @param animate true if the closing animation should play first.
     */
    public void closeIfOpen(boolean animate) {
        if (!isOpen()) return;
        closeSubPanels();
        if (!animate || !shouldAnimate()) {
            this.screen.getPanelManager().closePanel(this);
            return;
        }
        if (!isOpening() && !isClosing()) {
            if (isMainPanel()) {
                // if this is the main panel, start closing animation for all panels
                for (ModularPanel panel : getScreen().getPanelManager().getOpenPanels()) {
                    if (!panel.isMainPanel()) {
                        panel.closeIfOpen(true);
                    }
                }
            }
            getAnimator().setEndCallback(val -> this.screen.getPanelManager().closePanel(this)).backward();
        }
    }

    protected void closeSubPanels() {
        if (this.panelHandler != null) {
            this.panelHandler.closeSubPanels();
        }
    }

    public void animateClose() {
        closeIfOpen(true);
    }

    @Override
    public boolean hasParent() {
        return false;
    }

    @Override
    public WidgetTheme getWidgetThemeInternal(ITheme theme) {
        return theme.getPanelTheme();
    }

    @Override
    public void transform(IViewportStack stack) {
        super.transform(stack);
        // apply scaling for animation
        if (getScale() != 1f) {
            float x = getArea().w() / 2f;
            float y = getArea().h() / 2f;
            stack.translate(x, y);
            stack.scale(getScale(), getScale());
            stack.translate(-x, -y);
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (hasChildren()) {
            IViewport.getChildrenAt(this, stack, widgets, x, y);
        }
    }

    @Override
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (isInside(stack, x, y)) {
            widgets.add(this, stack.peek());
        }
    }

    private void findHoveredWidgets() {
        this.hovering.clear();
        this.hovering.trim();
        if (!isEnabled()) {
            return;
        }
        HoveredWidgetList widgetList = new HoveredWidgetList(this.hovering);
        getContext().reset();
        GuiViewportStack stack = new GuiViewportStack();
        stack.pushViewport(null, getScreen().getScreenArea());
        stack.pushViewport(this, getArea());
        transform(stack);
        getSelfAt(stack, widgetList, getContext().getMouseX(), getContext().getMouseY());
        transformChildren(stack);
        getWidgetsAt(stack, widgetList, getContext().getMouseX(), getContext().getMouseY());
        stack.popViewport(this);
        stack.popViewport(null);
    }

    @Override
    public boolean canHover() {
        return !this.invisible && super.canHover();
    }

    @MustBeInvokedByOverriders
    public void onOpen(ModularScreen screen) {
        this.screen = screen;
        getArea().z(1);
        this.scale = 1f;
        this.alpha = 1f;
        initialise(this);
        if (shouldAnimate()) {
            this.scale = 0.75f;
            this.alpha = 0f;
            getAnimator().setEndCallback(value -> {
                this.scale = 1f;
                this.alpha = 1f;
            }).forward();
        }
        this.state = State.OPEN;
    }

    void reopen() {
        if (this.state != State.CLOSED) throw new IllegalStateException();
        this.state = State.OPEN;
    }

    @MustBeInvokedByOverriders
    public void onClose() {
        if (!getScreen().isOverlay()) {
            getContext().getXeiSettings().removeExclusionArea(this);
        }
        this.state = State.CLOSED;
        if (this.panelHandler != null) {
            this.panelHandler.closePanelInternal();
        }
    }

    @MustBeInvokedByOverriders
    @Override
    public void dispose() {
        if (this.state == State.DISPOSED) return;
        if (this.state != State.CLOSED && this.state != State.WAIT_DISPOSING) {
            throw new IllegalStateException("Panel must be closed before disposing!");
        }
        if (this.cantDisposeNow) {
            this.state = State.WAIT_DISPOSING;
            return;
        }
        super.dispose();
        this.screen = null;
        this.state = State.DISPOSED;
    }

    /**
     * Wraps a function so it can be called safely. This is needed in methods where the panel can be closed and
     * disposed, but doing
     * so will result in unexpected errors. This wrapper stops the disposal until the function has been fully executed.
     * The return value of the function is then returned.
     *
     * @param runnable function to be called safely
     * @param <T>      return type
     * @return return value of function
     */
    public final <T> T doSafe(Supplier<T> runnable) {
        if (this.state == State.DISPOSED) return null;
        // make sure the screen is also not disposed
        return getScreen().getPanelManager().doSafe(() -> {
            this.cantDisposeNow = true;
            T t = runnable.get();
            this.cantDisposeNow = false;
            if (this.state == State.WAIT_DISPOSING) {
                this.state = State.CLOSED;
                dispose();
            }
            return t;
        });
    }

    public final boolean doSafeBool(BooleanSupplier runnable) {
        return Objects.requireNonNull(doSafe(runnable::getAsBoolean));
    }

    public final int doSafeInt(IntSupplier runnable) {
        return Objects.requireNonNull(doSafe(runnable::getAsInt));
    }

    public boolean onMousePressed(double mouseX, double mouseY, int button) {
        return doSafeBool(() -> {
            LocatedWidget pressed = LocatedWidget.EMPTY;
            boolean result = false;

            if (this.hovering.isEmpty()) {
                // no element is hovered -> try close panel
                if (closeOnOutOfBoundsClick()) {
                    animateClose();
                    result = true;
                }
            } else {
                for (LocatedWidget widget : this.hovering) {
                    widget.applyMatrix(getContext());
                    // click widget and see how it reacts
                    if (widget.getElement() instanceof Interactable interactable) {
                        Interactable.Result interactResult = interactable.onMousePressed(mouseX, mouseY, button);
                        if (interactResult.accepts) {
                            this.mouse.addAcceptedInteractable(interactable);
                            pressed = widget;
                        } else if (interactResult.stops) {
                            pressed = LocatedWidget.EMPTY;
                        }
                        if (interactResult.stops) {
                            result = true;
                            widget.unapplyMatrix(getContext());
                            break;
                        }
                    }
                    // see if widget can be dragged
                    if (getContext().onHoveredClick(button, widget)) {
                        pressed = LocatedWidget.EMPTY;
                        result = true;
                        widget.unapplyMatrix(getContext());
                        break;
                    }
                    widget.unapplyMatrix(getContext());
                    // see if widgets below this can be interacted with
                    if (!widget.getElement().canClickThrough()) {
                        // act as if the widget was clicked and accepted
                        result = true;
                        pressed = widget;
                        break;
                    }
                }
            }

            if (result && pressed.getElement() instanceof IFocusedWidget) {
                getContext().focus(pressed);
            } else {
                getContext().removeFocus();
            }
            this.mouse.pressed(pressed, button);
            return result;
        });
    }

    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        return isEnabled() && doSafeBool(() -> {
            if (!this.mouse.doRelease) {
                this.mouse.reset();
                return false;
            }
            if (interactFocused(widget -> widget.onMouseReleased(mouseX, mouseY, button), false)) {
                return true;
            }
            boolean lastPressedIsHovered = false;
            boolean tryTap = this.mouse.tryTap(button);
            // first see if the clicked widget is still hovered and try to interact with it
            for (LocatedWidget widget : this.hovering) {
                if (this.mouse.isWidget(widget)) {
                    if (widget.getElement() instanceof Interactable interactable &&
                            onMouseReleased(mouseX, mouseY, button, tryTap, widget, interactable)) {
                        return true;
                    }
                    lastPressedIsHovered = true;
                    break;
                }
            }
            // now try all other hovered
            for (LocatedWidget widget : this.hovering) {
                if (!this.mouse.isWidget(widget) && widget.getElement() instanceof Interactable interactable &&
                        onMouseReleased(mouseX, mouseY, button, tryTap, widget, interactable)) {
                    return true;
                }
            }
            // nothing worked, but since the pressed widget is still hovered we assume success
            // otherwise JEI tries to pull some weird stuff
            if (lastPressedIsHovered) {
                this.mouse.reset();
                return true;
            }
            this.mouse.reset();
            return false;
        });
    }

    private boolean onMouseReleased(double mouseX, double mouseY, int button, boolean tryTap, LocatedWidget widget,
                                    Interactable interactable) {
        boolean stop = false;
        widget.applyMatrix(getContext());
        if (tryTap && this.mouse.acceptedInteractions.remove(interactable)) {
            Interactable.Result tabResult = interactable.onMouseTapped(mouseX, mouseY, button);
            if (tabResult.stops) {
                stop = true;
                // we will try to trigger onMouseReleased() even after tapping tells to stop
            }
        }
        if (interactable.onMouseReleased(mouseX, mouseY, button)) {
            stop = true;
        }
        widget.unapplyMatrix(getContext());
        if (stop) {
            this.mouse.reset();
            return true;
        }
        return false;
    }

    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return doSafeBool(() -> {
            switch (interactFocused(widget -> widget.onKeyPressed(keyCode, scanCode, modifiers),
                    Interactable.Result.IGNORE)) {
                case STOP:
                    this.keyboard.pressed(LocatedWidget.EMPTY, keyCode);
                    return true;
                case SUCCESS:
                    this.keyboard.pressed(getContext().getFocusedWidget(), keyCode);
                    return true;
            }
            LocatedWidget pressed = null;
            boolean result = false;
            for (LocatedWidget widget : this.hovering) {
                if (widget.getElement() instanceof Interactable interactable) {
                    widget.applyMatrix(getContext());
                    Interactable.Result interactResult = interactable.onKeyPressed(keyCode, scanCode, modifiers);
                    if (interactResult.accepts) {
                        this.keyboard.addAcceptedInteractable(interactable);
                        pressed = widget;
                    } else if (interactResult.stops) {
                        pressed = null;
                    }
                    if (interactResult.stops) {
                        result = true;
                        widget.unapplyMatrix(getContext());
                        break;
                    }
                    widget.unapplyMatrix(getContext());
                }
                if (!widget.getElement().canClickThrough()) break;
            }
            this.keyboard.pressed(pressed, keyCode);
            return result;
        });
    }

    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        return doSafeBool(() -> {
            if (!this.keyboard.doRelease) {
                this.keyboard.reset();
                return false;
            }
            if (interactFocused(widget -> widget.onKeyReleased(keyCode, scanCode, modifiers), false)) {
                return true;
            }
            boolean lastPressedIsHovered = false;
            boolean tryTap = this.keyboard.tryTap(keyCode);
            // first see if the clicked widget is still hovered and try to interact with it
            for (LocatedWidget widget : this.hovering) {
                if (this.keyboard.isWidget(widget)) {
                    if (widget.getElement() instanceof Interactable interactable &&
                            onKeyReleased(keyCode, scanCode, modifiers, tryTap, widget, interactable)) {
                        return true;
                    }
                    lastPressedIsHovered = true;
                    break;
                }
            }
            // now try all other hovered
            for (LocatedWidget widget : this.hovering) {
                if (!this.keyboard.isWidget(widget) && widget.getElement() instanceof Interactable interactable &&
                        onKeyReleased(keyCode, scanCode, modifiers, tryTap, widget, interactable)) {
                    return true;
                }
            }
            // nothing worked, but since the pressed widget is still hovered we assume success
            // otherwise JEI tries to pull some weird stuff
            if (lastPressedIsHovered) {
                this.keyboard.reset();
                return true;
            }
            this.keyboard.reset();
            return false;
        });
    }

    private boolean onKeyReleased(int keyCode, int scanCode, int modifiers, boolean tryTap, LocatedWidget widget,
                                  Interactable interactable) {
        boolean stop = false;
        widget.applyMatrix(getContext());
        if (tryTap && this.keyboard.acceptedInteractions.remove(interactable)) {
            Interactable.Result tabResult = interactable.onKeyTapped(keyCode, scanCode, modifiers);
            if (tabResult.stops) {
                stop = true;
                // we will try to trigger onMouseReleased() even after tapping tells to stop
            }
        }
        if (interactable.onKeyReleased(keyCode, scanCode, modifiers)) {
            stop = true;
        }
        widget.unapplyMatrix(getContext());
        if (stop) {
            this.keyboard.reset();
            return true;
        }
        return false;
    }

    public boolean onCharTyped(char codePoint, int modifiers) {
        return doSafeBool(() -> {
            switch (interactFocused(widget -> widget.onCharTyped(codePoint, modifiers), Interactable.Result.IGNORE)) {
                case STOP:
                    this.keyboard.pressed(LocatedWidget.EMPTY, codePoint);
                    return true;
                case SUCCESS:
                    this.keyboard.pressed(getContext().getFocusedWidget(), codePoint);
                    return true;
            }
            LocatedWidget pressed = null;
            boolean result = false;
            for (LocatedWidget widget : this.hovering) {
                if (widget.getElement() instanceof Interactable interactable) {
                    widget.applyMatrix(getContext());
                    Interactable.Result interactResult = interactable.onCharTyped(codePoint, modifiers);
                    if (interactResult.accepts) {
                        this.keyboard.addAcceptedInteractable(interactable);
                        pressed = widget;
                    } else if (interactResult.stops) {
                        pressed = null;
                    }
                    if (interactResult.stops) {
                        result = true;
                        widget.unapplyMatrix(getContext());
                        break;
                    }
                    widget.unapplyMatrix(getContext());
                }
                if (!widget.getElement().canClickThrough()) break;
            }
            this.keyboard.pressed(pressed, codePoint);
            return result;
        });
    }

    public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        return doSafeBool(() -> {
            if (interactFocused(widget -> widget.onMouseScrolled(mouseX, mouseY, delta), false)) {
                return true;
            }
            if (this.hovering.isEmpty()) return false;
            for (LocatedWidget widget : this.hovering) {
                if (widget.getElement() instanceof Interactable interactable) {
                    widget.applyMatrix(getContext());
                    boolean result = interactable.onMouseScrolled(mouseX, mouseY, delta);
                    widget.unapplyMatrix(getContext());
                    if (result) return true;
                }
            }
            return false;
        });
    }

    public boolean onMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return doSafeBool(() -> {
            if (this.mouse.held &&
                    button == this.mouse.lastButton &&
                    this.mouse.lastPressed != null &&
                    this.mouse.lastPressed.getElement() instanceof Interactable interactable) {
                this.mouse.lastPressed.applyMatrix(getContext());
                interactable.onMouseDrag(mouseX, mouseY, button, dragX, dragY);
                this.mouse.lastPressed.unapplyMatrix(getContext());
                return true;
            }
            return false;
        });
    }

    @SuppressWarnings("unchecked")
    private <T, W extends IWidget & IFocusedWidget & Interactable> T interactFocused(Function<W, T> function,
                                                                                     T defaultValue) {
        LocatedWidget focused = this.getContext().getFocusedWidget();
        T result = defaultValue;
        if (focused.getElement() instanceof Interactable interactable) {
            focused.applyMatrix(getContext());
            result = function.apply((W) interactable);
            focused.unapplyMatrix(getContext());
        }
        return result;
    }

    /**
     * @return if this panel can be dragged. Never works on the main panel.
     */
    public boolean isDraggable() {
        return getScreen().getMainPanel() != this;
    }

    /**
     * @return if panels below this can still be interacted with.
     */
    public boolean disablePanelsBelow() {
        return false;
    }

    /**
     * @return if this panel should be closed if outside of this panel is clicked.
     */
    public boolean closeOnOutOfBoundsClick() {
        return false;
    }

    @Override
    public ModularScreen getScreen() {
        if (!isValid()) {
            throw new IllegalStateException();
        }
        return this.screen;
    }

    @Nullable
    public IWidget getTopHovering() {
        LocatedWidget lw = getTopHoveringLocated(false);
        return lw == null ? null : lw.getElement();
    }

    @Nullable
    public LocatedWidget getTopHoveringLocated(boolean debug) {
        int i = 0;
        while (i < this.hovering.size()) {
            LocatedWidget widget = this.hovering.get(i);
            if (!widget.getElement().isValid()) {
                this.hovering.remove(i);
                continue;
            }
            if (debug || widget.getElement().canHover()) {
                return widget;
            }
            i++;
        }
        return null;
    }

    @Override
    public int getDefaultHeight() {
        return 166;
    }

    @Override
    public int getDefaultWidth() {
        return 176;
    }

    final void setPanelGuiContext(@NotNull ModularGuiContext context) {
        setContext(context);
        if (!context.getScreen().isOverlay()) {
            context.getXeiSettings().addExclusionArea(this);
        }
    }

    public boolean isOpening() {
        return this.animator != null && this.animator.isRunningForwards();
    }

    public boolean isClosing() {
        return this.animator != null && this.animator.isRunningBackwards();
    }

    public final boolean isMainPanel() {
        return getScreen().getMainPanel() == this;
    }

    @ApiStatus.Internal
    @Override
    public void setSyncHandler(@Nullable SyncHandler syncHandler) {
        if (!isValidSyncHandler(syncHandler))
            throw new IllegalStateException("Panel SyncHandler's must implement IPanelHandler!");

        super.setSyncHandler(syncHandler);
        setPanelHandler((IPanelHandler) syncHandler);
    }

    @NotNull
    protected Animator getAnimator() {
        if (this.animator == null) {
            this.animator = new Animator(getScreen().getCurrentTheme().getOpenCloseAnimationOverride(),
                    Interpolation.QUINT_OUT)
                    .setValueBounds(0.0f, 1.0f)
                    .setCallback(val -> {
                        this.alpha = (float) val;
                        this.scale = (float) val * 0.25f + 0.75f;
                    });
        }
        return this.animator;
    }

    public boolean shouldAnimate() {
        return !getScreen().isOverlay() && getScreen().getCurrentTheme().getOpenCloseAnimationOverride() > 0;
    }

    void registerSubPanel(IPanelHandler handler) {
        if (!this.clientSubPanels.contains(handler)) {
            this.clientSubPanels.add(handler);
        }
    }

    void closeClientSubPanels() {
        for (IPanelHandler handler : this.clientSubPanels) {
            if (handler.isSubPanel()) {
                handler.closePanel();
            }
        }
    }

    public ModularPanel bindPlayerInventory() {
        return child(SlotGroupWidget.playerInventory(true));
    }

    public ModularPanel bindPlayerInventory(int bottom) {
        return child(SlotGroupWidget.playerInventory(bottom, true));
    }

    public ModularPanel invisible() {
        this.invisible = true;
        return background(IDrawable.EMPTY);
    }

    @Override
    public String toString() {
        return super.toString() + "#" + getName();
    }

    public enum State {
        /**
         * Initial state of any panel
         */
        IDLE,
        /**
         * State after the panel opened
         */
        OPEN,
        /**
         * State after panel closed
         */
        CLOSED,
        /**
         * State after panel disposed.
         * Panel can still be reopened in this state.
         */
        DISPOSED,
        /**
         * Panel is closed and is waiting to be disposed.
         */
        WAIT_DISPOSING
    }

    /**
     * A helper class to handle input states for mouse and keyboard separatly
     */
    private static class Input {

        private final ObjectList<Interactable> acceptedInteractions = new ObjectArrayList<>();
        @Nullable
        private LocatedWidget lastPressed;
        private boolean held;
        private long time;
        private int lastButton;
        private boolean doRelease = true;

        private Input() {
            reset();
        }

        private void addAcceptedInteractable(Interactable interactable) {
            if (!this.held) {
                this.acceptedInteractions.add(interactable);
            }
        }

        private void reset() {
            this.acceptedInteractions.clear();
            this.held = false;
            this.time = -1;
            this.lastButton = -1;
            this.doRelease = true;
        }

        private boolean isValid() {
            return this.lastPressed != null && this.time > 0;
        }

        private int getTimeSinceEvent() {
            return (int) Math.min(Util.getMillis() - this.time, Integer.MAX_VALUE);
        }

        private boolean tryTap(int button) {
            return this.lastButton == button && getTimeSinceEvent() <= tapTime;
        }

        private boolean isWidget(IWidget widget) {
            return this.lastPressed != null && this.lastPressed.getElement() == widget;
        }

        private boolean isWidget(LocatedWidget widget) {
            return isWidget(widget.getElement());
        }

        private void pressed(LocatedWidget pressed, int button) {
            if (!this.held) {
                this.lastPressed = pressed;
                if (this.lastPressed != null) {
                    this.time = Util.getMillis();
                }
                this.lastButton = button;
                this.held = true;
            }
        }
    }
}
