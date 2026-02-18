package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;

import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DynamicLinkedSyncHandler<S extends ValueSyncHandler<?>> extends SyncHandler
                                     implements IDynamicSyncNotifiable {

    private IWidgetProvider<S> widgetProvider;
    private Consumer<IWidget> onWidgetUpdate;

    private IWidget lastRejectedWidget;

    private final S linkedValue;

    public DynamicLinkedSyncHandler(S linkedValue) {
        this.linkedValue = linkedValue;
        linkedValue.setChangeListener(() -> notifyUpdate(false));
    }

    @Override
    public void readOnClient(int id, FriendlyByteBuf buf) {
        if (id == 0) {
            updateWidget(parseWidget());
        }
    }

    @Override
    public void readOnServer(int id, FriendlyByteBuf buf) {
        if (id == 0) {
            // do nothing with the widget on server side
            parseWidget();
        }
    }

    @Override
    public void init(String key, PanelSyncManager syncManager) {
        super.init(key, syncManager);
        notifyUpdate(false);
    }

    private IWidget parseWidget() {
        getSyncManager().allowTemporarySyncHandlerRegistration(true);
        IWidget widget = this.widgetProvider.createWidget(getSyncManager(), this.linkedValue);
        getSyncManager().allowTemporarySyncHandlerRegistration(false);
        // collects any unregistered sync handlers
        // since the sync manager is currently locked and we no longer allow bypassing the lock it will crash if it
        // finds any
        int unregistered = WidgetTree.countUnregisteredSyncHandlers(getSyncManager(), widget);
        if (unregistered > 0) {
            throw new IllegalStateException(
                    "Widgets created by DynamicSyncHandler can't have implicitly registered sync handlers. All" +
                            "sync handlers must be registered with a variant of 'PanelSyncManager#getOrCreateSyncHandler(...)'.");
        }
        return widget;
    }

    private void updateWidget(IWidget widget) {
        if (this.onWidgetUpdate == null) {
            // no dynamic widget is yet attached
            // store for later
            // also ignore previous stored widget
            this.lastRejectedWidget = widget;
        } else {
            this.onWidgetUpdate.accept(widget);
        }
    }

    /**
     * Notifies the sync handler to create a new widget. It is allowed to call this method before this sync handler is
     * initialised.
     * The packet will be cached until the sync handler is initialised. Only the last call of this method, while this
     * sync handler is not
     * initialised is effective.
     */
    private void notifyUpdate(boolean sync) {
        if (!isValid()) return;
        IWidget widget = parseWidget();
        if (getSyncManager().isClient()) {
            updateWidget(widget);
        }
        if (sync) sync(0, b -> {});
    }

    /**
     * Sets a widget creator which is called on client and server. {@link SyncHandler}s can be created here using
     * {@link PanelSyncManager#getOrCreateSyncHandler(String, int, Class, Supplier)}. Returning null in the function
     * will not update the widget.
     * On client side the result is handed over to a linked
     * {@link com.gregtechceu.gtceu.api.mui.widgets.DynamicSyncedWidget}.
     *
     * @param widgetProvider the widget creator function
     * @return this
     * @see IWidgetProvider
     */
    public DynamicLinkedSyncHandler<S> widgetProvider(IWidgetProvider<S> widgetProvider) {
        this.widgetProvider = widgetProvider;
        return this;
    }

    /**
     * An internal function which is used to link the {@link com.gregtechceu.gtceu.api.mui.widgets.DynamicSyncedWidget}.
     */
    @ApiStatus.Internal
    @Override
    public void attachDynamicWidgetListener(Consumer<IWidget> onWidgetUpdate) {
        this.onWidgetUpdate = onWidgetUpdate;
        if (this.onWidgetUpdate != null && this.lastRejectedWidget != null) {
            this.onWidgetUpdate.accept(this.lastRejectedWidget);
            this.lastRejectedWidget = null;
        }
    }

    public interface IWidgetProvider<S extends ValueSyncHandler<?>> {

        /**
         * This is the function which creates a widget on client and server.
         * In this method sync handlers can only be registered with
         * {@link PanelSyncManager#getOrCreateSyncHandler(String, int, Class, Supplier)}.
         *
         * @param syncManager the sync manager of the current panel
         * @param value       the linked sync value
         * @return a new widget or null if widget shouldn't be updated
         */
        @Nullable
        IWidget createWidget(PanelSyncManager syncManager, S value);
    }
}
