package com.gregtechceu.gtceu.api.sync_system;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformers;
import com.gregtechceu.gtceu.api.sync_system.managed.ISyncManaged;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Setter;
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
    @Setter
    private boolean hasDirtyChildSyncObject = false;
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

    public boolean needsSync() {
        return !dirtySyncFields.isEmpty() || resyncAll || hasDirtyChildSyncObject;
    }

    @SuppressWarnings("unchecked")
    public CompoundTag serializeNBT(HolderLookup.Provider lookup) {
        CompoundTag tag = new CompoundTag();
        for (var field : syncData.getServerSaveFields()) {
            Object currentValue = field.handle.get(holder);
            if (currentValue == null || !confirmTransformerPresent(field, holder)) continue;

            try {
                Tag nbtValue = ((ValueTransformer<Object>) Objects.requireNonNull(field.transformer))
                        .serializeNBT(currentValue,
                                new ValueTransformer.TransformerContext<>(holder, field.type, currentValue,
                                        field.fieldName,
                                        false, false, lookup));
                tag.put(field.nbtSaveKey, nbtValue);

            } catch (Exception e) {
                GTCEu.LOGGER.error("Sync: Failed to serialize field {}", field.fieldName, e);
            }
        }
        return tag;
    }

    @SuppressWarnings("unchecked")
    public void deserializeNBT(HolderLookup.Provider lookup, CompoundTag tag) {
        for (var field : syncData.getServerSaveFields()) {
            Tag newValue = tag.get(field.nbtSaveKey);
            if (newValue == null || newValue instanceof CompoundTag compound && compound.isEmpty()) continue;

            if (!confirmTransformerPresent(field, holder)) continue;

            try {
                ValueTransformer<Object> transformer = (ValueTransformer<Object>) field.transformer;
                var current = field.handle.get(holder);

                Object result = Objects.requireNonNull(transformer).deserializeNBT(newValue,
                        new ValueTransformer.TransformerContext<>(
                                holder, field.type, current, field.fieldName, false, false, lookup));

                if (result != current) trySetField(field, holder, result);

            } catch (Exception e) {
                GTCEu.LOGGER.error("Sync: Failed to deserialize field {}", field.fieldName, e);
            }
        }
    }

    public CompoundTag serializeClientData(HolderLookup.Provider lookup, boolean fullSync) {
        if (fullSync) resyncAllFields();
        return serializeClientData(lookup);
    }

    @SuppressWarnings("unchecked")
    public CompoundTag serializeClientData(HolderLookup.Provider lookup) {
        Set<FieldSyncData> fieldsToSerialize = syncData.getClientSyncFields();

        CompoundTag tag = new CompoundTag();
        for (var field : fieldsToSerialize) {
            if (shouldSerializeClientField(field, resyncAll)) {

                Object currentValue = field.handle.get(holder);

                if (!resyncAll && currentValue instanceof ISyncManaged syncManaged &&
                        !syncManaged.getSyncDataHolder().needsSync()) {
                    continue;
                }

                if (!confirmTransformerPresent(field, holder)) continue;

                if (currentValue == null) {
                    var nullCompound = new CompoundTag();
                    nullCompound.putBoolean("null", true);
                    tag.put(field.nbtSaveKey, nullCompound);
                    continue;
                }

                try {
                    Tag nbtValue = ((ValueTransformer<Object>) Objects.requireNonNull(field.transformer))
                            .serializeNBT(currentValue,
                                    new ValueTransformer.TransformerContext<>(holder, field.type, currentValue,
                                            field.fieldName,
                                            true, resyncAll, lookup));
                    tag.put(field.nbtSaveKey, nbtValue);
                } catch (Exception e) {
                    GTCEu.LOGGER.error("Sync: Failed to serialize field {}", field.fieldName, e);
                }
            }
        }

        resyncAll = false;
        hasDirtyChildSyncObject = false;
        dirtySyncFields.clear();
        return tag;
    }

    private boolean shouldSerializeClientField(FieldSyncData field, boolean fullSync) {
        return fullSync || dirtySyncFields.contains(field.fieldName) ||
                (field.type.getClassValue() != null && ISyncManaged.class.isAssignableFrom(field.type.getClassValue()));
    }

    @SuppressWarnings("unchecked")
    public void deserializeClientData(HolderLookup.Provider lookup, CompoundTag tag) {
        for (var field : syncData.getClientSyncFields()) {

            Tag newValue = tag.get(field.nbtSaveKey);

            if (newValue == null || newValue instanceof CompoundTag compound && compound.isEmpty()) continue;

            if (newValue instanceof CompoundTag compound && compound.getBoolean("null")) {
                field.handle.set(holder, null);
                continue;
            }

            if (!confirmTransformerPresent(field, holder)) continue;

            try {
                ValueTransformer<Object> transformer = (ValueTransformer<Object>) field.transformer;
                var current = field.handle.get(holder);

                Object result = Objects.requireNonNull(transformer).deserializeNBT(newValue,
                        new ValueTransformer.TransformerContext<>(
                                holder, field.type, current, field.fieldName, true, false, lookup));

                if (result != current) trySetField(field, holder, result);

            } catch (Exception e) {
                GTCEu.LOGGER.error("Sync: Failed to deserialize field {}", field.fieldName, e);
            }

            executeClientSyncCallbacks(field);
        }
    }

    private void executeClientSyncCallbacks(FieldSyncData field) {
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

    private static boolean confirmTransformerPresent(FieldSyncData field, Object holder) {
        if (field.transformer == null) {
            field.setTransformer(ValueTransformers.get(field.type.getRawType()));
            if (field.transformer == null) {
                GTCEu.LOGGER.error(
                        "Sync: Failed to serialize/deserialize field {} in class {}: Missing value transformer for {}",
                        field.fieldName, holder.getClass().getName(), field.type);
                return false;
            }
        }
        return true;
    }

    private static void trySetField(FieldSyncData field, Object holder, @Nullable Object value) {
        try {
            field.handle.set(holder, value);
        } catch (Exception e) {
            if (e instanceof UnsupportedOperationException) {
                GTCEu.LOGGER.error(
                        "Sync: failed to perform VarHandle set: unsupported op on {} (you are probably trying to sync a final field)",
                        field.fieldName);
                return;
            }
            GTCEu.LOGGER.error("Sync: Failed to perform VarHandle set on {} : {}", field.fieldName, e);
        }
    }

    public static class SyncManagedTransformer implements ValueTransformer<ISyncManaged> {

        @Override
        public Tag serializeNBT(ISyncManaged value, TransformerContext<ISyncManaged> context) {
            if (context.isClientSync()) return value.getSyncDataHolder().serializeClientData(context.lookup(),
                    context.isClientFullSyncUpdate());
            else return value.getSyncDataHolder().serializeNBT(context.lookup());
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

            if (context.isClientSync()) {
                syncManaged.getSyncDataHolder().deserializeClientData(context.lookup(), (CompoundTag) tag);
            } else {
                syncManaged.getSyncDataHolder().deserializeNBT(context.lookup(), (CompoundTag) tag);
            }

            return syncManaged;
        }
    }
}
