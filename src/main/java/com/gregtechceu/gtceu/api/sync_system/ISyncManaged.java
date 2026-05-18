package com.gregtechceu.gtceu.api.sync_system;

import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;

import net.minecraftforge.common.util.INBTSerializable;

/**
 * Represents a class with fields that have sync annotations. <br>
 * Differs from {@link ISyncAnnotated} in that more control is provided over syncing. <br>
 * An {@link ISyncManaged} class manages the sync status of itself and its fields,
 * while a {@link ISyncAnnotated} must be managed by a field in an {@link ISyncManaged} class.
 * <p>
 * A field of type {@code T} can be marked with sync annotations if:
 * <ul>
 * <li>{@code T} is primitive
 * <li>{@code T} has an {@link ValueTransformer} registered
 * <li>{@code T} implements {@link INBTSerializable}
 * <li>{@code T} is an {@link ISyncManaged} or {@link ISyncAnnotated} class
 * </ul>
 *
 * @see SyncDataHolder
 * @see ISyncAnnotated
 */
public interface ISyncManaged {

    SyncDataHolder getSyncDataHolder();

    /**
     * Function called when a synced field requests a rerender
     */
    void scheduleRenderUpdate();

    /**
     * Function called to notify the server that this object has been updated and must be synced to clients
     */
    void markAsChanged();
}
