package com.gregtechceu.gtceu.syncsystem;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.annotations.*;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformers;

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

        Map<String, MethodInfo> annotatedMethods = new HashMap<>();

        for (Method method : clazz.getDeclaredMethods()) {
            ClientFieldChangeListener listener = method.getAnnotation(ClientFieldChangeListener.class);
            FieldDataModifier modifier = method.getAnnotation(FieldDataModifier.class);
            if (listener == null && modifier == null) continue;
            if (listener != null && modifier != null) throw new IllegalArgumentException(
                    "Methods cannot be annotated with both @ClientFieldChangeListener and @FieldDataModifier: %s.%s"
                            .formatted(clazz.getCanonicalName(), method.getName()));
            if (Modifier.isStatic(method.getModifiers()))
                throw new IllegalArgumentException("Cannot apply syncdata annotation to static method: %s.%s"
                        .formatted(clazz.getCanonicalName(), method.getName()));

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
            annotatedMethods.putIfAbsent(fieldName,
                    new MethodInfo(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
            if (listener != null) annotatedMethods.get(fieldName).listeners.add(handle);
            else if (modifier.target() == FieldDataModifier.ModifyTarget.LOAD_NBT)
                annotatedMethods.get(fieldName).nbtLoaders.add(handle);
            else if (modifier.target() == FieldDataModifier.ModifyTarget.SAVE_NBT)
                annotatedMethods.get(fieldName).nbtSavers.add(handle);
        }

        for (Field field : clazz.getDeclaredFields()) {
            boolean hasSaveField = field.isAnnotationPresent(SaveField.class);
            boolean hasClientSync = field.isAnnotationPresent(SyncToClient.class);
            if (!hasSaveField && !hasClientSync) continue;
            if (Modifier.isStatic(field.getModifiers()))
                throw new IllegalArgumentException("Cannot apply syncdata annotations to static field: %s.%s"
                        .formatted(clazz.getCanonicalName(), field.getName()));

            VarHandle handle;
            try {
                handle = privateLookup.unreflectVarHandle(field);
            } catch (IllegalAccessException e) {
                GTCEu.LOGGER.error("Sync: Failed to acquire variable handle for field {} {}", field.getName(),
                        clazz.getCanonicalName());
                GTCEu.LOGGER.error(e.getMessage());
                continue;
            }

            FieldSyncData syncData = new FieldSyncData(field, handle, annotatedMethods.get(field.getName()));
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

    public static final class FieldSyncData {

        public final String fieldName, nbtSaveKey;
        public final VarHandle handle;
        public final boolean triggerClientRerender, isCustomData, isComplex;
        public final IValueTransformer<?> transformer;
        public final MethodHandle[] changeListenerHandles, nbtSaveModifiers, nbtLoadModifiers;

        public FieldSyncData(@NotNull Field field, @NotNull VarHandle handle, MethodInfo appliedMethods) {
            this.fieldName = field.getName();
            this.handle = handle;
            SaveField savedField = field.getAnnotation(SaveField.class);
            this.nbtSaveKey = (savedField != null && !savedField.nbtKey().isBlank()) ? savedField.nbtKey() : fieldName;
            triggerClientRerender = field.isAnnotationPresent(RerenderOnChanged.class);
            isCustomData = field.isAnnotationPresent(CustomDataField.class);
            isComplex = ISyncManaged.class.isAssignableFrom(field.getType());

            if (isCustomData && (appliedMethods.nbtSavers.size() != 1 || appliedMethods.nbtLoaders.size() != 1))
                throw new IllegalArgumentException(
                        "Fields marked with @CustomDataField must have exactly one SAVE_NBT FieldDataModifier and one LOAD_NBT FieldDataModifier: %s.%s"
                                .formatted(field.getClass().getCanonicalName(), fieldName));

            if (!isCustomData) {
                IValueTransformer<?> collectionTransformer = ValueTransformers.getCollectionTransformer(field);
                if (collectionTransformer == null) {
                    transformer = ValueTransformers.get(field.getType());
                } else {
                    transformer = collectionTransformer;
                }
            } else transformer = null;

            if (appliedMethods != null) {
                changeListenerHandles = appliedMethods.listeners.toArray(MethodHandle[]::new);
                nbtSaveModifiers = appliedMethods.nbtSavers.toArray(MethodHandle[]::new);
                nbtLoadModifiers = appliedMethods.nbtLoaders.toArray(MethodHandle[]::new);
            } else {
                changeListenerHandles = new MethodHandle[0];
                nbtSaveModifiers = new MethodHandle[0];
                nbtLoadModifiers = new MethodHandle[0];
            }
        }
    }

    public record MethodInfo(List<MethodHandle> listeners, List<MethodHandle> nbtLoaders,
                             List<MethodHandle> nbtSavers) {}
}
