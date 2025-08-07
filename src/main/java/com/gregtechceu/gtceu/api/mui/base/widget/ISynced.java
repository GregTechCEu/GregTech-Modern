package com.gregtechceu.gtceu.api.mui.base.widget;

import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;

import org.jetbrains.annotations.NotNull;

/**
 * Marks a widget as synced
 *
 * @param <W> widget type
 */
public interface ISynced<W extends IWidget> {

    /**
     * @return this cast to the true widget type
     */
    @SuppressWarnings("unchecked")
    default W getThis() {
        return (W) this;
    }

    /**
     * Called when this widget gets initialised or when this widget is added to the gui
     *
     * @param syncManager sync manager
     */
    void initialiseSyncHandler(ModularSyncManager syncManager);

    /**
     * Checks if the received sync handler is valid for this widget.
     * <b>Synced widgets must override this!</b>
     *
     * @param syncHandler received sync handler
     * @return true if sync handler is valid
     */
    default boolean isValidSyncHandler(SyncHandler syncHandler) {
        return false;
    }

    default <T> T castIfTypeElseNull(SyncHandler syncHandler, Class<T> clazz) {
        if (syncHandler != null && clazz.isAssignableFrom(syncHandler.getClass())) {
            return (T) syncHandler;
        }
        return null;
    }

    /**
     * @return true if this widget has a valid sync handler
     */
    boolean isSynced();

    /**
     * @return the sync handler of this widget
     * @throws IllegalStateException if this widget has no valid sync handler
     */
    @NotNull
    SyncHandler getSyncHandler();

    /**
     * Sets the sync handler key. The sync handler will be obtained in
     * {@link #initialiseSyncHandler(ModularSyncManager)}
     *
     * @param name sync handler key name
     * @param id   sync handler key id
     * @return this
     */
    W syncHandler(String name, int id);

    /**
     * Sets the sync handler key. The sync handler will be obtained in
     * {@link #initialiseSyncHandler(ModularSyncManager)}
     *
     * @param key sync handler name
     * @return this
     */
    default W syncHandler(String key) {
        return syncHandler(key, 0);
    }

    /**
     * Sets the sync handler key. The sync handler will be obtained in
     * {@link #initialiseSyncHandler(ModularSyncManager)}
     *
     * @param id sync handler id
     * @return this
     */
    default W syncHandler(int id) {
        return syncHandler("_", id);
    }
}
