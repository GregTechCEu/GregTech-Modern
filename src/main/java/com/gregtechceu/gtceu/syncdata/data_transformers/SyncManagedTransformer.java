package com.gregtechceu.gtceu.syncdata.data_transformers;

import com.gregtechceu.gtceu.syncdata.ISyncManaged;
import com.gregtechceu.gtceu.syncdata.IValueTransformer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

public class SyncManagedTransformer implements IValueTransformer<ISyncManaged> {

    @Override
    public boolean mustProvideObject() {
        return true;
    }

    @Override
    public Tag serializeNBT(ISyncManaged value, boolean isSync, boolean isFullSync) {
        return isSync ? value.getSyncDataHolder().getClientSyncNBT(isFullSync) : value.getSyncDataHolder().saveNBT();
    }

    @Override
    public ISyncManaged deserializeNBT(Tag tag, @Nullable ISyncManaged currentVal, boolean isSync) {
        if (!(tag instanceof CompoundTag compound) || currentVal == null) return currentVal;
        if (isSync) {
            currentVal.getSyncDataHolder().loadClientSyncNBT(compound);
        } else {
            currentVal.getSyncDataHolder().loadFromNBT(compound);
        }
        return currentVal;
    }
}
