package com.gregtechceu.gtceu.api.sync_system;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldSyncHandler {

    @SuppressWarnings("unchecked")
    public static Tag serializeField(Object holder, FieldSyncData field,
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
    public static void deserializeField(Object holder, FieldSyncData field,
                                        @Nullable Tag savedValue,
                                        boolean readingClientFields) {
        if (savedValue == null || savedValue instanceof CompoundTag compound && compound.isEmpty()) return;

        if (savedValue instanceof CompoundTag compound && compound.getBoolean("null")) {
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

            Object result = transformer.deserializeNBT(savedValue, new ValueTransformer.TransformerContext<>(
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
}
