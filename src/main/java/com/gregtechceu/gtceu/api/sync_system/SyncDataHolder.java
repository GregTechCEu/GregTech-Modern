package com.gregtechceu.gtceu.api.sync_system;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformers;
import com.gregtechceu.gtceu.api.sync_system.managed.ISyncManaged;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.util.*;
import java.util.stream.Collectors;

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
        for (var field : syncData.getServerSaveFields().values()) {
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
        for (var field : syncData.getServerSaveFields().values()) {
            Tag newValue = tag.get(field.nbtSaveKey);
            if (newValue == null || newValue instanceof CompoundTag compound &&
                    (compound.isEmpty() || (compound.size() == 1 && compound.getBoolean("null"))))
                continue;

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

    @SuppressWarnings("unchecked")
    public void writeClientPacket(HolderLookup.Provider lookup, FriendlyByteBuf buf) {
        Set<FieldSyncData> fieldsToSerialize = syncData.getClientSyncFields().values().stream()
                .filter(this::shouldSyncFieldToClient).collect(Collectors.toSet());

        var fieldData = new FriendlyByteBuf(Unpooled.buffer());
        boolean hadErrorWritingData = false;

        for (var field : fieldsToSerialize) {
            Object currentValue = field.handle.get(holder);

            fieldData.writeUtf(field.fieldName);
            fieldData.writeBoolean(currentValue == null);
            if (currentValue == null) continue;

            if (!confirmTransformerPresent(field, holder)) continue;

            try {
                ((ValueTransformer<Object>) Objects.requireNonNull(field.transformer))
                        .writeToPacket(fieldData, currentValue,
                                new ValueTransformer.TransformerContext<>(holder, field.type, currentValue,
                                        field.fieldName,
                                        true, resyncAll, lookup));
            } catch (Exception e) {
                GTCEu.LOGGER.error("Sync: Failed to write client packet on field {}", field.fieldName, e);
                hadErrorWritingData = true;
                break;
            }
        }

        resyncAll = false;
        dirtySyncFields.clear();
        hasDirtyChildSyncObject = false;

        if (hadErrorWritingData) {
            buf.writeVarInt(0);
            return;
        }

        buf.writeVarInt(fieldsToSerialize.size());
        buf.writeBytes(fieldData);
    }

    @SuppressWarnings("unchecked")
    public void readClientPacket(HolderLookup.Provider lookup, FriendlyByteBuf buf) {
        int fieldsToRead = buf.readVarInt();

        for (int fieldIndex = 0; fieldIndex < fieldsToRead; fieldIndex++) {
            String fieldName = buf.readUtf();
            FieldSyncData field = syncData.getClientSyncFields().get(fieldName);

            if (field == null) {
                GTCEu.LOGGER.error("Sync: Failed to read client packet: Unknown field {}", fieldName);
                return;
            }

            Object currentValue = field.handle.get(holder);

            boolean isNull = buf.readBoolean();
            if (isNull) {
                trySetField(field, holder, null);
                executeClientsideUpdateCallbacks(field);
                continue;
            }

            if (!confirmTransformerPresent(field, holder)) continue;

            try {
                Object result = ((ValueTransformer<Object>) Objects.requireNonNull(field.transformer))
                        .readFromPacket(buf,
                                new ValueTransformer.TransformerContext<>(holder, field.type, currentValue,
                                        field.fieldName,
                                        true, resyncAll, lookup));

                if (result != currentValue) {
                    trySetField(field, holder, result);
                }
                executeClientsideUpdateCallbacks(field);
            } catch (Exception e) {
                GTCEu.LOGGER.error("Sync: Failed to read client packet on field {}", field.fieldName, e);
                return;
            }

        }
    }

    private boolean shouldSyncFieldToClient(FieldSyncData field) {
        return (resyncAll || dirtySyncFields.contains(field.fieldName) ||
                field.handle.get(holder) instanceof ISyncManaged syncManaged &&
                        syncManaged.getSyncDataHolder().needsSync());
    }

    private void executeClientsideUpdateCallbacks(FieldSyncData field) {
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
            return value.getSyncDataHolder().serializeNBT(context.lookup());
        }

        @Override
        public @Nullable ISyncManaged deserializeNBT(Tag tag, TransformerContext<ISyncManaged> context) {
            ISyncManaged syncManaged = context.currentValue();

            if (syncManaged == null) {
                GTCEu.LOGGER.error("Sync: ISyncManaged field was null, cannot instantiate {}",
                        context.fieldName());
                return null;
            }

            syncManaged.getSyncDataHolder().deserializeNBT(context.lookup(), (CompoundTag) tag);
            return syncManaged;
        }

        @Override
        public void writeToPacket(FriendlyByteBuf buf, ISyncManaged value, TransformerContext<ISyncManaged> context) {
            if (context.isClientFullSyncUpdate()) value.getSyncDataHolder().resyncAllFields();
            value.getSyncDataHolder().writeClientPacket(context.lookup(), buf);
        }

        @Override
        public @Nullable ISyncManaged readFromPacket(FriendlyByteBuf buf, TransformerContext<ISyncManaged> context) {
            ISyncManaged syncManaged = context.currentValue();
            var clazz = context.type().getClassValue();

            if (syncManaged == null && clazz != null && ISyncManaged.class.isAssignableFrom(clazz)) {
                var ctor = ClassSyncData.getClassData(clazz).getClientsideConstructor();
                if (ctor != null) syncManaged = (ISyncManaged) ctor.get();
            }

            if (syncManaged == null) {
                GTCEu.LOGGER.error("Sync: ISyncManaged field was null on client, cannot instantiate {}",
                        context.fieldName());
                return null;
            }

            syncManaged.getSyncDataHolder().readClientPacket(context.lookup(), buf);
            return syncManaged;
        }
    }
}
