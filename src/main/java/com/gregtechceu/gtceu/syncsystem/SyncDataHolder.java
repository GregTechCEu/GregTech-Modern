package com.gregtechceu.gtceu.syncsystem;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;

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

    public SyncDataHolder(@NotNull ISyncManaged o) {
        holder = o;
        syncData = ClassSyncData.CACHE.get(o.getClass());
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
        Map<String, ClassSyncData.FieldSyncData> fieldsToSerialize;
        if (!writeClientFields) {
            fieldsToSerialize = syncData.serverSaveFields;
        } else {
            fieldsToSerialize = syncData.clientSyncFields;
        }

        CompoundTag tag = new CompoundTag();
        for (var fieldEntry : fieldsToSerialize.entrySet()) {

            if (writeClientFields &&
                    (!fullSync && !dirtySyncFields.contains(fieldEntry.getKey()) && !fieldEntry.getValue().isComplex))
                continue;
            var field = fieldEntry.getValue();
            if (field.isCustomData) {
                try {
                    Object result = field.nbtSaveModifiers[0].invoke(holder, new CompoundTag(), writeClientFields);
                    tag.put(field.nbtSaveKey, (CompoundTag) result);
                } catch (Throwable e) {
                    GTCEu.LOGGER.error("Sync: Error while invoking nbtSaveModifier for field {}", field.fieldName);
                    GTCEu.LOGGER.error(e.getMessage());
                    return new CompoundTag();
                }
                continue;
            }

            Tag nbtValue = serialiseField(holder, field, writeClientFields);

            for (MethodHandle modifier : field.nbtSaveModifiers) {
                try {
                    nbtValue = (Tag) modifier.invoke(holder, nbtValue, writeClientFields);
                } catch (Throwable e) {
                    GTCEu.LOGGER.error("Sync: Error while invoking nbtSaveModifier for field {}", field.fieldName);
                    GTCEu.LOGGER.error(e.getMessage());
                    return new CompoundTag();
                }
            }

            tag.put(field.nbtSaveKey, nbtValue);
        }
        return tag;
    }

    public void deserializeNBT(CompoundTag tag, boolean readingClientFields) {
        Map<String, ClassSyncData.FieldSyncData> fieldsToCheck = readingClientFields ? syncData.clientSyncFields :
                syncData.serverSaveFields;

        for (var fieldEntry : fieldsToCheck.entrySet()) {

            var field = fieldEntry.getValue();

            if (field.isCustomData) {
                try {
                    field.nbtLoadModifiers[0].invoke(holder, tag, readingClientFields);
                } catch (Throwable e) {
                    GTCEu.LOGGER.error("Sync: Error while invoking nbtLoadModifier for field {}", field.fieldName);
                    GTCEu.LOGGER.error(e.getMessage());
                    return;
                }
                continue;
            }

            Tag savedValue = tag.get(field.nbtSaveKey);
            deserialiseField(holder, field, savedValue, readingClientFields);

            for (MethodHandle modifier : field.nbtLoadModifiers) {
                try {
                    modifier.invoke(holder, savedValue, readingClientFields);
                } catch (Throwable e) {
                    GTCEu.LOGGER.error("Sync: Error while invoking nbtLoadModifier for field {}", field.fieldName);
                    GTCEu.LOGGER.error(e.getMessage());
                    return;
                }
            }

            if (readingClientFields) {
                try {
                    for (MethodHandle changeListenerHandle : field.changeListenerHandles) {
                        changeListenerHandle.invoke(holder);
                    }
                } catch (Throwable e) {
                    if (e instanceof WrongMethodTypeException) {
                        throw new IllegalArgumentException(
                                "Invalid method signature for change listener for field %s %s"
                                        .formatted(field.fieldName, holder.getClass().getCanonicalName()));
                    }
                    GTCEu.LOGGER.error("Sync: Error while invoking change listener for field {}", field.fieldName);
                    GTCEu.LOGGER.error(e);
                }

                if (field.triggerClientRerender) holder.scheduleRenderUpdate();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Tag serialiseField(ISyncManaged holder, ClassSyncData.FieldSyncData field,
                                      boolean writeClientFields) {
        Object currentValue = field.handle.get(holder);

        if (!field.isComplex && currentValue == null) {
            var nullCompound = new CompoundTag();
            nullCompound.putBoolean("null", true);
            return nullCompound;
        }

        try {

            if (field.transformer != null) {
                if (writeClientFields) {
                    return ((IValueTransformer<Object>) field.transformer).serializeClientSyncNBT(currentValue, holder);
                } else {
                    return ((IValueTransformer<Object>) field.transformer).serializeNBT(currentValue, holder);
                }
            } else if (field.isComplex && currentValue instanceof ISyncManaged syncObj) {
                return syncObj.getSyncDataHolder().serializeNBT(writeClientFields);
            }

        } catch (Exception e) {
            GTCEu.LOGGER.error("Sync: Failed to serialise field {}", field.fieldName);
            GTCEu.LOGGER.error(e);
        }

        return new CompoundTag();
    }

    @SuppressWarnings("unchecked")
    private static void deserialiseField(ISyncManaged holder, ClassSyncData.FieldSyncData field, Tag savedValue,
                                         boolean readingClientFields) {
        Object currentVal = field.handle.get(holder);

        if (savedValue == null || savedValue instanceof CompoundTag compound && compound.isEmpty()) return;

        if (savedValue instanceof CompoundTag compound && compound.getBoolean("null")) {
            field.handle.set(holder, null);
            return;
        }

        try {
            if (field.transformer != null) {
                IValueTransformer<Object> transformer = (IValueTransformer<Object>) field.transformer;
                if (transformer.mustProvideObject()) {
                    if (readingClientFields) {
                        transformer.deserializeClientNBT(savedValue, holder, field.handle.get(holder));
                    } else {
                        transformer.deserializeNBT(savedValue, holder, field.handle.get(holder));
                    }
                } else {
                    try {
                        if (readingClientFields) {
                            field.handle.set(holder, transformer.deserializeClientNBT(savedValue, holder, null));
                        } else {
                            field.handle.set(holder, transformer.deserializeNBT(savedValue, holder, null));
                        }
                    } catch (UnsupportedOperationException e) {
                        GTCEu.LOGGER.error("Sync: failed to perform VarHandle set: unsupported op {} {}",
                                field.fieldName, field.handle.toString());
                    }
                }
            } else if (field.isComplex && savedValue instanceof CompoundTag compound) {
                if (currentVal == null) {
                    GTCEu.LOGGER.error("Sync: ISyncManaged field was null, cannot instantiate {}",
                            field.fieldName);
                    return;
                }
                if (currentVal instanceof ISyncManaged syncObj)
                    syncObj.getSyncDataHolder().deserializeNBT(compound, readingClientFields);
            }
        } catch (Exception e) {
            GTCEu.LOGGER.error("Sync: Failed to deserialise field {}", field.fieldName);
            GTCEu.LOGGER.error(e);
        }
    }
}
