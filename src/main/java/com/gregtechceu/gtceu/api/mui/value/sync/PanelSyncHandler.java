package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.widget.ISynced;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;

import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * If you want another panel where some widgets may be able to sync data, you will need this.
 * Register it in any {@link PanelSyncManager} (preferably the main one).
 * Then you can call {@link #openPanel()} and {@link #closePanel()} from any side.
 */
public final class PanelSyncHandler extends SyncHandler implements IPanelHandler {

    public static final int SYNC_NOTIFY_OPEN = 0;
    public static final int SYNC_OPEN = 1;
    public static final int SYNC_CLOSE = 2;
    public static final int SYNC_DISPOSE = 3;

    private final IPanelBuilder panelBuilder;
    private final boolean subPanel;
    private String panelName;
    private ModularPanel openedPanel;
    private PanelSyncManager syncManager;
    private boolean open = false;

    /**
     * Creates a PanelSyncHandler
     *
     * @param panelBuilder a panel builder function
     */
    PanelSyncHandler(IPanelBuilder panelBuilder, boolean subPanel) {
        this.panelBuilder = panelBuilder;
        this.subPanel = subPanel;
    }

    public ModularPanel createUI(PanelSyncManager syncManager) {
        return this.panelBuilder.buildUI(syncManager, this);
    }

    @Override
    public void openPanel() {
        openPanel(true);
    }

    private void openPanel(boolean syncToServer) {
        if (isPanelOpen()) return;
        boolean client = getSyncManager().isClient();
        if (syncToServer && client) {
            syncToServer(SYNC_NOTIFY_OPEN);
            return;
        }
        if (this.syncManager != null &&
                this.syncManager.getModularSyncManager() != getSyncManager().getModularSyncManager()) {
            throw new IllegalStateException("Can't reopen synced panel in another screen!");
        } else if (this.syncManager == null) {
            this.syncManager = new PanelSyncManager(getSyncManager().getModularSyncManager(), false);
            this.openedPanel = Objects.requireNonNull(createUI(this.syncManager));
            this.panelName = this.openedPanel.getName();
            this.openedPanel.setPanelSyncHandler(this);
            WidgetTree.collectSyncValues(this.syncManager, this.openedPanel, false);
            if (!client) {
                this.openedPanel = null;
            }
        }
        if (client) {
            ModularScreen screen = getSyncManager().getContainer().getScreen();
            if (!screen.isPanelOpen(this.openedPanel.getName())) {
                openInModularSyncManager();
                screen.getPanelManager().openPanel(this.openedPanel, this);
            } else {
                // this was not supposed to happen
                // make sure server side also closes the panel
                closePanelInternal();
                return;
            }
        } else {
            openInModularSyncManager();
        }
        this.open = true;
    }

    private void openInModularSyncManager() {
        getSyncManager().getModularSyncManager().open(this.panelName, this.syncManager);
    }

    @Override
    public void closePanel() {
        if (getSyncManager().isClient()) {
            if (this.openedPanel != null) {
                this.openedPanel.closeIfOpen();
            }
        } else {
            syncToClient(SYNC_CLOSE);
        }
    }

    @Override
    public void closeSubPanels() {
        this.syncManager.closeSubPanels();
    }

    @ApiStatus.Internal
    @Override
    public void closePanelInternal() {
        getSyncManager().getModularSyncManager().close(this.panelName);
        this.open = false;
        if (getSyncManager().isClient()) {
            syncToServer(SYNC_CLOSE);
        }
    }

    @Override
    public void deleteCachedPanel() {
        if (openedPanel == null || isPanelOpen()) return;
        boolean canDispose = WidgetTree.foreachChild(openedPanel, iWidget -> {
            if (iWidget instanceof ISynced<?> synced && synced.isSynced()) {
                return !(synced.getSyncHandler() instanceof ItemSlotSyncHandler);
            }
            return true;
        }, false);

        // This is because we can't guarantee that the sync handlers of the new panel are the same.
        // Dynamic sync handler changing is very error-prone.
        if (!canDispose) {
            throw new UnsupportedOperationException(
                    "Can't delete cached panel if it's still open or has ItemSlot Sync Handlers!");
        }
        disposePanel();

        sync(SYNC_DISPOSE);
    }

    private void disposePanel() {
        this.panelName = null;
        this.syncManager = null;
        this.openedPanel = null;
    }

    @Override
    public boolean isSubPanel() {
        return subPanel;
    }

    @Override
    public boolean isPanelOpen() {
        return this.open;
    }

    @Override
    public void readOnClient(int i, FriendlyByteBuf packetBuffer) {
        if (i == SYNC_OPEN) {
            openPanel(false);
        } else if (i == SYNC_CLOSE) {
            closePanel();
        } else if (i == SYNC_DISPOSE) {
            disposePanel();
        }
    }

    @Override
    public void readOnServer(int i, FriendlyByteBuf packetBuffer) {
        if (i == SYNC_NOTIFY_OPEN) {
            openPanel(false);
            syncToClient(SYNC_OPEN);
        } else if (i == SYNC_CLOSE) {
            closePanelInternal();
        } else if (i == SYNC_DISPOSE) {
            disposePanel();
        }
    }

    /**
     * A function which creates a secondary {@link ModularPanel}
     */
    public interface IPanelBuilder {

        /**
         * Creates a {@link ModularPanel}. It must NOT return null or the main panel.
         *
         * @param syncManager the sync manager for this panel
         * @param syncHandler the sync handler that sync opening and closing of this panel
         * @return the created panel
         */
        @NotNull
        ModularPanel buildUI(@NotNull PanelSyncManager syncManager, @NotNull IPanelHandler syncHandler);
    }
}
