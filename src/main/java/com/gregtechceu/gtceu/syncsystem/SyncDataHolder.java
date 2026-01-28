package com.gregtechceu.gtceu.syncsystem;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.util.*;

/**
 * Class that holds all sync info for an {@link ISyncManaged} object.
 */
public class SyncDataHolder {

    private final ClassSyncData syncData;
    private final ISyncManaged holder;

    private final ObjectSet<String> dirtySyncFields = new ObjectOpenHashSet<>();
    private boolean resyncAll = false;

    public SyncDataHolder(ISyncManaged o) {
        holder = o;
        syncData = ClassSyncData.getClassData(o.getClass());
    }

    /**
     * Instructs the sync system that this field has been updated and must be synced with clients.
     * 
     * @param fieldName The field that has changed.
     */
    public void markClientSyncFieldDirty(String fieldName) {
        dirtySyncFields.add(fieldName);
        holder.markAsChanged();
    }

    public void resyncAllFields() {
        resyncAll = true;
        holder.markAsChanged();
    }

    public CompoundTag serializeNBT(boolean writeClientFields) {
        CompoundTag tag = serializeNBT(writeClientFields, resyncAll);
        resyncAll = false;
        dirtySyncFields.clear();
        return tag;
    }

    public CompoundTag serializeNBT(boolean writeClientFields, boolean fullSync) {
        Map<String, FieldSyncData> fieldsToSerialize;
        if (!writeClientFields) {
            fieldsToSerialize = syncData.getServerSaveFields();
        } else {
            fieldsToSerialize = syncData.getClientSyncFields();
        }

        CompoundTag tag = new CompoundTag();
        for (var fieldEntry : fieldsToSerialize.entrySet()) {

            if (writeClientFields &&
                    (!fullSync && !dirtySyncFields.contains(fieldEntry.getKey()) &&
                            !fieldEntry.getValue().isSyncManaged))
                continue;
            var field = fieldEntry.getValue();

            Tag nbtValue = serializeField(holder, field, writeClientFields);
            tag.put(field.nbtSaveKey, nbtValue);
        }
        return tag;
    }

    public void deserializeNBT(CompoundTag tag, boolean readingClientFields) {
        Map<String, FieldSyncData> fieldsToCheck = readingClientFields ? syncData.getClientSyncFields() :
                syncData.getServerSaveFields();

        for (var fieldEntry : fieldsToCheck.entrySet()) {

            var field = fieldEntry.getValue();

            Tag savedValue = tag.get(field.nbtSaveKey);
            deserializeField(holder, field, savedValue, readingClientFields);

            if (readingClientFields) {
                try {
                    for (MethodHandle changeListenerHandle : field.changeListenerHandles) {
                        changeListenerHandle.invoke(holder);
                    }
                } catch (Throwable e) {
                    if (e instanceof WrongMethodTypeException) {
                        throw new IllegalArgumentException(
                                "Invalid method signature for change listener for field %s %s"
                                        .formatted(field.fieldName, holder.getClass().getName()));
                    }
                    GTCEu.LOGGER.error("Sync: Error while invoking change listener for field {}", field.fieldName);
                    GTCEu.LOGGER.error(e);
                }

                if (field.triggerClientRerender) holder.scheduleRenderUpdate();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Tag serializeField(ISyncManaged holder, FieldSyncData field,
                                      boolean writeClientFields) {
        Object currentValue = field.handle.get(holder);

        if (!field.isSyncManaged && currentValue == null) {
            var nullCompound = new CompoundTag();
            nullCompound.putBoolean("null", true);
            return nullCompound;
        }

        try {

            if (field.transformer != null) {
                return ((ValueTransformer<Object>) field.transformer).serializeNBT(currentValue,
                        new ValueTransformer.TransformerContext<>(holder, field.clazz, field.genericArgs, currentValue,
                                field.fieldName,
                                writeClientFields));
            } else if (currentValue instanceof ISyncManaged syncObj) {
                return syncObj.getSyncDataHolder().serializeNBT(writeClientFields);
            } else {
                GTCEu.LOGGER.error("Sync: Failed to serialize field {}: Missing value transformer", field.fieldName);
            }

        } catch (Exception e) {
            GTCEu.LOGGER.error("Sync: Failed to serialize field {}", field.fieldName);
            GTCEu.LOGGER.error(e);
        }

        return new CompoundTag();
    }

    @SuppressWarnings("unchecked")
    private static void deserializeField(ISyncManaged holder, FieldSyncData field,
                                         @Nullable Tag savedValue,
                                         boolean readingClientFields) {
        Object currentVal = field.handle.get(holder);

        if (savedValue == null || savedValue instanceof CompoundTag compound && compound.isEmpty()) return;

        if (savedValue instanceof CompoundTag compound && compound.getBoolean("null")) {
            field.handle.set(holder, null);
            return;
        }

        try {
            if (field.transformer != null) {
                ValueTransformer<Object> transformer = (ValueTransformer<Object>) field.transformer;
                try {
                    var current = field.handle.get(holder);
                    Object result = transformer.deserializeNBT(savedValue, new ValueTransformer.TransformerContext<>(
                            holder, field.clazz, field.genericArgs, current, field.fieldName, readingClientFields));
                    if (result != current) {
                        field.handle.set(holder, result);
                    }
                } catch (UnsupportedOperationException e) {
                    GTCEu.LOGGER.error("Sync: failed to perform VarHandle set: unsupported op {} {}",
                            field.fieldName, field.handle.toString());
                }
            } else if (field.isSyncManaged && savedValue instanceof CompoundTag compound) {
                if (currentVal == null) {
                    GTCEu.LOGGER.error("Sync: ISyncManaged field was null, cannot instantiate {}",
                            field.fieldName);
                    return;
                }
                if (currentVal instanceof ISyncManaged syncObj)
                    syncObj.getSyncDataHolder().deserializeNBT(compound, readingClientFields);
            } else {
                GTCEu.LOGGER.error("Sync: Failed to deserialize field {}: Missing value transformer", field.fieldName);
            }
        } catch (Exception e) {
            GTCEu.LOGGER.error("Sync: Failed to deserialize field {}", field.fieldName);
            GTCEu.LOGGER.error(e);
        }
    }
}
