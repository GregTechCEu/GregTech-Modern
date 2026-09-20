package brachy.modularui.screen;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.api.ITheme;
import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.MCHelper;
import brachy.modularui.api.RecipeViewerSettings;
import brachy.modularui.api.UIType;
import brachy.modularui.api.widget.IFocusedWidget;
import brachy.modularui.api.widget.IGuiAction;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.overlay.OverlayScreenWrapper;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Color;
import brachy.modularui.value.sync.ModularSyncManager;
import brachy.modularui.widget.Widget;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widget.sizer.ScreenResizeNode;
import brachy.modularui.widgets.menu.MenuPanel;

import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * This is the base class for all modular UIs. It only exists on client side.
 * It handles drawing the screen, all panels and widget interactions.
 */
@OnlyIn(Dist.CLIENT)
public class ModularScreen implements Renderable {

    public static final double UPDATE_INTERVAL = 1000 / 20.0;
    public static final double FRAME_UPDATE_INTERVAL = 1000 / 60.0;

    public static boolean isScreen(@Nullable Screen guiScreen, String owner, String name) {
        if (guiScreen instanceof IMuiScreen screenWrapper) {
            ModularScreen screen = screenWrapper.screen();
            return screen.getOwner().equals(owner) && screen.getName().equals(name);
        }
        return false;
    }

    public static boolean isActive(String owner, String name) {
        return isScreen(Minecraft.getInstance().gui.screen(), owner, name);
    }

    @Nullable
    public static ModularScreen getCurrent() {
        if (MCHelper.getCurrentScreen() instanceof IMuiScreen screenWrapper) {
            return screenWrapper.screen();
        }
        return null;
    }

    public static ModularScreen createEmbed(String owner, ModularPanel<?> panel) {
        Window window = Minecraft.getInstance().getWindow();
        return createEmbed(owner, panel, window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }

    public static ModularScreen createEmbed(String owner, ModularPanel<?> panel, int width, int height) {
        ModularScreen screen = new ModularScreen(UIType.EMBED, owner, c -> panel.pos(0, 0), false);
        screen.construct(new EmbedHandler.EmbedWrapper(screen));
        screen.getContext().setSettings(new UISettings());
        screen.onResize(width, height);
        return screen;
    }

    /**
     * The owner of this screen. Usually a modid. This is mainly used to find theme overrides.
     */
    @Getter private final String owner;
    /**
     * The name of this screen, which is also the name of the panel. Every UI under one owner should have a different
     * name.
     * Unfortunately there is no good way to verify this, so it's the UI implementors responsibility to set a proper
     * name for the main panel.
     * This is mainly used to find theme overrides.
     */
    @Getter private final String name;
    @Getter private final PanelManager panelManager;
    @Getter private final ModularGuiContext context;
    private final Map<Class<?>, List<IGuiAction>> guiActionListeners = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectArrayMap<IWidget, Runnable> frameUpdates = new Object2ObjectArrayMap<>();
    @Getter private final ScreenResizeNode resizeNode = new ScreenResizeNode(this);
    @Getter private boolean pauseScreen = false;
    @Getter private boolean openParentOnClose = false;

    @Getter
    @Nullable
    private String themeOverride;
    private ITheme currentTheme;
    @Getter private IMuiScreen screenWrapper;
    /**
     * true if this is an overlay for another screen
     */
    @Getter private boolean overlay = false;

    private double lastUpdate = -1, lastFrameUpdate = -1;

    /**
     * Creates a new screen with a given owner and {@link ModularPanel}.
     *
     * @param owner     owner of this screen (usually a mod id)
     * @param mainPanel main panel of this screen
     */
    public ModularScreen(@NotNull String owner, @NotNull ModularPanel<?> mainPanel) {
        this(owner, context -> mainPanel);
    }

    /**
     * Creates a new screen with the given owner and a main panel function. The function must return a non-null value.
     *
     * @param owner            owner of this screen (usually a mod id)
     * @param mainPanelCreator function which creates the main panel of this screen
     */
    public ModularScreen(String owner, @NotNull Function<ModularGuiContext, ModularPanel<?>> mainPanelCreator) {
        this(UIType.MODULAR_SCREEN, owner, Objects.requireNonNull(mainPanelCreator, "The main panel function must not be null!"), false);
    }

    private ModularScreen(UIType uiType, String owner, @Nullable Function<ModularGuiContext, ModularPanel<?>> mainPanelCreator, boolean __) {
        this.context = new ModularGuiContext(Objects.requireNonNull(uiType, "UIType must not be null"), this);
        Objects.requireNonNull(owner, "The owner must not be null!");
        this.owner = owner;
        ModularPanel<?> mainPanel = mainPanelCreator != null ? mainPanelCreator.apply(this.context) : buildUI(this.context);
        Objects.requireNonNull(mainPanel, "The main panel must not be null!");
        this.name = mainPanel.getName();
        this.panelManager = new PanelManager(this, mainPanel);
        NeoForge.EVENT_BUS.post(new BuildPanelEvent.MainPanel(this));
    }

    /**
     * Intended for use in {@link CustomModularScreen}
     */
    ModularScreen(@NotNull String owner) {
        this(UIType.MODULAR_SCREEN, owner, null, false);
    }

    /**
     * Intended for use in {@link CustomModularScreen}
     */
    ModularPanel<?> buildUI(ModularGuiContext context) {
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
        if (this.screenWrapper.wrappedScreen() instanceof AbstractContainerScreen<?> containerScreen) {
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
        if (!this.context.hasSettings()) {
            this.context.setSettings(new UISettings(RecipeViewerSettings.DUMMY));
        }
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
        WidgetTree.verifyTree(this.resizeNode, new ReferenceOpenHashSet<>());
        WidgetTree.resizeInternal(this.resizeNode, true);

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
                Minecraft.getInstance().gui.popScreenLayer();
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
    public boolean isPanelOpen(ModularPanel<?> panel) {
        return this.panelManager.hasOpenPanel(panel);
    }

    /**
     * Called at the start of every client tick (20 times per second).
     */
    @MustBeInvokedByOverriders
    public void onUpdate() {
        for (ModularPanel<?> panel : this.panelManager.getOpenPanels()) {
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
                .object2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
            Object2ObjectMap.Entry<IWidget, Runnable> entry = iterator.next();
            if (!entry.getKey().isValid()) {
                iterator.remove();
                continue;
            }
            entry.getValue().run();
        }
        this.context.onFrameUpdate();
    }

    private void checkManualUpdate() {
        long time = Util.getMillis();
        if (this.lastFrameUpdate < 0 || this.lastUpdate < 0) {
            this.lastUpdate = time;
            this.lastFrameUpdate = time;
            return;
        }
        while (time - this.lastFrameUpdate > FRAME_UPDATE_INTERVAL) {
            onFrameUpdate();
            this.lastFrameUpdate += FRAME_UPDATE_INTERVAL;
        }
        while (time - this.lastUpdate > UPDATE_INTERVAL) {
            onUpdate();
            this.lastUpdate += UPDATE_INTERVAL;
        }
    }

    /**
     * Draws this screen and all open panels with their whole widget tree.
     * <p>
     * Do not call, only override!
     */
    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        render(graphics, mouseX, mouseY, partialTick);
    }

    public void render(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (!this.context.getUItype().isScreen) {
            checkManualUpdate(); // embeds can't trigger frame updates the proper way
        }
        this.context.setGraphics(graphics);
        this.context.updateState(mouseX, mouseY, partialTick);

        this.context.pushViewport(null, this.context.getScreenArea());
        for (ModularPanel<?> panel : this.panelManager.getReverseOpenPanels()) {
            this.context.updateZ(0);
            if (panel.disablePanelsBelow()) {
                GuiDraw.drawRect(graphics, 0, 0, this.context.getScreenArea().w(), this.context.getScreenArea().h(),
                        Color.argb(16, 16, 16, (int) (125 * panel.getAlpha())));
            }
            WidgetTree.drawTree(panel, this.context);
            // Each subsequent panel occupies a new deferred GUI layer.
            graphics.nextStratum();

        }
        this.context.updateZ(0);
        this.context.popViewport(null);

        this.context.postRenderCallbacks.forEach(element -> element.accept(this.context));
    }

    /**
     * Called after all panels with their whole widget trees and potential additional elements are drawn.
     * <p>
     * Do not call, only override!
     */
    public void drawForeground(GuiGraphicsExtractor graphics) {
        this.context.setGraphics(graphics);

        this.context.pushViewport(null, this.context.getScreenArea());
        for (ModularPanel<?> panel : this.panelManager.getReverseOpenPanels()) {
            this.context.updateZ(100);
            if (panel.isEnabled()) {
                WidgetTree.drawTreeForeground(panel, this.context);
            }
        }
        this.context.drawDraggable(graphics);
        this.context.popViewport(null);
    }

    /**
     * Called when a mouse button is pressed or released. Used to handle dropping of currently dragged elements.
     */
    public boolean handleDraggableInput(int button, boolean pressed) {
        if (this.context.hasDraggable()) {
            if (pressed) {
                this.context.onMousePressed(button);
            } else {
                this.context.onMouseReleased(button);
            }
            return true;
        }
        return false;
    }

    /**
     * Called when a mouse button is pressed. Tries to invoke
     * {@link Interactable#onMousePressed(int)
     * Interactable#onMousePressed(double, double, int)} on every widget under
     * the mouse after gui action listeners have been called. Will try to focus widgets that have been interacted with.
     * Focused widgets will be interacted with first in other interaction methods (mouse scroll, release and drag, key
     * press and release).
     *
     * @param button mouse button (0 = left button, 1 = right button, 2 = scroll button, 4 and 5 = side buttons)
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean mousePressed(int button) {
        this.context.updateMouseButton(button, true);
        // call all action listeners
        for (IGuiAction.MousePressed action : getGuiActionListeners(IGuiAction.MousePressed.class)) {
            action.press(this.context, button);
        }
        // check if any context menu is open and close them if they or their children are not hovered
        for (ModularPanel<?> panel : this.panelManager.getOpenPanels()) {
            if (panel instanceof MenuPanel menuPanel) {
                menuPanel.closeAllMenus(false, true);
            }
        }
        // handle dragging of draggable widgets
        if (this.context.onMousePressed(button)) {
            return true;
        }
        // finally click hovered widgets
        for (ModularPanel<?> panel : this.panelManager.getOpenPanels()) {
            if (panel.onMousePressed(button)) {
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
     * {@link Interactable#onMouseReleased(int)
     * Interactable#onMouseRelease(int)} on every widget under
     * the mouse after gui action listeners have been called.
     *
     * @param button mouse button (0 = left button, 1 = right button, 2 = scroll button, 4 and 5 = side buttons)
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean mouseReleased(int button) {
        this.context.updateMouseButton(button, false);
        for (IGuiAction.MouseReleased action : getGuiActionListeners(IGuiAction.MouseReleased.class)) {
            action.release(this.context, button);
        }
        if (this.context.onMouseReleased(button)) {
            return true;
        }
        for (ModularPanel<?> panel : this.panelManager.getOpenPanels()) {
            if (panel.onMouseReleased(button)) {
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
     * {@link Interactable#onKeyPressed(int, int, int)
     * Interactable#onKeyPressed(int, int, int)} on every
     * widget under the mouse after gui action listeners have been called.
     *
     * @param keyCode   the key code of the pressed key (see constants at {@link InputConstants})
     * @param scanCode  the character of the pressed key or {@link Character#MIN_VALUE} for keys without a character
     * @param modifiers the key modifiers of the pressed key (see modifiers at {@link InputConstants})
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.context.updateKey(keyCode, scanCode, modifiers, true);
        for (IGuiAction.KeyPressed action : getGuiActionListeners(IGuiAction.KeyPressed.class)) {
            action.press(context, modifiers);
        }
        for (ModularPanel<?> panel : this.panelManager.getOpenPanels()) {
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
     * {@link Interactable#onKeyReleased(int, int, int)
     * Interactable#onKeyRelease(int, int, int)} on every
     * widget under the mouse after gui action listeners have been called.
     *
     * @param keyCode   the key code of the pressed key (see constants at {@link InputConstants})
     * @param scanCode  the character of the pressed key or {@link Character#MIN_VALUE} for keys without a character
     * @param modifiers the key modifiers of the pressed key (see modifiers at {@link InputConstants})
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.context.updateKey(keyCode, scanCode, modifiers, false);
        for (IGuiAction.KeyReleased action : getGuiActionListeners(IGuiAction.KeyReleased.class)) {
            action.release(getContext(), keyCode, scanCode, modifiers);
        }
        for (ModularPanel<?> panel : this.panelManager.getOpenPanels()) {
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
     * {@link Interactable#onCharTyped(int, int)
     * Interactable#onCharTyped(int, int)} on every
     * widget under the mouse after gui action listeners have been called.
     *
     * @param codePoint the code point of the typed character
     * @param modifiers the key modifiers of the typed character (see modifiers at {@link InputConstants})
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean charTyped(int codePoint, int modifiers) {
        this.context.updateTypedChar(codePoint, modifiers);
        for (IGuiAction.CharTyped action : getGuiActionListeners(IGuiAction.CharTyped.class)) {
            action.type(getContext(), codePoint, modifiers);
        }
        for (ModularPanel<?> panel : this.panelManager.getOpenPanels()) {
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
     * {@link Interactable#onMouseScrolled(double, double)
     * Interactable#onMouseScrolled(double, double, double)} on every widget under
     * the mouse after gui action listeners have been called.
     *
     * @param scrollX the direction and speed of the scroll on the X axis (usually irrelevant)
     * @param scrollY the direction and speed of the scroll on the Y axis (this is usually what you want)
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean mouseScrolled(double deltaX, double deltaY) {
        this.context.updateMouseWheel(deltaX, deltaY);
        for (IGuiAction.MouseScroll action : getGuiActionListeners(IGuiAction.MouseScroll.class)) {
            action.scroll(getContext(), deltaX, deltaY);
        }
        for (ModularPanel<?> panel : this.panelManager.getOpenPanels()) {
            if (panel.onMouseScrolled(deltaX, deltaY)) {
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
     * {@link Interactable#onMouseDrag(int, double, double)
     * Interactable#onMouseDrag(double, double, int, double, double)} on every widget
     * under the mouse after gui action listeners have been called.
     *
     * @param button mouse button that is held down (0 = left button, 1 = right button, 2 = scroll button, 4 and 5 = side buttons)
     * @param dragX  the X distance of the drag
     * @param dragY  the Y distance of the drag
     * @return true if the action was consumed and further processing should be canceled
     */
    public boolean mouseDragged(int button, double dragX, double dragY) {
        for (IGuiAction.MouseDrag action : getGuiActionListeners(IGuiAction.MouseDrag.class)) {
            action.drag(getContext(), button, dragX, dragY);
        }
        for (ModularPanel<?> panel : this.panelManager.getOpenPanels()) {
            if (panel.onMouseDrag(button, dragX, dragY)) {
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
     * {@link IFocusedWidget IFocusedWidget}
     * has consumed a mouse press and called with {@code false} if a widget is currently focused and anything else has
     * consumed a mouse
     * press. This is required for other mods like JEI/EMI to not interfere with inputs.
     *
     * @param focus true if the gui screen will be focused
     */
    @ApiStatus.Internal
    public void setFocused(boolean focus) {
        this.screenWrapper.wrappedScreen().setFocused(focus);
    }

    public boolean isFocused() {
        return this.screenWrapper.wrappedScreen().isFocused();
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
     * @return the owner and name as a {@link Identifier}
     * @see #getOwner()
     * @see #getName()
     */
    public Identifier getResourceLocation() {
        return Identifier.fromNamespaceAndPath(this.owner, this.name);
    }

    public ModularSyncManager getSyncManager() {
        return getContainer().getSyncManager();
    }

    public ModularPanel<?> getMainPanel() {
        return this.panelManager.getMainPanel();
    }

    public Area getScreenArea() {
        return this.context.getScreenArea();
    }

    public @NotNull ScreenRectangle getMainRectangle() {
        return this.panelManager.getMainPanel().getArea().toScreenRectangle();
    }

    public boolean isClientOnly() {
        return isOverlay() || getContext().getUItype() != UIType.MODULAR_SCREEN
                || !this.screenWrapper.isContainerScreen() || getContainer().isClientOnly();
    }

    public ModularContainerMenu getContainer() {
        if (isOverlay()) {
            throw new IllegalStateException("Can't get ModularContainer for overlay");
        }
        if (this.screenWrapper.wrappedScreen() instanceof AbstractContainerScreen<?> container) {
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
     * Use {@link Widget#listenGuiAction(IGuiAction)} for that!
     *
     * @param action action listener
     */
    public void registerGuiActionListener(IGuiAction action) {
        // TODO these should be linked to a IWidget, which can be checked for isValid() and is panel open on use ->
        // proper event system
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
            useTheme(this.themeOverride);
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
        this.themeOverride = theme;
        this.currentTheme = IThemeApi.get().getThemeForScreen(this, this.themeOverride);
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
}
