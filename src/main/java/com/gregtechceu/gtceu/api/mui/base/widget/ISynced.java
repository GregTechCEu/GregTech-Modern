package com.gregtechceu.gtceu.api.mui.base.widget;

import com.gregtechceu.gtceu.api.mui.base.value.ISyncOrValue;
import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;

import org.jetbrains.annotations.ApiStatus;
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
     * @param late        if this is called at any point after the panel this widget belongs to opened
     */
    void initialiseSyncHandler(ModularSyncManager syncManager, boolean late);

    /**
     * Returns if the given value or sync handler is valid for this widget. This is usually a call to
     * {@link ISyncOrValue#isTypeOrEmpty(Class)}. If the widget must specify a value (disallow null) instanceof check
     * can be used. You can
     * check for primitive types which don't have a dedicated {@link com.gregtechceu.gtceu.api.mui.base.value.IValue
     * IValue} interface with
     * {@link ISyncOrValue#isValueOfType(Class)}.
     *
     * @param syncOrValue a sync handler or a value, but never null
     * @return if the value or sync handler is valid for this class
     */
    default boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return false;
    }

    /**
     * Checks if the given sync handler is valid for this widget and throws an exception if not.
     * Override {@link #isValidSyncOrValue(ISyncOrValue)}
     *
     * @param syncHandler given sync handler
     * @throws IllegalStateException if the given sync handler is invalid for this widget.
     */
    @ApiStatus.NonExtendable
    default void checkValidSyncOrValue(ISyncOrValue syncHandler) {
        if (!isValidSyncOrValue(syncHandler)) {
            throw new IllegalStateException(
                    "SyncHandler of type '" + syncHandler.getClass().getSimpleName() + "' is not valid " +
                            "for widget '" + this + "'.");
        }
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
