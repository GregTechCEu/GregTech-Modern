package com.gregtechceu.gtceu.api.sync_system;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformers;
import com.gregtechceu.gtceu.api.sync_system.managed.ISyncManaged;

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
        return serializeNBT(writeClientFields, resyncAll);
    }

    public CompoundTag serializeNBT(boolean writeClientFields, boolean fullSync) {
        Set<FieldSyncData> fieldsToSerialize = writeClientFields ? syncData.getClientSyncFields() :
                syncData.getServerSaveFields();

        CompoundTag tag = new CompoundTag();
        for (var field : fieldsToSerialize) {
            if (shouldSerializeField(field, writeClientFields, fullSync)) {
                Tag nbtValue = serializeField(holder, field, writeClientFields, fullSync);
                tag.put(field.nbtSaveKey, nbtValue);
            }
        }
        resyncAll = false;
        dirtySyncFields.clear();
        return tag;
    }

    private boolean shouldSerializeField(FieldSyncData field, boolean writeClient, boolean fullSync) {
        return !writeClient || fullSync || dirtySyncFields.contains(field.fieldName) ||
                (field.type.getClassValue() != null && ISyncManaged.class.isAssignableFrom(field.type.getClassValue()));
    }

    public void deserializeNBT(CompoundTag tag, boolean readingClientFields) {
        Set<FieldSyncData> fieldsToCheck = readingClientFields ? syncData.getClientSyncFields() :
                syncData.getServerSaveFields();

        for (var field : fieldsToCheck) {

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
                    GTCEu.LOGGER.error("Sync: Error while invoking change listener for field {}", field.fieldName, e);
                }

                if (field.triggerClientRerender) holder.scheduleRenderUpdate();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Tag serializeField(Object holder, FieldSyncData field,
                                      boolean writeClientFields, boolean fullSync) {
        Object currentValue = field.handle.get(holder);

        if (currentValue == null) {
            var nullCompound = new CompoundTag();
            nullCompound.putBoolean("null", true);
            return nullCompound;
        }

        if (field.transformer == null) {
            field.setTransformer(ValueTransformers.get(field.type.getRawType()));
            if (field.transformer == null) {
                GTCEu.LOGGER.error("Sync: Failed to serialize field {} in class {}: Missing value transformer for {}",
                        field.fieldName, holder.getClass().getName(), field.type);
                return new CompoundTag();
            }
        }

        try {
            return ((ValueTransformer<Object>) field.transformer).serializeNBT(currentValue,
                    new ValueTransformer.TransformerContext<>(holder, field.type, currentValue, field.fieldName,
                            writeClientFields, fullSync));

        } catch (Exception e) {
            GTCEu.LOGGER.error("Sync: Failed to serialize field {}", field.fieldName, e);
        }

        return new CompoundTag();
    }

    @SuppressWarnings("unchecked")
    private static void deserializeField(Object holder, FieldSyncData field,
                                         @Nullable Tag newValue,
                                         boolean readingClientFields) {
        if (newValue == null || newValue instanceof CompoundTag compound && compound.isEmpty()) return;

        if (newValue instanceof CompoundTag compound && compound.getBoolean("null")) {
            field.handle.set(holder, null);
            return;
        }

        if (field.transformer == null) {
            field.setTransformer(ValueTransformers.get(field.type.getRawType()));
            if (field.transformer == null) {
                GTCEu.LOGGER.error("Sync: Failed to deserialize field {} in class {}: Missing value transformer for {}",
                        field.fieldName, holder.getClass().getName(), field.type);
                return;
            }
        }

        try {
            ValueTransformer<Object> transformer = (ValueTransformer<Object>) field.transformer;
            var current = field.handle.get(holder);

            Object result = transformer.deserializeNBT(newValue, new ValueTransformer.TransformerContext<>(
                    holder, field.type, current, field.fieldName, readingClientFields, false));

            if (result != current) {
                field.handle.set(holder, result);
            }

        } catch (Exception e) {
            if (e instanceof UnsupportedOperationException) {
                GTCEu.LOGGER.error(
                        "Sync: failed to perform VarHandle set: unsupported op on {} (you are probably trying to sync a final field)",
                        field.fieldName);
                return;
            }
            GTCEu.LOGGER.error("Sync: Failed to deserialize field {}", field.fieldName, e);
        }
    }

    public static class SyncManagedTransformer implements ValueTransformer<ISyncManaged> {

        @Override
        public Tag serializeNBT(ISyncManaged value, TransformerContext<ISyncManaged> context) {
            return value.getSyncDataHolder().serializeNBT(context.isClientSync(), context.isClientFullSyncUpdate());
        }

        @Override
        public @Nullable ISyncManaged deserializeNBT(Tag tag, TransformerContext<ISyncManaged> context) {
            ISyncManaged syncManaged = context.currentValue();
            var clazz = context.type().getClassValue();

            if (syncManaged == null && clazz != null && ISyncManaged.class.isAssignableFrom(clazz)) {
                var ctor = ClassSyncData.getClassData(clazz).getClientsideConstructor();
                if (ctor != null) syncManaged = (ISyncManaged) ctor.get();
            }

            if (syncManaged == null) {
                GTCEu.LOGGER.error("Sync: ISyncManaged field was null, cannot instantiate {}",
                        context.fieldName());
                return null;
            }

            syncManaged.getSyncDataHolder().deserializeNBT((CompoundTag) tag, context.isClientSync());

            return syncManaged;
        }
    }
}
