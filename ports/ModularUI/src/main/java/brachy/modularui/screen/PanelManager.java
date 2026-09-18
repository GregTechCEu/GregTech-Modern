package brachy.modularui.screen;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.viewport.LocatedWidget;
import brachy.modularui.utils.ReverseIterable;
import brachy.modularui.widget.WidgetTree;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class PanelManager {

    private static final int DISPOSAL_CAPACITY = 1 << 4;

    @Getter
    private final @NotNull ModularScreen screen;
    /**
     * At least one panel must exist always exist.
     * If this panel is closed, all panels will close.
     */
    private final ModularPanel<?> mainPanel;
    /**
     * List of all open panels from top to bottom.
     */
    private final List<ModularPanel<?>> panels = new ObjectArrayList<>();
    // a clone of the list to avoid CMEs
    private final List<ModularPanel<?>> panelsClone = new ArrayList<>();
    private final List<ModularPanel<?>> panelsView = Collections.unmodifiableList(this.panelsClone);
    private final ReverseIterable<ModularPanel<?>> reversePanels = new ReverseIterable<>(this.panelsView);

    private final List<ModularPanel<?>> disposal = new ObjectArrayList<>(DISPOSAL_CAPACITY);
    private final Map<String, IPanelHandler> panelHandlerMap = new Object2ObjectOpenHashMap<>();
    private boolean cantDisposeNow = false;
    private boolean dirty = false;
    private State state = State.INIT;

    public PanelManager(@NotNull ModularScreen screen, ModularPanel<?> panel) {
        this.screen = screen;
        this.mainPanel = Objects.requireNonNull(panel, "Main panel must not be null!");
    }

    boolean tryInit() {
        return switch (this.state) {
            case WAIT_DISPOSAL -> throw new IllegalStateException(
                    "Tried to open panel while its waiting to be disposed. This shouldn't happen.");
            case OPEN, REOPENED -> false;
            case CLOSED -> {
                if (this.panels.isEmpty()) {
                    throw new IllegalStateException("Can't init in closed state!");
                }
                this.panels.forEach(ModularPanel::reopen);
                this.disposal.removeIf(this.panels::contains);
                setState(State.REOPENED);
                yield true;
            }
            case INIT, DISPOSED -> {
                setState(State.OPEN);
                openPanel(this.mainPanel, false);
                checkDirty();
                yield true;
            }
        };
    }

    public boolean isMainPanel(ModularPanel<?> panel) {
        return this.mainPanel == panel;
    }

    void checkDirty() {
        if (this.dirty) {
            this.panelsClone.clear();
            this.panelsClone.addAll(this.panels);
            this.dirty = false;
        }
    }

    @NotNull
    public List<LocatedWidget> getAllHoveredWidgetsList(boolean debug) {
        for (ModularPanel<?> panel : this.panels) {
            if (panel.isAnyHovered()) {
                return panel.getAllHoveringList(debug);
            }
        }
        return Collections.emptyList();
    }

    @Nullable
    public ModularPanel<?> getTopHoveredPanel() {
        for (ModularPanel<?> panel : this.panels) {
            if (panel.isAnyHovered()) return panel;
        }
        return null;
    }

    public boolean isBelowMouseInTopPanel(IWidget widget) {
        for (ModularPanel<?> panel : this.panels) {
            if (panel.isAnyHovered()) {
                return panel.isBelowMouse(widget);
            }
        }
        return false;
    }

    private void openPanel(ModularPanel<?> panel, boolean resize) {
        if (this.panels.size() == 127) {
            throw new IllegalStateException("Too many panels are open!");
        }
        if (this.panels.contains(panel) || isPanelOpen(panel.getName())) {
            throw new IllegalStateException("Panel " + panel.getName() + " is already open.");
        }
        this.disposal.remove(panel);
        panel.setPanelGuiContext(this.screen.getContext());
        this.panels.addFirst(panel);
        this.dirty = true;
        panel.onOpen(this.screen);
        if (resize) {
            WidgetTree.resizeInternal(panel.resizer(), true);
        }
    }

    public boolean isPanelOpen(String name) {
        for (ModularPanel<?> panel : this.panels) {
            if (panel.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public ModularPanel<?> getMainPanel() {
        if (isDisposed()) {
            throw new IllegalStateException("Screen has been disposed");
        }
        return this.mainPanel;
    }

    /**
     * Returns the panel that was opened last.
     *
     * @return last opened panel
     * @throws IndexOutOfBoundsException if the current state is {@link State#DISPOSED}
     */
    @NotNull
    public ModularPanel<?> getTopMostPanel() {
        return this.panels.getFirst();
    }

    @Nullable
    public IWidget getTopWidget() {
        for (ModularPanel<?> panel : this.panels) {
            IWidget widget = panel.getTopHovering();
            if (widget != null) {
                return widget;
            }
        }
        return null;
    }

    @Nullable
    public LocatedWidget getTopWidgetLocated(boolean debug) {
        for (ModularPanel<?> panel : this.panels) {
            LocatedWidget widget = panel.getTopHoveringLocated(debug);
            if (widget != null) {
                return widget;
            }
        }
        return null;
    }

    @ApiStatus.Internal
    public void openPanel(@NotNull ModularPanel<?> panel, @NotNull IPanelHandler panelHandler) {
        IPanelHandler existing = this.panelHandlerMap.get(panel.getName());
        if (existing == null) {
            this.panelHandlerMap.put(panel.getName(), panelHandler);
        } else if (existing != panelHandler) {
            ModularUI.LOGGER.error(
                    "Tried to open a panel, but a panel handler that opens the same panel already exists. Using existing panel handler!");
            existing.openPanel();
            return;
        }
        openPanel(panel, true);
    }

    public void closePanel(@NotNull ModularPanel<?> panel) {
        if (!hasOpenPanel(panel)) {
            throw new IllegalArgumentException("Panel '" + panel.getName() + "' is open in this screen!");
        }
        if (panel == getMainPanel()) {
            closeAll();
            this.screen.close(true);
            return;
        }
        if (this.panels.remove(panel)) {
            finalizePanel(panel);
            this.dirty = true;
        }
    }

    public void closeTopPanel() {
        getTopMostPanel().closeIfOpen();
    }

    /**
     * Closes all panels. Note that this won't close the screen and can put the screen and main panel into an invalid state if used
     * incorrectly. Use {@link #closePanelsAndScreen()} to actually close all panels and the screen properly.
     *
     * @return if the screen was open
     */
    public boolean closeAll() {
        if (this.state.isOpen) {
            // any open panel will be set to closed, but will not actually be removed, so it can be reopened
            this.panels.forEach(this::finalizePanel);
            setState(State.CLOSED);
            this.screen.onClose();
            return true;
        }
        return false;
    }

    /**
     * Closes all panels and the screen. Should not be used for overlays or embeds.
     *
     * @return if this screen was open
     */
    public boolean closePanelsAndScreen() {
        if (this.state.isOpen) {
            // create a list with non-main panels
            // looping directly over panels may cause CME
            List<ModularPanel<?>> subPanels = new ArrayList<>();
            for (ModularPanel<?> panel : this.panels) {
                if (panel != this.mainPanel) subPanels.add(panel);
            }
            // close all non-main panels
            for (ModularPanel<?> panel : subPanels) {
                panel.closeIfOpen();
            }
            // finally close main panel
            this.mainPanel.closeIfOpen();
            return true;
        }
        return false;
    }

    void closeScreen() {
        // only close the screen without closing the panels
        // this is useful when we expect the screen to reopen at some point and the sync managers are still available
        if (this.state.isOpen) {
            setState(State.CLOSED);
            this.screen.onClose();
        }
    }

    private void finalizePanel(ModularPanel<?> panel) {
        if (panel.isOpen()) panel.onClose();
        if (!this.disposal.contains(panel)) {
            if (this.disposal.size() == DISPOSAL_CAPACITY) {
                this.disposal.removeFirst().dispose();
            }
            this.disposal.add(panel);
        }
    }

    public <T> T doSafe(Supplier<T> runnable) {
        if (isDisposed()) return null;
        this.cantDisposeNow = true;
        T t = runnable.get();
        this.cantDisposeNow = false;
        if (this.state == State.WAIT_DISPOSAL) {
            setState(State.CLOSED);
            dispose();
        }
        return t;
    }

    @ApiStatus.Internal
    public void dispose() {
        if (isDisposed()) return;
        if (this.state != State.CLOSED && this.state != State.WAIT_DISPOSAL) {
            throw new IllegalStateException("Must close screen first before disposing!");
        }
        if (this.cantDisposeNow) {
            setState(State.WAIT_DISPOSAL);
            return;
        }
        // make sure every panel gets closed before disposing
        this.panels.forEach(this::finalizePanel);
        setState(State.CLOSED);
        this.disposal.forEach(ModularPanel::dispose);
        this.disposal.clear();
        this.panels.clear();
        this.panelsClone.clear();
        this.dirty = false;
        setState(State.DISPOSED);
    }

    public boolean hasOpenPanel(ModularPanel<?> panel) {
        return this.panels.contains(panel);
    }

    public boolean hasPanelOpen(String name) {
        return getOpenPanel(name) != null;
    }

    public @Nullable ModularPanel<?> getOpenPanel(String name) {
        for (ModularPanel<?> panel : this.panels) {
            if (panel.getName().equals(name)) {
                return panel;
            }
        }
        return null;
    }

    public int getOpenPanelCount() {
        return this.panels.size();
    }

    public int getPanelIndex(ModularPanel<?> panel) {
        return this.panels.indexOf(panel);
    }

    public int getPanelIndexOrFail(ModularPanel<?> panel, String action) {
        int index = getPanelIndex(panel);
        if (index < 0) {
            throw new IllegalArgumentException("Failed to perform action '" + action + "' on panel '" + panel +
                    "', because it is not open in this screen");
        }
        return index;
    }

    public void pushUp(@NotNull ModularPanel<?> panel) {
        int index = getPanelIndexOrFail(panel, "push up");
        if (index == 0) return;
        movePanel(index, index - 1);
    }

    public void pushDown(@NotNull ModularPanel<?> panel) {
        int index = getPanelIndexOrFail(panel, "push down");
        if (index == this.panels.size() - 1) return;
        movePanel(index, index + 1);
    }

    public void pushToTop(@NotNull ModularPanel<?> window) {
        int index = getPanelIndexOrFail(window, "push to top");
        if (index == 0) return;
        movePanel(index, 0);
    }

    public void pushToBottom(@NotNull ModularPanel<?> window) {
        int index = getPanelIndexOrFail(window, "push to bottom");
        if (index == this.panels.size() - 1) return;
        movePanel(index, -1);
    }

    public void movePanelAbove(ModularPanel<?> panelToMove, ModularPanel<?> target) {
        int index = getPanelIndexOrFail(panelToMove, "move panel after");
        if (index == 0) return;
        int targetIndex = getTopSubPanelIndexOf(target);
        if (targetIndex < 0) {
            throw new IllegalArgumentException("Could not find target or a sub panel of '" + target + "'.");
        }
        movePanel(index, targetIndex);
    }

    public void movePanelBelow(ModularPanel<?> panelToMove, ModularPanel<?> target) {
        int index = getPanelIndexOrFail(panelToMove, "move panel after");
        if (index == this.panels.size() - 1) return;
        int targetIndex = getBottomSubPanelIndexOf(target);
        if (targetIndex < 0) {
            throw new IllegalArgumentException("Could not find target or a sub panel of '" + target + "'.");
        }
        movePanel(index, targetIndex + 1);
    }

    private void movePanel(int panelIndex, int target) {
        if (target < 0) target += this.panels.size();
        else if (panelIndex < target) target--;
        ModularPanel<?> panel = this.panels.remove(panelIndex);
        this.panels.add(target, panel);
        this.dirty = true;
    }

    private int getTopSubPanelIndexOf(ModularPanel<?> target) {
        int targetIndex = -1;
        for (int i = this.panels.size() - 1; i >= 0; i--) {
            ModularPanel<?> panel = this.panels.get(i);
            if (isSubPanelOf(panel, target)) {
                targetIndex = i;
                continue;
            }
            break;
        }
        return targetIndex;
    }

    private int getBottomSubPanelIndexOf(ModularPanel<?> target) {
        int targetIndex = -1;
        for (int i = 0; i < this.panels.size(); i++) {
            ModularPanel<?> panel = this.panels.get(i);
            if (isSubPanelOf(panel, target)) {
                targetIndex = i;
                continue;
            }
            break;
        }
        return targetIndex;
    }

    public boolean isSubPanelOf(ModularPanel<?> panel, ModularPanel<?> target) {
        if (panel == target) return true;
        IPanelHandler panelHandler = this.panelHandlerMap.get(panel.getName());
        while (panelHandler != null) {
            if (panelHandler instanceof SecondaryPanel secPanel) {
                if (secPanel.getParent() == target) {
                    return true;
                }
                panelHandler = this.panelHandlerMap.get(secPanel.getParent().getName());
            } else {
                break;
            }
        }
        return false;
    }

    @NotNull
    @UnmodifiableView
    public List<ModularPanel<?>> getOpenPanels() {
        checkDirty();
        return this.panelsView;
    }

    @NotNull
    @UnmodifiableView
    public Iterable<ModularPanel<?>> getReverseOpenPanels() {
        checkDirty();
        return this.reversePanels;
    }

    private void setState(State state) {
        this.state = Objects.requireNonNull(state);
    }

    public boolean isClosed() {
        return this.state == State.CLOSED || this.state == State.DISPOSED;
    }

    public boolean isDisposed() {
        return this.state == State.DISPOSED;
    }

    public boolean isOpen() {
        return this.state.isOpen;
    }

    public boolean isReopened() {
        return this.state == State.REOPENED;
    }

    private void checkDisposed() {
        if (isDisposed()) {
            throw new IllegalStateException("Screen is disposed!");
        }
    }

    public enum State {

        /**
         * Screen is created, but not yet opened.
         */
        INIT(false),
        /**
         * Screen is open after init, or after it was disposed and opened again.
         */
        OPEN(true),
        /**
         * Screen was closed, but is now open again.
         */
        REOPENED(true),
        /**
         * Screen is closed after it was open. Panels may still be considered open in some cases.
         */
        CLOSED(false),
        /**
         * Screen is closed and waiting to be disposed.
         */
        WAIT_DISPOSAL(false),
        /**
         * Screen is disposed. Screen can be reopened in this state, but every panel has to be rebuilt.
         */
        DISPOSED(false);

        public final boolean isOpen;

        State(boolean isOpen) {
            this.isOpen = isOpen;
        }
    }
}
