package com.gregtechceu.gtceu.api.syncdata;

import com.gregtechceu.gtceu.GTCEu;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.managed.IRef;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author KilaBash
 * @date 2023/6/23
 * @implNote EnhancedFieldManagedStorage
 */
public class EnhancedFieldManagedStorage extends FieldManagedStorage {
    final IEnhancedManaged enhancedManaged;

    public EnhancedFieldManagedStorage(IEnhancedManaged enhancedManaged) {
        super(enhancedManaged);
        this.enhancedManaged = enhancedManaged;
    }

    public void initEnhancedFeature() {
        for (IRef syncField : getSyncFields()) {
            var rawField = syncField.getKey().getRawField();
            if (rawField.isAnnotationPresent(RequireRerender.class)) {
                addSyncUpdateListener(syncField.getKey(),  enhancedManaged::scheduleRender);
            }
            if (rawField.isAnnotationPresent(UpdateListener.class)) {
                var methodName = rawField.getAnnotation(UpdateListener.class).methodName();
                Method method = null;
                try {
                    method = enhancedManaged.getClass().getDeclaredMethod(methodName, rawField.getType(), rawField.getType());
                    method.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    GTCEu.LOGGER.error("couldn't find the listener method {} for synced field {}", methodName, rawField.getName());
                }
                if (method != null) {
                    final Method finalMethod = method;
                    addSyncUpdateListener(syncField.getKey(), (name, newValue, oldValue) -> {
                        try {
                            finalMethod.invoke(enhancedManaged, newValue, oldValue);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }
    }
}
