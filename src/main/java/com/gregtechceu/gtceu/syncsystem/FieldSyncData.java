package com.gregtechceu.gtceu.syncsystem;

import com.gregtechceu.gtceu.syncsystem.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Information about the sync behaviour of fields with sync annotations in ISyncManaged classes
 */
public final class FieldSyncData {

    public final String fieldName, nbtSaveKey;
    public final VarHandle handle;
    public final boolean triggerClientRerender, isSyncManaged;
    @Setter
    public @Nullable ValueTransformer<?> transformer;
    public final List<MethodHandle> changeListenerHandles;
    public final Class<?> clazz;
    public final Type[] genericArgs;

    public FieldSyncData(Field field, VarHandle handle, @Nullable ValueTransformer<?> transformer,
                         List<MethodHandle> changeListenerHandles) {
        fieldName = field.getName();
        SaveField saveField = field.getAnnotation(SaveField.class);
        nbtSaveKey = (saveField != null) ? saveField.nbtKey() : fieldName;
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
