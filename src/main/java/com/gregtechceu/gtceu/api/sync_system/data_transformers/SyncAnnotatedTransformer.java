package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.api.sync_system.ClassSyncData;
import com.gregtechceu.gtceu.api.sync_system.FieldSyncData;
import com.gregtechceu.gtceu.api.sync_system.FieldSyncHandler;
import com.gregtechceu.gtceu.api.sync_system.ISyncAnnotated;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public class SyncAnnotatedTransformer implements ValueTransformer<ISyncAnnotated> {

    @Override
    public Tag serializeNBT(ISyncAnnotated value, TransformerContext<ISyncAnnotated> context) {
        var syncData = ClassSyncData.getClassData(value.getClass());

        Set<FieldSyncData> fieldsToSerialize = context.isClientSync() ? syncData.getClientSyncFields() :
                syncData.getServerSaveFields();

        CompoundTag tag = new CompoundTag();
        for (var field : fieldsToSerialize) {
            Tag nbtValue = FieldSyncHandler.serializeField(context.holder(), field, context.isClientSync(), true);
            tag.put(field.nbtSaveKey, nbtValue);
        }

        return tag;
    }

    @Override
    @SneakyThrows
    public @Nullable ISyncAnnotated deserializeNBT(Tag tag, TransformerContext<ISyncAnnotated> context) {
        if (!(tag instanceof CompoundTag compound)) return null;

        var typeData = Objects.requireNonNull(context.type().getClassValue());

        var syncData = ClassSyncData.getClassData(typeData);

        Set<FieldSyncData> fieldsToCheck = context.isClientSync() ? syncData.getClientSyncFields() :
                syncData.getServerSaveFields();

        var holder = context.currentValue();
        if (holder == null) holder = (ISyncAnnotated) typeData.getConstructor().newInstance();

        for (var field : fieldsToCheck) {
            Tag savedValue = compound.get(field.nbtSaveKey);
            FieldSyncHandler.deserializeField(holder, field, savedValue, context.isClientSync());
        }

        return holder;
    }
}
