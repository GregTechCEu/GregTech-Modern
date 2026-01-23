package com.gregtechceu.gtceu.syncsystem;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.annotations.*;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Static data for {@link ISyncManaged} classes.
 */
public final class ClassSyncData {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    public static final ClassValue<ClassSyncData> CACHE = new ClassValue<>() {

        @Override
        protected ClassSyncData computeValue(@NotNull Class<?> type) {
            return new ClassSyncData(type);
        }
    };

    public final Object2ObjectMap<String, FieldSyncData> clientSyncFields = new Object2ObjectOpenHashMap<>();
    public final Object2ObjectMap<String, FieldSyncData> serverSaveFields = new Object2ObjectOpenHashMap<>();

    private ClassSyncData(@NotNull Class<?> clazz) {
        MethodHandles.Lookup privateLookup;
        try {
            privateLookup = MethodHandles.privateLookupIn(clazz, LOOKUP);
        } catch (IllegalAccessException e) {
            GTCEu.LOGGER.error("Sync: Failed to create method handle lookup for class {}", clazz);
            GTCEu.LOGGER.error(e.getMessage());
            return;
        }

        Map<String, List<MethodHandle>> changeListeners = new HashMap<>();
        Map<String, MethodHandle> nbtSaveFuncs = new HashMap<>();
        Map<String, MethodHandle> nbtLoadFuncs = new HashMap<>();

        for (Method method : clazz.getDeclaredMethods()) {
            ClientFieldChangeListener listener = method.getAnnotation(ClientFieldChangeListener.class);
            FieldDataModifier modifier = method.getAnnotation(FieldDataModifier.class);
            if (listener == null && modifier == null) continue;

            validateMethodAnnotations(method, clazz);

            MethodHandle handle;
            try {
                handle = privateLookup.unreflect(method);
            } catch (IllegalAccessException e) {
                GTCEu.LOGGER.error("Sync: Failed to acquire method handle for method {} {}", method.getName(),
                        clazz.getCanonicalName());
                GTCEu.LOGGER.error(e.getMessage());
                continue;
            }

            String fieldName = listener != null ? listener.fieldName() : modifier.fieldName();

            if (listener != null) {
                changeListeners.computeIfAbsent(fieldName, $ -> new ArrayList<>()).add(handle);
            } else {
                if (modifier.target() == FieldDataModifier.ModifyTarget.SAVE_NBT) {
                    if (nbtSaveFuncs.containsKey(fieldName)) {
                        throw new IllegalArgumentException(
                                "Fields marked with @CustomDataField must have one SAVE_NBT FieldDataModifier and one LOAD_NBT FieldDataModifier: %s.%s"
                                        .formatted(clazz.getCanonicalName(), fieldName));
                    } else {
                        nbtSaveFuncs.put(fieldName, handle);
                    }
                } else {
                    if (nbtLoadFuncs.containsKey(fieldName)) {
                        throw new IllegalArgumentException(
                                "Fields marked with @CustomDataField must have one SAVE_NBT FieldDataModifier and one LOAD_NBT FieldDataModifier: %s.%s"
                                        .formatted(clazz.getCanonicalName(), fieldName));
                    } else {
                        nbtLoadFuncs.put(fieldName, handle);
                    }
                }
            }
        }

        for (Field field : clazz.getDeclaredFields()) {

            boolean hasSaveField = field.isAnnotationPresent(SaveField.class);
            boolean hasClientSync = field.isAnnotationPresent(SyncToClient.class);
            if (!hasSaveField && !hasClientSync) continue;

            validateFieldAnnotations(field, clazz);

            VarHandle handle;
            try {
                handle = privateLookup.unreflectVarHandle(field);
            } catch (IllegalAccessException e) {
                GTCEu.LOGGER.error("Sync: Failed to acquire variable handle for field {} {}", field.getName(),
                        clazz.getCanonicalName());
                GTCEu.LOGGER.error(e.getMessage());
                continue;
            }

            ValueTransformer<?> transformer;
            if (!nbtSaveFuncs.containsKey(field.getName()) && !nbtLoadFuncs.containsKey(field.getName())) {
                transformer = ValueTransformers.getForField(field);
            } else {
                var nbtSave = nbtSaveFuncs.get(field.getName());
                var nbtLoad = nbtLoadFuncs.get(field.getName());
                transformer = new ValueTransformer<>() {

                    @Override
                    public Tag serializeNBT(Object value, ValueTransformer.TransformerContext<Object> context) {
                        try {
                            return (Tag) nbtSave.invoke(context);
                        } catch (Throwable e) {
                            GTCEu.LOGGER.error("Sync: Error while invoking nbt save function for field {}",
                                    field.getName());
                            GTCEu.LOGGER.error(e.getMessage());
                            return new CompoundTag();
                        }
                    }

                    @Override
                    public Object deserializeNBT(Tag tag, ValueTransformer.TransformerContext<Object> context) {
                        try {
                            nbtLoad.invokeWithArguments(context, tag);
                            return context.currentValue();
                        } catch (Throwable e) {
                            GTCEu.LOGGER.error("Sync: Error while invoking nbt load function for field {}",
                                    field.getName());
                            GTCEu.LOGGER.error(e.getMessage());
                            return context.currentValue();
                        }
                    }
                };
            }

            FieldSyncData syncData = new FieldSyncData(field, handle, transformer,
                    changeListeners.getOrDefault(field.getName(), List.of()).toArray(MethodHandle[]::new));
            if (hasClientSync) clientSyncFields.put(field.getName(), syncData);
            if (hasSaveField) serverSaveFields.put(field.getName(), syncData);
        }

        Class<?> parent = clazz.getSuperclass();
        if (parent != Object.class) {
            ClassSyncData parentHandles = CACHE.get(parent);
            clientSyncFields.putAll(parentHandles.clientSyncFields);
            serverSaveFields.putAll(parentHandles.serverSaveFields);
        }
    }

    private static void validateMethodAnnotations(Method method, Class<?> clazz) {
        ClientFieldChangeListener listener = method.getAnnotation(ClientFieldChangeListener.class);
        FieldDataModifier modifier = method.getAnnotation(FieldDataModifier.class);
        if (listener != null && modifier != null) throw new IllegalArgumentException(
                "Methods cannot be annotated with both @ClientFieldChangeListener and @FieldDataModifier: %s.%s"
                        .formatted(clazz.getCanonicalName(), method.getName()));
        if (Modifier.isStatic(method.getModifiers()))
            throw new IllegalArgumentException("Cannot apply syncdata annotation to static method: %s.%s"
                    .formatted(clazz.getCanonicalName(), method.getName()));
    }

    private static void validateFieldAnnotations(Field field, Class<?> clazz) {
        if (Modifier.isStatic(field.getModifiers()))
            throw new IllegalArgumentException("Cannot apply syncdata annotations to static field: %s.%s"
                    .formatted(field.getDeclaringClass().getCanonicalName(), field.getName()));
    }

    public static final class FieldSyncData {

        public final String fieldName, nbtSaveKey;
        public final VarHandle handle;
        public final boolean triggerClientRerender, isSyncManaged;
        public final ValueTransformer<?> transformer;
        public final MethodHandle[] changeListenerHandles;

        public FieldSyncData(@NotNull Field field, VarHandle handle, ValueTransformer<?> transformer,
                             MethodHandle[] changeListenerHandles) {
            fieldName = field.getName();
            SaveField saveField = field.getAnnotation(SaveField.class);
            nbtSaveKey = (saveField != null && !saveField.nbtKey().isBlank()) ? saveField.nbtKey() : fieldName;
            this.isSyncManaged = ISyncManaged.class.isAssignableFrom(field.getType());
            this.handle = handle;
            this.triggerClientRerender = field.isAnnotationPresent(RerenderOnChanged.class);
            this.changeListenerHandles = changeListenerHandles;
            this.transformer = transformer;
        }
    }
}
