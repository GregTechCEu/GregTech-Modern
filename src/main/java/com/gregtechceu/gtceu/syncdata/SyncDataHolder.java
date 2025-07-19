package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Class that holds all sync info for an {@link ISyncManaged} object.
 */
public class SyncDataHolder {

    private final ClassSyncData syncData;
    private final ISyncManaged holder;

    private final List<ClassSyncData.FieldSyncData> dirtySyncFields = new ArrayList<>();

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
        var fieldData = Arrays.stream(syncData.clientSyncFields).filter(f -> Objects.equals(f.fieldName, fieldName))
                .findFirst();
        fieldData.ifPresent(dirtySyncFields::add);
        holder.markAsChanged();
    }

    public CompoundTag getClientSyncNBT(boolean fullClientSync) {
        return serialize(true, fullClientSync);
    }

    public void loadClientSyncNBT(CompoundTag tag) {
        deserialize(tag, true);
    }

    public CompoundTag saveNBT() {
        return serialize(false, false);
    }

    public void loadFromNBT(CompoundTag tag) {
        deserialize(tag, false);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private CompoundTag serialize(boolean isSync, boolean fullSync) {
        CompoundTag tag = new CompoundTag();

        ClassSyncData.FieldSyncData[] fieldsToSerialize;

        if (isSync && fullSync) {
            fieldsToSerialize = syncData.clientSyncFields;
        } else if (isSync) {
            fieldsToSerialize = dirtySyncFields.toArray(ClassSyncData.FieldSyncData[]::new);
            dirtySyncFields.clear();
        } else {
            fieldsToSerialize = syncData.serverSaveFields;
        }

        for (ClassSyncData.FieldSyncData field : fieldsToSerialize) {

            if (field.isCustomData) {
                var result = field.nbtSaveModifiers[0].invoke(holder, new CompoundTag());
                tag.put(field.nbtSaveKey, (Tag) result);
                continue;
            }

            Tag nbtValue;
            if (field.isComplex) {
                ISyncManaged currentValue = (ISyncManaged) field.handle.get(holder);
                if (currentValue != null) nbtValue = currentValue.getSyncDataHolder().serialize(isSync, fullSync);
                else nbtValue = new CompoundTag();
            } else {
                if (field.transformer == null) {
                    GTCEu.LOGGER.error("missing value transformer for field {}", field.fieldName);
                    return new CompoundTag();
                }
                IValueTransformer<Object> transformer = (IValueTransformer<Object>) field.transformer;
                Object result = field.handle.get(holder);
                if (result == null) continue;
                nbtValue = transformer.serializeNBT(result, isSync, fullSync);
            }

            for (MethodHandle modifier : field.nbtSaveModifiers) {
                nbtValue = (Tag) modifier.invoke(holder, nbtValue);
            }
            tag.put(field.nbtSaveKey, nbtValue);
        }
        return tag;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private void deserialize(CompoundTag tag, boolean isSync) {
        ClassSyncData.FieldSyncData[] fieldsToCheck = (isSync) ? syncData.clientSyncFields : syncData.serverSaveFields;
        for (ClassSyncData.FieldSyncData field : fieldsToCheck) {
            if (field.isCustomData) {
                field.nbtLoadModifiers[0].invoke(holder, tag);
                continue;
            }

            Tag savedValue = tag.get(field.nbtSaveKey);
            if (savedValue == null || savedValue instanceof CompoundTag compound && compound.isEmpty()) continue;

            if (field.isComplex && savedValue instanceof CompoundTag compound) {
                ISyncManaged currentVal = (ISyncManaged) field.handle.get(holder);
                currentVal.getSyncDataHolder().deserialize(compound, isSync);
            } else {
                if (field.transformer == null) {
                    GTCEu.LOGGER.error("no value transformer for field {}", field.fieldName);
                    return;
                }
                IValueTransformer<Object> transformer = (IValueTransformer<Object>) field.transformer;

                if (transformer.mustProvideObject()) {
                    transformer.deserializeNBT(savedValue, field.handle.get(holder), isSync);
                } else {
                    field.handle.set(holder, transformer.deserializeNBT(savedValue, null, isSync));
                }

            }
            if (isSync) {
                for (MethodHandle listener : field.changeListenerHandles) {
                    listener.invoke(holder);
                }
                if (field.triggerClientRerender) holder.scheduleRenderUpdate();
            }
            for (MethodHandle modifier : field.nbtLoadModifiers) {
                modifier.invoke(holder, savedValue);
            }

        }
        if (!isSync) holder.onSaveDataLoaded();
    }
}
