package com.gregtechceu.gtceu.api.sync_system.managed;

import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;

import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a class with fields that have sync annotations. <br>
 * <p>
 * A field of type {@code T} can be marked with sync annotations if:
 * <ul>
 * <li>{@code T} is primitive
 * <li>{@code T} has an {@link ValueTransformer} registered
 * <li>{@code T} implements {@link INBTSerializable}
 * <li>{@code T} is an {@link ISyncManaged} class
 * </ul>
 *
 * @see SyncDataHolder
 */
public interface ISyncManaged {

    SyncDataHolder getSyncDataHolder();

    /**
     * Gets the parent sync object of this sync object
     * 
     * @return The parent sync object, can only return null if this object does not have a parent sync object and both
     *         {@link #scheduleRenderUpdate()} and {@link #markAsChanged()} are overriden
     */
    @Nullable
    ISyncManaged getParentSyncObject();

    /**
     * Function called when a synced field requests a rerender
     */
    default void scheduleRenderUpdate() {
        if (getParentSyncObject() != null) getParentSyncObject().scheduleRenderUpdate();
    }

    /**
     * Function called to notify the server that this object has been updated and must be synced to clients
     */
    default void markAsChanged() {
        if (getParentSyncObject() != null) getParentSyncObject().markAsChanged();
    }
}
