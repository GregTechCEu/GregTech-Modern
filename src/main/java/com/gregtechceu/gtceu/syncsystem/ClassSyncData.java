package com.gregtechceu.gtceu.syncsystem;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.annotations.*;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformers;

import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

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
    private final Object2ReferenceArrayMap<String, FieldSyncData> clientSyncFields = new Object2ReferenceArrayMap<>();
    @Getter
    private final Object2ReferenceArrayMap<String, FieldSyncData> serverSaveFields = new Object2ReferenceArrayMap<>();

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

            changeListeners.computeIfAbsent(listener.fieldName(), $ -> new ArrayList<>()).add(handle);
        }

        for (Field field : clazz.getDeclaredFields()) {

            boolean hasSaveField = field.isAnnotationPresent(SaveField.class);
            boolean hasClientSync = field.isAnnotationPresent(SyncToClient.class);
            if (!hasSaveField && !hasClientSync) continue;

            if (Modifier.isStatic(field.getModifiers()))
                throw new IllegalArgumentException("Cannot apply syncdata annotations to static field: %s.%s"
                        .formatted(field.getDeclaringClass().getCanonicalName(), field.getName()));

            VarHandle handle;
            try {
                handle = privateLookup.unreflectVarHandle(field);
            } catch (IllegalAccessException e) {
                GTCEu.LOGGER.error("Sync: Failed to acquire variable handle for field {} {}", field.getName(),
                        clazz.getCanonicalName());
                GTCEu.LOGGER.error(e.getMessage());
                continue;
            }

            FieldSyncData syncData = new FieldSyncData(field, handle, ValueTransformers.get(field.getGenericType()),
                    changeListeners.getOrDefault(field.getName(), List.of()).toArray(MethodHandle[]::new));
            managedFields.add(syncData);
            if (hasClientSync) clientSyncFields.put(field.getName(), syncData);
            if (hasSaveField) serverSaveFields.put(field.getName(), syncData);
        }

        Class<?> parent = clazz.getSuperclass();
        if (parent != Object.class) {
            ClassSyncData parentHandles = CACHE.get(parent);
            managedFields.addAll(parentHandles.managedFields);
            clientSyncFields.putAll(parentHandles.clientSyncFields);
            serverSaveFields.putAll(parentHandles.serverSaveFields);
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
        Optional<FieldSyncData> fieldData = managedFields.stream().filter(f -> Objects.equals(f.fieldName, fieldName))
                .findFirst();
        if (fieldData.isEmpty()) return;
        fieldData.get().setTransformer(transformer);
    }

    /**
     * Information about the sync behaviour of fields with sync annotations in ISyncManaged classes
     */
    public static final class FieldSyncData {

        public final String fieldName, nbtSaveKey;
        public final VarHandle handle;
        public final boolean triggerClientRerender, isSyncManaged;
        @Setter
        @Nullable
        public ValueTransformer<?> transformer;
        public final MethodHandle[] changeListenerHandles;
        public final Class<?> clazz;
        public final Type[] genericArgs;

        public FieldSyncData(Field field, VarHandle handle, @Nullable ValueTransformer<?> transformer,
                             MethodHandle[] changeListenerHandles) {
            fieldName = field.getName();
            SaveField saveField = field.getAnnotation(SaveField.class);
            nbtSaveKey = (saveField != null && !saveField.nbtKey().isBlank()) ? saveField.nbtKey() : fieldName;
            this.isSyncManaged = ISyncManaged.class.isAssignableFrom(field.getType());
            this.handle = handle;
            this.triggerClientRerender = field.isAnnotationPresent(RerenderOnChanged.class);
            this.changeListenerHandles = changeListenerHandles;
            this.transformer = transformer;

            var type = field.getGenericType();
            if (type instanceof ParameterizedType parameterizedType) {
                clazz = (Class<?>) parameterizedType.getRawType();
                genericArgs = parameterizedType.getActualTypeArguments();
            } else {
                clazz = (Class<?>) type;
                genericArgs = new Type[0];
            }
        }
    }
}
