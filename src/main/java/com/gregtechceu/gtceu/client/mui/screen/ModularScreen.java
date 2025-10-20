package com.gregtechceu.gtceu.client.mui.screen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiAction;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.overlay.OverlayScreenWrapper;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widget.wrapper.WidgetWrapper;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * This is the base class for all modular UIs. It only exists on client side.
 * It handles drawing the screen, all panels and widget interactions.
 */
@OnlyIn(Dist.CLIENT)
public class ModularScreen implements GuiEventListener, Renderable, LayoutElement, NarratableEntry {

    public static boolean isScreen(@Nullable Screen guiScreen, String owner, String name) {
        if (guiScreen instanceof IMuiScreen screenWrapper) {
            ModularScreen screen = screenWrapper.getScreen();
            return screen.getOwner().equals(owner) && screen.getName().equals(name);
        }
        return false;
    }

    public static boolean isActive(String owner, String name) {
        return isScreen(Minecraft.getInstance().screen, owner, name);
    }

    @Nullable
    public static ModularScreen getCurrent() {
        if (MCHelper.getCurrentScreen() instanceof IMuiScreen screenWrapper) {
            return screenWrapper.getScreen();
        }
        return null;
    }

    /**
     * The owner of this screen. Usually a modid. This is mainly used to find theme overrides.
     */
    @Getter
    private final String owner;
    /**
     * The name of this screen, which is also the name of the panel. Every UI under one owner should have a different
     * name.
     * Unfortunately there is no good way to verify this, so it's the UI implementors responsibility to set a proper
     * name for the main panel.
     * This is mainly used to find theme overrides.
     */
    @Getter
    private final String name;
    @Getter
    private final PanelManager panelManager;
    @Getter
    private final ModularGuiContext context = new ModularGuiContext(this);
    private final Map<Class<?>, List<IGuiAction>> guiActionListeners = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectArrayMap<IWidget, Runnable> frameUpdates = new Object2ObjectArrayMap<>();
    @Getter
    private boolean pauseScreen = false;
    @Getter
    private boolean openParentOnClose = false;

    private ITheme currentTheme;
    @Getter
    private IMuiScreen screenWrapper;
    /**
     * true if this is an overlay for another screen
     */
    @Getter
    private boolean overlay = false;

    /**
     * Creates a new screen with a ModularUI as its owner and a given {@link ModularPanel}.
     *
     * @param mainPanel main panel of this screen
     */
    public ModularScreen(@NotNull ModularPanel mainPanel) {
        this(GTCEu.MOD_ID, mainPanel);
    }

    /**
     * Creates a new screen with a given owner and {@link ModularPanel}.
     *
     * @param owner     owner of this screen (usually a mod id)
     * @param mainPanel main panel of this screen
     */
    public ModularScreen(@NotNull String owner, @NotNull ModularPanel mainPanel) {
        this(owner, context -> mainPanel);
    }

    /**
     * Creates a new screen with the given owner and a main panel function. The function must return a non-null value.
     *
     * @param owner            owner of this screen (usually a mod id)
     * @param mainPanelCreator function which creates the main panel of this screen
     */
    public ModularScreen(@NotNull String owner, @NotNull Function<ModularGuiContext, ModularPanel> mainPanelCreator) {
        this(owner, Objects.requireNonNull(mainPanelCreator, "The main panel function must not be null!"), false);
    }

    private ModularScreen(@NotNull String owner, @Nullable Function<ModularGuiContext, ModularPanel> mainPanelCreator,
                          boolean ignored) {
        Objects.requireNonNull(owner, "The owner must not be null!");
        this.owner = owner;
        ModularPanel mainPanel = mainPanelCreator != null ? mainPanelCreator.apply(this.context) :
                buildUI(this.context);
        Objects.requireNonNull(mainPanel, "The main panel must not be null!");
        this.name = mainPanel.getName();
        this.panelManager = new PanelManager(this, mainPanel);
    }

    /**
     * Intended for use in {@link CustomModularScreen}
     */
    ModularScreen(@NotNull String owner) {
        this(owner, null, false);
    }

    /**
     * Intended for use in {@link CustomModularScreen}
     */
    ModularPanel buildUI(ModularGuiContext context) {
        throw new UnsupportedOperationException();
    }

    /**
     * Should be called in custom {@link ScreenWrapper GuiScreen} constructors which implement {@link IMuiScreen}.
     *
     * @param wrapper the gui screen wrapping this screen
     */
    @MustBeInvokedByOverriders
    public void construct(IMuiScreen wrapper) {
        if (this.screenWrapper != null) throw new IllegalStateException("ModularScreen is already constructed!");
        if (wrapper == null) throw new NullPointerException("ScreenWrapper must not be null!");
        this.screenWrapper = wrapper;
        if (this.screenWrapper.getWrappedScreen() instanceof AbstractContainerScreen<?> containerScreen) {
            if (containerScreen.getMenu() instanceof ModularContainerMenu modular && !modular.isScreenInitialized()) {
                modular.initializeClient(this);
            }
        }
        this.screenWrapper.updateGuiArea(this.panelManager.getMainPanel().getArea());
        this.overlay = false;
    }

    @ApiStatus.Internal
    @MustBeInvokedByOverriders
    public void constructOverlay(Screen screen) {
        if (this.screenWrapper != null) throw new IllegalStateException("ModularScreen is already constructed!");
        if (screen == null) throw new NullPointerException("ScreenWrapper must not be null!");
        this.screenWrapper = new OverlayScreenWrapper(screen, this);
        this.overlay = true;
    }

    /**
     * Called everytime the Game window changes its size. Overriding for additional logic is allowed, but super must be
     * called.
     * This method resizes the entire widget tree of every panel currently open and then updates the size of the
     * {@link IMuiScreen} wrapper.
     * <p>
     * Do not call this method except in an override!
     *
     * @param width  with of the resized game window
     * @param height height of the resized game window
     */
    @MustBeInvokedByOverriders
    public void onResize(int width, int height) {
        this.context.updateScreenArea(width, height);
        if (this.panelManager.tryInit()) {
            onOpen();
        }

        this.context.pushViewport(null, this.context.getScreenArea());
        for (ModularPanel panel : this.panelManager.getReverseOpenPanels()) {
            WidgetTree.resizeInternal(panel, true);
        }

        this.context.popViewport(null);
        if (!isOverlay()) {
            this.screenWrapper.updateGuiArea(this.panelManager.getMainPanel().getArea());
        }
    }

    /**
     * Called after the screen is opened, but before the screen and all widgets are resized.
     */
    @ApiStatus.OverrideOnly
    public void onOpen() {}

    /**
     * Called after the last panel (always the main panel) closes which closes the screen.
     */
    @ApiStatus.OverrideOnly
    public void onClose() {}

    /**
     * Gently closes all open panels and this screen. If NeverEnoughAnimations is installed and open/close is enabled
     * this will play the
     * animation for all open panels and closes the screen after the animation is finished.
     */
    public void close() {
        close(false);
    }

    /**
     * Closes all open panels and this screen. If {@code force} is true, the screen will immediately close and skip all
     * lifecycle steps to
     * properly close panels and this screen. <b>This should be avoided in most situations</b>.
     * If {@code force} is false, the panels are gently closed. If NeverEnoughAnimations is installed and open/close is
     * enabled this will
     * play the animation for all open panels and closes the screen after the animation is finished.
     *
     * @param force true if the screen should be closed immediately without going through remaining lifecycle steps.
     */
    @ApiStatus.Internal
    public void close(boolean force) {
        if (isActive()) {
            if (force) {
                Minecraft.getInstance().popGuiLayer();
                return;
            }
            getMainPanel().closeIfOpen();
        }
    }

    /**
     * Checks if a panel with a given name is currently open in this screen.
     *
     * @param name name of the panel
     * @return true if a panel with the name is open
     */
    public boolean isPanelOpen(String name) {
        return this.panelManager.isPanelOpen(name);
    }

    /**
     * Checks if a panel is currently open in this screen.
     *
     * @param panel panel to check
     * @return true if the panel is open
     */
    public boolean isPanelOpen(ModularPanel panel) {
        return this.panelManager.hasOpenPanel(panel);
    }

    /**
     * Called at the start of every client tick (20 times per second).
     */
    @MustBeInvokedByOverriders
    public void onUpdate() {
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            WidgetTree.onUpdate(panel);
        }
    }

    /**
     * Called 60 times per second in custom ticks. This logic is separate from rendering.
     */
    @MustBeInvokedByOverriders
    public void onFrameUpdate() {
        this.panelManager.checkDirty();
        for (ObjectIterator<Object2ObjectMap.Entry<IWidget, Runnable>> iterator = this.frameUpdates
                .object2ObjectEntrySet().fastIterator(); iterator.hasNext();) {
            Object2ObjectMap.Entry<IWidget, Runnable> entry = iterator.next();
            if (!entry.getKey().isValid()) {
                iterator.remove();
                continue;
            }
            entry.getValue().run();
        }
        this.context.onFrameUpdate();
    }

    /**
     * Draws this screen and all open panels with their whole widget tree.
     * <p>
     * Do not call, only override!
     */
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Lighting.setupForFlatItems();
        RenderSystem.disableDepthTest();

        this.context.reset();
        this.context.pushViewport(null, this.context.getScreenArea());
        for (ModularPanel panel : this.panelManager.getReverseOpenPanels()) {
            this.context.updateZ(panel.getArea().getPanelLayer() * 20);
            if (panel.disablePanelsBelow()) {
                GuiDraw.drawRect(graphics, 0, 0, this.context.getScreenArea().w(), this.context.getScreenArea().h(),
                        Color.argb(16, 16, 16, (int) (125 * panel.getAlpha())));
            }
            WidgetTree.drawTree(panel, this.context);
        }
        this.context.updateZ(0);
        this.context.popViewport(null);

        this.context.postRenderCallbacks.forEach(element -> element.accept(this.context));
        Lighting.setupFor3DItems();
    }

    /**
     * Called after all panels with their whole widget trees and potential additional elements are drawn.
     * <p>
     * Do not call, only override!
     */
    public void drawForeground(GuiGraphics guiGraphics, float partialTicks) {
        Lighting.setupForFlatItems();
        RenderSystem.disableDepthTest();

        this.context.reset();
        this.context.pushViewport(null, this.context.getScreenArea());
        for (ModularPanel panel : this.panelManager.getReverseOpenPanels()) {
            this.context.updateZ(100 + panel.getArea().getPanelLayer() * 20);
            if (panel.isEnabled()) {
                WidgetTree.drawTreeForeground(panel, this.context);
            }
        }
        this.context.drawDraggable(guiGraphics);
        this.context.popViewport(null);

        Lighting.setupFor3DItems();
    }

    /**
     * Called when a mouse button is pressed or released. Used to handle dropping of currently dragged elements.
     */
    public boolean handleDraggableInput(double mouseX, double mouseY, int button, boolean pressed) {
        if (this.context.hasDraggable()) {
            if (pressed) {
                this.context.onMousePressed(mouseX, mouseY, button);
            } else {
                this.context.onMouseReleased(mouseX, mouseY, button);
            }
            return true;
        }
        return false;
    }

    /**
     * Called when a mouse button is pressed. Tries to invoke
     * {@link com.gregtechceu.gtceu.api.mui.base.widget.Interactable#onMousePressed(double, double, int)
     * Interactable#onMousePressed(double, double, int)} on every widget under
     * the mouse after gui action listeners have been called. Will try to focus widgets that have been interacted with.
     * Focused widgets will be interacted with first in other interaction methods (mouse scroll, release and drag, key
     * press and release).
     *
     * @param mouseX mouse x-coordinate
     * @param mouseY mouse y-coordinate
     * @param button mouse button (0 = left button, 1 = right button, 2 = scroll button, 4 and 5 = side buttons)
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean onMousePressed(double mouseX, double mouseY, int button) {
        for (IGuiAction.MousePressed action : getGuiActionListeners(IGuiAction.MousePressed.class)) {
            action.press(mouseX, mouseY, button);
        }
        if (this.context.onMousePressed(mouseX, mouseY, button)) {
            return true;
        }
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            if (panel.onMousePressed(mouseX, mouseY, button)) {
                return true;
            }
            if (panel.disablePanelsBelow()) {
                break;
            }
        }
        return false;
    }

    /**
     * Called when a mouse button is released. Tries to invoke
     * {@link com.gregtechceu.gtceu.api.mui.base.widget.Interactable#onMouseReleased(double, double, int)
     * Interactable#onMouseRelease(int)} on every widget under
     * the mouse after gui action listeners have been called.
     *
     * @param mouseX mouse x-coordinate
     * @param mouseY mouse y-coordinate
     * @param button mouse button (0 = left button, 1 = right button, 2 = scroll button, 4 and 5 = side buttons)
     * @return true if the action was consumed and further processing should be canceled
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (IGuiAction.MouseReleased action : getGuiActionListeners(IGuiAction.MouseReleased.class)) {
            action.release(mouseX, mouseY, button);
        }
        if (this.context.onMouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            if (panel.onMouseReleased(mouseX, mouseY, button)) {
                return true;
            }
            if (panel.disablePanelsBelow()) {
                break;
            }
        }
        return false;
    }

    /**
     * Called when a keyboard key is pressed. Tries to invoke
     * {@link com.gregtechceu.gtceu.api.mui.base.widget.Interactable#onKeyPressed(int, int, int)
     * Interactable#onKeyPressed(int, int, int)} on every
     * widget under the mouse after gui action listeners have been called.
     *
     * @param keyCode   the key code of the pressed key (see constants at {@link InputConstants})
     * @param scanCode  the character of the pressed key or {@link Character#MIN_VALUE} for keys without a character
     * @param modifiers the key modifiers of the pressed key (see modifiers at {@link InputConstants})
     * @return true if the action was consumed and further processing should be canceled
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (IGuiAction.KeyPressed action : getGuiActionListeners(IGuiAction.KeyPressed.class)) {
            action.press(keyCode, scanCode, modifiers);
        }
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            if (panel.onKeyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (panel.disablePanelsBelow()) {
                break;
            }
        }
        return false;
    }

    /**
     * Called when a keyboard key is released. Tries to invoke
     * {@link com.gregtechceu.gtceu.api.mui.base.widget.Interactable#onKeyReleased(int, int, int)
     * Interactable#onKeyRelease(int, int, int)} on every
     * widget under the mouse after gui action listeners have been called.
     *
     * @param keyCode   the key code of the pressed key (see constants at {@link InputConstants})
     * @param scanCode  the character of the pressed key or {@link Character#MIN_VALUE} for keys without a character
     * @param modifiers the key modifiers of the pressed key (see modifiers at {@link InputConstants})
     * @return true if the action was consumed and further processing should be canceled
     */
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (IGuiAction.KeyReleased action : getGuiActionListeners(IGuiAction.KeyReleased.class)) {
            action.release(keyCode, scanCode, modifiers);
        }
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            if (panel.onKeyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (panel.disablePanelsBelow()) {
                break;
            }
        }
        return false;
    }

    /**
     * Called when a keyboard key is released. Tries to invoke
     * {@link com.gregtechceu.gtceu.api.mui.base.widget.Interactable#onCharTyped(char, int)
     * Interactable#onCharTyped(char, int)} on every
     * widget under the mouse after gui action listeners have been called.
     *
     * @param codePoint the code point of the typed character
     * @param modifiers the key modifiers of the typed character (see modifiers at {@link InputConstants})
     * @return true if the action was consumed and further processing should be canceled
     */
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (IGuiAction.CharTyped action : getGuiActionListeners(IGuiAction.CharTyped.class)) {
            action.type(codePoint, modifiers);
        }
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            if (panel.onCharTyped(codePoint, modifiers)) {
                return true;
            }
            if (panel.disablePanelsBelow()) {
                break;
            }
        }
        return false;
    }

    /**
     * Called when a mouse button is released. Tries to invoke
     * {@link com.gregtechceu.gtceu.api.mui.base.widget.Interactable#onMouseScrolled(double, double, double)
     * Interactable#onMouseScrolled(double, double, double)} on every widget under
     * the mouse after gui action listeners have been called.
     *
     * @param mouseX current mouse X coordinate relative to the screen
     * @param mouseY current mouse Y coordinate relative to the screen
     * @param delta  the direction and speed of the scroll
     * @return true if the action was consumed and further processing should be canceled
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (IGuiAction.MouseScroll action : getGuiActionListeners(IGuiAction.MouseScroll.class)) {
            action.scroll(mouseX, mouseY, delta);
        }
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            if (panel.onMouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
            if (panel.disablePanelsBelow()) {
                break;
            }
        }
        return false;
    }

    /**
     * Called every time the mouse pos changes and a mouse button is held down. Invokes
     * {@link com.gregtechceu.gtceu.api.mui.base.widget.Interactable#onMouseDrag(double, double, int, double, double)
     * Interactable#onMouseDrag(double, double, int, double, double)} on every widget
     * under the mouse after gui action listeners have been called.
     *
     * @param mouseX current mouse X coordinate relative to the screen
     * @param mouseY current mouse Y coordinate relative to the screen
     * @param button mouse button that is held down
     *               (0 = left button, 1 = right button, 2 = scroll button, 4 and 5 = side buttons)
     * @param dragX  the X distance of the drag
     * @param dragY  the Y distance of the drag
     * @return true if the action was consumed and further processing should be canceled
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (IGuiAction.MouseDrag action : getGuiActionListeners(IGuiAction.MouseDrag.class)) {
            action.drag(mouseX, mouseY, button, dragX, dragY);
        }
        for (ModularPanel panel : this.panelManager.getOpenPanels()) {
            if (panel.onMouseDrag(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
            if (panel.disablePanelsBelow()) {
                break;
            }
        }
        return false;
    }

    /**
     * Called with {@code true} after a widget which implements
     * {@link com.gregtechceu.gtceu.api.mui.base.widget.IFocusedWidget IFocusedWidget}
     * has consumed a mouse press and called with {@code false} if a widget is currently focused and anything else has
     * consumed a mouse
     * press. This is required for other mods like JEI/EMI to not interfere with inputs.
     *
     * @param focus true if the gui screen will be focused
     */
    @ApiStatus.Internal
    public void setFocused(boolean focus) {
        this.screenWrapper.getWrappedScreen().setFocused(focus);
    }

    @Override
    public boolean isFocused() {
        return this.screenWrapper.getWrappedScreen().isFocused();
    }

    /**
     * @return true if this screen is currently open and displayed on the screen
     */
    public boolean isActive() {
        return getCurrent() == this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + getOwner() + ":" + getName();
    }

    /**
     * @return the owner and name as a {@link ResourceLocation}
     * @see #getOwner()
     * @see #getName()
     */
    public ResourceLocation getResourceLocation() {
        return new ResourceLocation(this.owner, this.name);
    }

    public ModularSyncManager getSyncManager() {
        return getContainer().getSyncManager();
    }

    public ModularPanel getMainPanel() {
        return this.panelManager.getMainPanel();
    }

    public Area getScreenArea() {
        return this.context.getScreenArea();
    }

    public boolean isClientOnly() {
        return isOverlay() || !this.screenWrapper.isGuiContainer() || getContainer().isClientOnly();
    }

    public ModularContainerMenu getContainer() {
        if (isOverlay()) {
            throw new IllegalStateException("Can't get ModularContainer for overlay");
        }
        if (this.screenWrapper.getWrappedScreen() instanceof AbstractContainerScreen<?> container) {
            return (ModularContainerMenu) container.getMenu();
        }
        throw new IllegalStateException("Screen does not extend AbstractContainerScreen!");
    }

    @SuppressWarnings("unchecked")
    private <T extends IGuiAction> List<T> getGuiActionListeners(Class<T> clazz) {
        return (List<T>) this.guiActionListeners.getOrDefault(clazz, Collections.emptyList());
    }

    /**
     * Registers an interaction listener. This is useful when you want to listen to any GUI interactions and not just
     * for a specific widget. <br>
     * <b>Do NOT register listeners which are bound to a widget here!</b>
     * Use {@link com.gregtechceu.gtceu.api.mui.widget.Widget#listenGuiAction(IGuiAction)
     * Widget#listenGuiAction(IGuiAction)} for that!
     *
     * @param action action listener
     */
    public void registerGuiActionListener(IGuiAction action) {
        List<IGuiAction> list = this.guiActionListeners.computeIfAbsent(getGuiActionClass(action),
                key -> new ArrayList<>());
        if (!list.contains(action)) list.add(action);
    }

    /**
     * Removes an interaction listener
     *
     * @param action action listener to remove
     */
    public void removeGuiActionListener(IGuiAction action) {
        this.guiActionListeners.getOrDefault(getGuiActionClass(action), Collections.emptyList()).remove(action);
    }

    /**
     * Registers a frame update listener which runs approximately 60 times per second.
     * Listeners are automatically removed if the widget becomes invalid.
     * If a listener is already registered from the given widget, the listeners get merged.
     *
     * @param widget   widget the listener is bound to
     * @param runnable listener function
     */
    public void registerFrameUpdateListener(IWidget widget, Runnable runnable) {
        registerFrameUpdateListener(widget, runnable, true);
    }

    /**
     * Registers a frame update listener which runs approximately 60 times per second.
     * Listeners are automatically removed if the widget becomes invalid.
     * If a listener is already registered from the given widget and <code>merge</code> is true, the listeners get
     * merged.
     * Otherwise, the current listener is overwritten (if any)
     *
     * @param widget   widget the listener is bound to
     * @param runnable listener function
     * @param merge    if listener should be merged with existing listener
     */
    public void registerFrameUpdateListener(IWidget widget, Runnable runnable, boolean merge) {
        Objects.requireNonNull(runnable);
        if (merge) {
            this.frameUpdates.merge(widget, runnable, (old, now) -> () -> {
                old.run();
                now.run();
            });
        } else {
            this.frameUpdates.put(widget, runnable);
        }
    }

    /**
     * Removes all frame update listeners for a widget.
     *
     * @param widget widget to remove listeners from
     */
    public void removeFrameUpdateListener(IWidget widget) {
        this.frameUpdates.remove(widget);
    }

    private static Class<?> getGuiActionClass(IGuiAction action) {
        Class<?>[] classes = action.getClass().getInterfaces();
        for (Class<?> clazz : classes) {
            if (IGuiAction.class.isAssignableFrom(clazz)) {
                return clazz;
            }
        }
        throw new IllegalArgumentException();
    }

    public ITheme getCurrentTheme() {
        if (this.currentTheme == null) {
            useTheme(null);
        }
        return this.currentTheme;
    }

    /**
     * Tries to use a specific theme for this screen. If the theme for this screen has been overriden via resource
     * packs, this method does
     * nothing.
     *
     * @param theme id of theme to use
     * @return this for builder like usage
     */
    public ModularScreen useTheme(String theme) {
        this.currentTheme = IThemeApi.get().getThemeForScreen(this, theme);
        return this;
    }

    /**
     * Sets if the gui should pause the game in the background. Pausing means every ticking will halt. If the client is
     * connected to a
     * dedicated server the UI will NEVER pause the game.
     *
     * @param pausesGame true if the ui should pause the game in the background.
     * @return this for builder like usage
     */
    public ModularScreen pausesGame(boolean pausesGame) {
        this.pauseScreen = pausesGame;
        return this;
    }

    public ModularScreen openParentOnClose(boolean openParentOnClose) {
        this.openParentOnClose = openParentOnClose;
        return this;
    }

    @Override
    public void setX(int x) {
        this.panelManager.getMainPanel().getArea().setX(x);
    }

    @Override
    public void setY(int y) {
        this.panelManager.getMainPanel().getArea().setY(y);
    }

    @Override
    public int getX() {
        return this.panelManager.getMainPanel().getArea().getX();
    }

    @Override
    public int getY() {
        return this.panelManager.getMainPanel().getArea().getY();
    }

    @Override
    public int getWidth() {
        return this.panelManager.getMainPanel().getArea().getWidth();
    }

    @Override
    public int getHeight() {
        return this.panelManager.getMainPanel().getArea().getHeight();
    }

    @Override
    public @NotNull ScreenRectangle getRectangle() {
        Area area = this.panelManager.getMainPanel().getArea();
        return new ScreenRectangle(area.x(), area.y(), area.w(), area.h());
    }

    @Override
    public void visitWidgets(@NotNull Consumer<AbstractWidget> consumer) {
        for (WidgetWrapper wrapper : panelManager.getReverseOpenPanelsWrappers()) {
            consumer.accept(wrapper);
        }
    }

    private static final Component USAGE_NARRATION = Component.translatable("narrator.screen.usage");

    private NarratableEntry lastNarratable = null;

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
        output.add(NarratedElementType.USAGE, USAGE_NARRATION);
        var entries = StreamSupport.stream(panelManager.getReverseOpenPanelsWrappers().spliterator(), false);
        WidgetWrapper.updateNarrations(entries, output, lastNarratable, entry -> lastNarratable = entry);
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        if (this.isFocused()) return NarrationPriority.FOCUSED;
        else if (this.context.isHovered()) return NarrationPriority.HOVERED;
        else return NarrationPriority.NONE;
    }
}
