package com.gregtechceu.gtceu.api.sync_system;

import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
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
    public final TypeDeclaration type;

    public FieldSyncData(Field field, VarHandle handle, @Nullable ValueTransformer<?> transformer,
                         List<MethodHandle> changeListenerHandles) {
        fieldName = field.getName();
        SaveField saveField = field.getAnnotation(SaveField.class);
        this.nbtSaveKey = (saveField != null && !saveField.nbtKey().isBlank()) ? saveField.nbtKey() : fieldName;
        this.isSyncManaged = ISyncManaged.class.isAssignableFrom(field.getType());
        this.handle = handle;
        this.triggerClientRerender = field.isAnnotationPresent(RerenderOnChanged.class);
        this.changeListenerHandles = changeListenerHandles;
        this.transformer = transformer;
        this.type = new TypeDeclaration(field.getGenericType());
    }
}
