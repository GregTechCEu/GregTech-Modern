package com.gregtechceu.gtceu.api.mui.base.widget;

import com.gregtechceu.gtceu.api.mui.value.sync.GenericSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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
     * @param late        true if this is called some time after the widget tree of the parent has been initialised
     */
    void initialiseSyncHandler(ModularSyncManager syncManager, boolean late);

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
        return castIfTypeElseNull(syncHandler, clazz, null);
    }

    @SuppressWarnings("unchecked")
    default <T> T castIfTypeElseNull(SyncHandler syncHandler, Class<T> clazz, @Nullable Consumer<T> setup) {
        if (syncHandler != null && clazz.isAssignableFrom(syncHandler.getClass())) {
            T t = (T) syncHandler;
            if (setup != null) setup.accept(t);
            return t;
        }
        return null;
    }

    default <T> GenericSyncValue<T> castIfTypeGenericElseNull(SyncHandler syncHandler, Class<T> clazz) {
        return castIfTypeGenericElseNull(syncHandler, clazz, null);
    }

    default <T> GenericSyncValue<T> castIfTypeGenericElseNull(SyncHandler syncHandler, Class<T> clazz,
                                                              @Nullable Consumer<GenericSyncValue<T>> setup) {
        if (syncHandler instanceof GenericSyncValue<?> genericSyncValue && genericSyncValue.isOfType(clazz)) {
            GenericSyncValue<T> t = genericSyncValue.cast();
            if (setup != null) setup.accept(t);
            return t;
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
     * {@link #initialiseSyncHandler(ModularSyncManager, boolean)}
     *
     * @param name sync handler key name
     * @param id   sync handler key id
     * @return this
     */
    W syncHandler(String name, int id);

    /**
     * Sets the sync handler key. The sync handler will be obtained in
     * {@link #initialiseSyncHandler(ModularSyncManager, boolean)}
     *
     * @param key sync handler name
     * @return this
     */
    default W syncHandler(String key) {
        return syncHandler(key, 0);
    }

    /**
     * Sets the sync handler key. The sync handler will be obtained in
     * {@link #initialiseSyncHandler(ModularSyncManager, boolean)}
     *
     * @param id sync handler id
     * @return this
     */
    default W syncHandler(int id) {
        return syncHandler("_", id);
    }
}
