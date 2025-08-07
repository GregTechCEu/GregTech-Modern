package com.gregtechceu.gtceu.api.mui.base;

import com.gregtechceu.gtceu.api.mui.value.sync.ItemSlotSH;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.SecondaryPanel;

import org.jetbrains.annotations.ApiStatus;

/**
 * This class can handle opening and closing of a {@link ModularPanel}. It makes sure, that the same panel is not
 * created multiple
 * times and instead reused.
 * <p>
 * Using {@link #openPanel()} is the only way to open multiple panels.
 * </p>
 * <p>
 * Panels can be closed with {@link #closePanel()}, but also with {@link ModularPanel#closeIfOpen(boolean)} and
 * {@link ModularPanel#animateClose()}. With the difference, that the method from this interface also works on server
 * side.
 * </p>
 * Synced panels must be created with {@link PanelSyncManager#panel(String, PanelSyncHandler.IPanelBuilder, boolean)}.
 * If the panel does not contain any synced widgets, a simple panel handler using
 * {@link #simple(ModularPanel, SecondaryPanel.IPanelBuilder, boolean)}
 * is likely what you need.
 */
public interface IPanelHandler {

    /**
     * Creates a non synced panel handler. Trying to use synced values anyway will result in a crash.
     * It only works on client side. Doing anything with it on server side might result in a crash.
     *
     * @param parent   an existing parent panel of the gui
     * @param provider the panel builder, that will create the new panel. It must not return null or the main panel.
     * @param subPanel true if this panel should close when its parent closes (the parent is defined by the first
     *                 parameter)
     * @return a simple panel handler.
     * @throws NullPointerException     if the build panel of the builder is null
     * @throws IllegalArgumentException if the build panel of the builder is the main panel or there are synced values
     *                                  in the panel
     */
    static IPanelHandler simple(ModularPanel parent, SecondaryPanel.IPanelBuilder provider, boolean subPanel) {
        return new SecondaryPanel(parent, provider, subPanel);
    }

    boolean isPanelOpen();

    /**
     * Opens the panel. If there is no cached panel, one will be created.
     * Can be called on both sides if this handler is synced.
     */
    void openPanel();

    /**
     * Initiates the closing animation if the panel is open.
     * Can be called on both sides if this handler is synced.
     */
    void closePanel();

    /**
     * Initiates the closing animation of all sub panels.
     * Usually for internal use.
     */
    void closeSubPanels();

    /**
     * Called internally after the panel is closed.
     */
    @ApiStatus.OverrideOnly
    void closePanelInternal();

    /**
     * Deletes the current cached panel. Should not be used frequently.
     * This only works on panels which don't have {@link ItemSlotSH} sync handlers.
     *
     * @throws UnsupportedOperationException if this handler has ItemSlot sync handlers
     */
    void deleteCachedPanel();

    /**
     * If this is a sub panel of another panel. A sub panel will be closed when its parent is closed.
     *
     * @return true if this is a sub panel
     */
    boolean isSubPanel();
}
