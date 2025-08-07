package com.gregtechceu.gtceu.client.mui.screen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.api.mui.widget.wrapper.WidgetWrapper;
import com.gregtechceu.gtceu.client.mui.screen.viewport.LocatedWidget;
import com.gregtechceu.gtceu.utils.ReverseIterable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Supplier;

public class PanelManager {

    @Getter
    private final @NotNull ModularScreen screen;
    /**
     * At least one panel must exist always exist.
     * If this panel is closed, all panels will close.
     */
    private final ModularPanel mainPanel;
    /**
     * List of all open panels from top to bottom.
     */
    private final ObjectList<ModularPanel> panels = new ObjectArrayList<>();
    // a clone of the list to avoid CMEs
    private final List<ModularPanel> panelsClone = new ArrayList<>();
    private final List<ModularPanel> panelsView = Collections.unmodifiableList(this.panelsClone);
    private final ReverseIterable<ModularPanel> reversePanels = new ReverseIterable<>(this.panelsView);
    private final List<WidgetWrapper> panelWrappers = new ArrayList<>();
    private final List<WidgetWrapper> panelWrappersView = Collections.unmodifiableList(this.panelWrappers);
    private final ReverseIterable<WidgetWrapper> reversePanelWrappers = new ReverseIterable<>(this.panelWrappersView);
    private final ObjectList<ModularPanel> disposal = new ObjectArrayList<>(20);
    private final Map<String, IPanelHandler> panelHandlerMap = new Object2ObjectOpenHashMap<>();
    private boolean cantDisposeNow = false;
    private boolean dirty = false;
    private State state = State.INIT;

    public PanelManager(@NotNull ModularScreen screen, ModularPanel panel) {
        this.screen = screen;
        this.mainPanel = Objects.requireNonNull(panel, "Main panel must not be null!");
    }

    boolean tryInit() {
        if (this.state == State.CLOSED) throw new IllegalStateException("Can't init in closed state!");
        if (this.state == State.INIT || this.state == State.DISPOSED) {
            setState(State.OPEN);
            openPanel(this.mainPanel, false);
            checkDirty();
            return true;
        }
        return false;
    }

    public boolean isMainPanel(ModularPanel panel) {
        return this.mainPanel == panel;
    }

    void checkDirty() {
        if (this.dirty) {
            this.panelsClone.clear();
            this.panelsClone.addAll(this.panels);

            this.panelWrappers.clear();
            this.panelsClone.stream()
                    .map(WidgetWrapper::new)
                    .forEach(this.panelWrappers::add);

            this.dirty = false;
        }
    }

    private void openPanel(ModularPanel panel, boolean resize) {
        if (this.panels.size() == 127) {
            throw new IllegalStateException("Too many panels are open!");
        }
        if (this.panels.contains(panel) || isPanelOpen(panel.getName())) {
            throw new IllegalStateException("Panel " + panel.getName() + " is already open.");
        }
        this.disposal.remove(panel);
        panel.setPanelGuiContext(this.screen.getContext());
        this.panels.add(0, panel);
        this.dirty = true;
        panel.getArea().setPanelLayer((byte) this.panels.size());
        panel.onOpen(this.screen);
        if (resize) {
            WidgetTree.resize(panel);
        }
    }

    public boolean isPanelOpen(String name) {
        for (ModularPanel panel : this.panels) {
            if (panel.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public ModularPanel getMainPanel() {
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
    public ModularPanel getTopMostPanel() {
        return this.panels.get(0);
    }

    @Nullable
    public IWidget getTopWidget() {
        for (ModularPanel panel : this.panels) {
            IWidget widget = panel.getTopHovering();
            if (widget != null) {
                return widget;
            }
        }
        return null;
    }

    @Nullable
    public LocatedWidget getTopWidgetLocated(boolean debug) {
        for (ModularPanel panel : this.panels) {
            LocatedWidget widget = panel.getTopHoveringLocated(debug);
            if (widget != null) {
                return widget;
            }
        }
        return null;
    }

    @ApiStatus.Internal
    public void openPanel(@NotNull ModularPanel panel, @NotNull IPanelHandler panelHandler) {
        IPanelHandler existing = this.panelHandlerMap.get(panel.getName());
        if (existing == null) {
            this.panelHandlerMap.put(panel.getName(), panelHandler);
        } else if (existing != panelHandler) {
            GTCEu.LOGGER.error(
                    "Tried to open a panel, but a panel handler that opens the same panel already exists. Using existing panel handler!");
            existing.openPanel();
            return;
        }
        openPanel(panel, true);
    }

    public void closePanel(@NotNull ModularPanel panel) {
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

    public void closeTopPanel(boolean animate) {
        getTopMostPanel().closeIfOpen(animate);
    }

    public boolean closeAll() {
        if (this.state.isOpen) {
            this.panels.forEach(this::finalizePanel);
            setState(State.CLOSED);
            this.screen.onClose();
            return true;
        }
        return false;
    }

    private void finalizePanel(ModularPanel panel) {
        panel.onClose();
        if (!this.disposal.contains(panel)) {
            if (this.disposal.size() == 20) {
                this.disposal.remove(0).dispose();
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
        if (this.cantDisposeNow) {
            setState(State.WAIT_DISPOSAL);
            return;
        }
        if (!isClosed()) throw new IllegalStateException("Must close screen first before disposing!");
        this.disposal.forEach(ModularPanel::dispose);
        this.disposal.clear();
        this.panels.clear();
        this.panelsClone.clear();
        this.panelWrappers.clear();
        this.dirty = false;
        setState(State.DISPOSED);
    }

    @ApiStatus.Internal
    public void reopen() {
        if (this.panels.isEmpty()) {
            throw new IllegalStateException("Screen is disposed. Can't be recovered!");
        }
        this.panels.forEach(ModularPanel::reopen);
        this.disposal.removeIf(this.panels::contains);
        setState(State.REOPENED);
    }

    public boolean hasOpenPanel(ModularPanel panel) {
        return this.panels.contains(panel);
    }

    public void pushUp(@NotNull ModularPanel window) {
        int index = this.panels.indexOf(window);
        if (index < 0) throw new IllegalStateException();
        if (index == 0) return;
        this.panels.remove(index);
        this.panels.add(index - 1, window);
    }

    public void pushDown(@NotNull ModularPanel window) {
        int index = this.panels.indexOf(window);
        if (index < 0) throw new IllegalStateException();
        if (index == this.panels.size() - 1) return;
        this.panels.remove(index);
        this.panels.add(index + 1, window);
    }

    public void pushToTop(@NotNull ModularPanel window) {
        int index = this.panels.indexOf(window);
        if (index < 0) throw new IllegalStateException();
        if (index == 0) return;
        this.panels.remove(index);
        this.panels.add(0, window);
    }

    public void pushToBottom(@NotNull ModularPanel window) {
        int index = this.panels.indexOf(window);
        if (index < 0) throw new IllegalStateException();
        if (index == this.panels.size() - 1) return;
        this.panels.remove(index);
        this.panels.add(window);
    }

    @NotNull
    @UnmodifiableView
    public List<ModularPanel> getOpenPanels() {
        checkDirty();
        return this.panelsView;
    }

    @NotNull
    @UnmodifiableView
    public Iterable<ModularPanel> getReverseOpenPanels() {
        checkDirty();
        return this.reversePanels;
    }

    @NotNull
    @UnmodifiableView
    public List<WidgetWrapper> getOpenPanelsWrappers() {
        checkDirty();
        return this.panelWrappersView;
    }

    @NotNull
    @UnmodifiableView
    public Iterable<WidgetWrapper> getReverseOpenPanelsWrappers() {
        checkDirty();
        return this.reversePanelWrappers;
    }

    private void setState(State state) {
        this.state = state;
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

        INIT(false),
        OPEN(true),
        REOPENED(true),
        CLOSED(false),
        WAIT_DISPOSAL(true),
        DISPOSED(false);

        public final boolean isOpen;

        State(boolean isOpen) {
            this.isOpen = isOpen;
        }
    }
}
