package com.gregtechceu.gtceu.api.sync_system.managed;

import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link SavedData} object that stores and loads its data via the sync system.<br>
 * {@link ManagedSavedData} is not synced to clients.
 */
public abstract class ManagedSavedData extends SavedData implements ISyncManaged {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    public ManagedSavedData() {}

    public ManagedSavedData(CompoundTag tag) {
        getSyncDataHolder().deserializeNBT(tag, false);
    }

    @Override
    public @Nullable ISyncManaged getParentSyncObject() {
        return null;
    }

    // No functionality, not synced to clients
    @Override
    public void markAsChanged() {}

    // No functionality, not synced to clients
    @Override
    public void scheduleRenderUpdate() {}

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        return getSyncDataHolder().serializeNBT(false);
    }
}
