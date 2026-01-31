package com.gregtechceu.gtceu.api.sync_system;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformers;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.*;
import java.util.*;

/**
 * Static data for {@link ISyncManaged} classes.
 */
public final class ClassSyncData {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final ClassValue<ClassSyncData> CACHE = new ClassValue<>() {

        @Override
        protected ClassSyncData computeValue(Class<?> type) {
            return new ClassSyncData(type);
        }
    };

    /**
     * Gets the {@link ClassSyncData} object for a specific class
     */
    public static ClassSyncData getClassData(Class<?> cls) {
        return CACHE.get(cls);
    }

    @Getter
    private final List<FieldSyncData> managedFields = new ObjectArrayList<>();
    @Getter
    private final Set<FieldSyncData> clientSyncFields = new ObjectOpenHashSet<>();
    @Getter
    private final Set<FieldSyncData> serverSaveFields = new ObjectOpenHashSet<>();

    private ClassSyncData(Class<?> clazz) {
        MethodHandles.Lookup privateLookup;
        try {
            privateLookup = MethodHandles.privateLookupIn(clazz, LOOKUP);
        } catch (IllegalAccessException e) {
            GTCEu.LOGGER.error("Sync: Failed to create method handle lookup for class {}", clazz);
            GTCEu.LOGGER.error(e.getMessage());
            return;
        }

        Map<String, List<MethodHandle>> changeListeners = new HashMap<>();

        for (Method method : clazz.getDeclaredMethods()) {
            ClientFieldChangeListener listener = method.getAnnotation(ClientFieldChangeListener.class);
            if (listener == null) continue;

            if (Modifier.isStatic(method.getModifiers()))
                throw new IllegalArgumentException("Cannot apply syncdata annotation to static method: %s.%s"
                        .formatted(clazz.getName(), method.getName()));

            MethodHandle handle;
            try {
                handle = privateLookup.unreflect(method);
            } catch (IllegalAccessException e) {
                GTCEu.LOGGER.error("Sync: Failed to acquire method handle for method {} {}", method.getName(),
                        clazz.getName());
                GTCEu.LOGGER.error(e.getMessage());
                continue;
            }

            changeListeners.computeIfAbsent(listener.fieldName(), $ -> new ArrayList<>()).add(handle);
        }

        for (Field field : clazz.getDeclaredFields()) {

            boolean hasSaveField = field.isAnnotationPresent(SaveField.class);
            boolean hasClientSync = field.isAnnotationPresent(SyncToClient.class);
            if (!hasSaveField && !hasClientSync) continue;

            if (Modifier.isStatic(field.getModifiers()))
                throw new IllegalArgumentException("Cannot apply syncdata annotations to static field: %s.%s"
                        .formatted(field.getDeclaringClass().getName(), field.getName()));

            VarHandle handle;
            try {
                handle = privateLookup.unreflectVarHandle(field);
            } catch (IllegalAccessException e) {
                GTCEu.LOGGER.error("Sync: Failed to acquire variable handle for field {} {}", field.getName(),
                        clazz.getName());
                GTCEu.LOGGER.error(e.getMessage());
                continue;
            }

            FieldSyncData syncData = new FieldSyncData(field, handle, ValueTransformers.get(field.getGenericType()),
                    changeListeners.getOrDefault(field.getName(), List.of()));
            managedFields.add(syncData);
            if (hasClientSync) clientSyncFields.add(syncData);
            if (hasSaveField) serverSaveFields.add(syncData);
        }

        Class<?> parent = clazz.getSuperclass();
        if (parent != Object.class) {
            ClassSyncData parentHandles = CACHE.get(parent);
            managedFields.addAll(parentHandles.managedFields);
            clientSyncFields.addAll(parentHandles.clientSyncFields);
            serverSaveFields.addAll(parentHandles.serverSaveFields);
        }
    }

    /**
     * Allows for a custom value transformer to be used for a specific field on this class, ignoring any other sync
     * behaviour attached to the field.
     * 
     * @param fieldName   The field name
     * @param transformer The custom value transformer
     */
    public void setCustomTransformerForField(String fieldName, ValueTransformer<?> transformer) {
        managedFields.stream().filter(f -> Objects.equals(f.fieldName, fieldName))
                .findFirst()
                .ifPresent(fieldData -> fieldData.setTransformer(transformer));
    }
}
