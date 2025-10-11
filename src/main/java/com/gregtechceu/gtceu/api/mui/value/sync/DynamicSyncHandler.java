package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.api.mui.base.IPacketWriter;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;

import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This sync handler calls a function on client and server which creates a widget after being notified. The widget is
 * then handed over to a
 * linked {@link DynamicSyncedWidget}.
 */
public class DynamicSyncHandler extends SyncHandler {

    private IWidgetProvider widgetProvider;
    private Consumer<IWidget> onWidgetUpdate;

    @Override
    public void readOnClient(int id, FriendlyByteBuf buf) {
        if (id == 0) {
            updateWidgets(buf);
        }
    }

    @Override
    public void readOnServer(int id, FriendlyByteBuf buf) {
        if (id == 0) {
            updateWidgets(buf);
        }
    }

    private void updateWidgets(FriendlyByteBuf buf) {
        getSyncManager().allowTemporarySyncHandlerRegistration(true);
        IWidget widget = widgetProvider.createWidget(getSyncManager(), buf);
        getSyncManager().allowTemporarySyncHandlerRegistration(false);
        // collects any unregistered sync handlers
        // since the sync manager is currently locked and we no longer allow bypassing the lock it will crash if it
        // finds any
        WidgetTree.collectSyncValues(getSyncManager(), getSyncManager().getPanelName(), widget, true);
        if (widget != null && this.widgetProvider != null) {
            this.onWidgetUpdate.accept(widget);
        }
    }

    /**
     * Notifies the sync handler to create a new widget.
     *
     * @param packetWriter data to pass to the function
     */
    public void notifyUpdate(IPacketWriter packetWriter) {
        updateWidgets(packetWriter.toPacket());
        sync(0, packetWriter);
    }

    /**
     * Sets a widget creator which is called on client and server. {@link SyncHandler}s can be created here using
     * {@link PanelSyncManager#getOrCreateSyncHandler(String, int, Class, Supplier)}. Returning null in the function
     * will not update the widget.
     * On client side the result is handed over to a linked
     * {@link com.cleanroommc.modularui.widgets.DynamicSyncedWidget}.
     *
     * @param widgetProvider the widget creator function
     * @return this
     * @see IWidgetProvider
     */
    public DynamicSyncHandler widgetProvider(IWidgetProvider widgetProvider) {
        this.widgetProvider = widgetProvider;
        return this;
    }

    /**
     * An internal function which is used to link the {@link com.cleanroommc.modularui.widgets.DynamicSyncedWidget}.
     */
    @ApiStatus.Internal
    public DynamicSyncHandler onWidgetUpdate(Consumer<IWidget> onWidgetUpdate) {
        this.onWidgetUpdate = onWidgetUpdate;
        return this;
    }

    public interface IWidgetProvider {

        /**
         * This is the function which creates a widget on client and server.
         * In this method sync handlers can only be registered with
         * {@link PanelSyncManager#getOrCreateSyncHandler(String, int, Class, Supplier)}.
         *
         * @param syncManager the sync manager of the current panel
         * @param buf         data which was passed in the notify method
         * @return a new widget or null if widget shouldn't be updated
         */
        @Nullable
        IWidget createWidget(PanelSyncManager syncManager, FriendlyByteBuf buf);
    }
}
