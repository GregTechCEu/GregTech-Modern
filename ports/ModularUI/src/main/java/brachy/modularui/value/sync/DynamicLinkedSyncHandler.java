package brachy.modularui.value.sync;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.widget.WidgetTree;

import brachy.modularui.widgets.dynamic.IDynamicHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This is a variation of {@link DynamicSyncHandler} with the difference that this is linked to a {@link ValueSyncHandler}.
 * This sync handler is automatically notified, when the linked value is updated. The widget provider here has the linked sync handler as an
 * argument instead of a packet.
 * To use it simply pass in a registered value sync handler into the constructor and link it to a
 * {@link brachy.modularui.widgets.dynamic.DynamicWidget DynamicWidget}.
 */
public class DynamicLinkedSyncHandler<B extends ByteBuf, S extends ValueSyncHandler<B, ?, ?>> extends SyncHandler<DynamicLinkedSyncHandler<B, S>> implements IDynamicHandler {

    private IWidgetProvider<B, S> widgetProvider;
    private Consumer<IWidget> onWidgetUpdate;

    private IWidget lastRejectedWidget;

    private final S linkedValue;

    public DynamicLinkedSyncHandler(S linkedValue) {
        this.linkedValue = linkedValue;
        linkedValue.setChangeListener(() -> notifyUpdate(false));
        allowC2S();
    }

    @Override
    public void readOnClient(int id, RegistryFriendlyByteBuf buf) {
        if (id == 0) {
            updateWidget(parseWidget());
        }
    }

    @Override
    public void readOnServer(int id, RegistryFriendlyByteBuf buf) {
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
     * On client side the result is handed over to a linked {@link brachy.modularui.widgets.dynamic.DynamicWidget DynamicWidget}.
     *
     * @param widgetProvider the widget creator function
     * @return this
     * @see IWidgetProvider
     */
    public DynamicLinkedSyncHandler<B, S> widgetProvider(IWidgetProvider<B, S> widgetProvider) {
        this.widgetProvider = widgetProvider;
        return this;
    }

    /**
     * An internal function which is used to link the {@link brachy.modularui.widgets.dynamic.DynamicWidget DynamicWidget}.
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

    public interface IWidgetProvider<B extends ByteBuf, S extends ValueSyncHandler<B, ?, ?>> {

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
