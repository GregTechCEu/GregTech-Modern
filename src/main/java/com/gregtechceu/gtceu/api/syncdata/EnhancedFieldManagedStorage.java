package com.gregtechceu.gtceu.api.syncdata;

import com.gregtechceu.gtceu.GTCEu;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.managed.IRef;
import net.minecraft.Util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/6/23
 * @implNote EnhancedFieldManagedStorage
 */
public class EnhancedFieldManagedStorage extends FieldManagedStorage {
    private final IEnhancedManaged enhancedManaged;
    private boolean initialized = false;

    public EnhancedFieldManagedStorage(IEnhancedManaged enhancedManaged) {
        super(enhancedManaged);
        this.enhancedManaged = enhancedManaged;
    }

    @Override
    public void init() {
        super.init();
        if (initialized) return;
        initialized = true;
        if (LDLib.isClient()) {
            initEnhancedFeature();
        }
    }

     final static BiFunction<Field, Class<?>, Method> METHOD_CACHES = Util.memoize((rawField, clazz) -> {
         var methodName = rawField.getAnnotation(UpdateListener.class).methodName();
         Method method = null;
         while (clazz != null && method == null) {
             try {
                 method = clazz.getDeclaredMethod(methodName, rawField.getType(), rawField.getType());
                 method.setAccessible(true);
             } catch (NoSuchMethodException ignored) {
             }
             clazz = clazz.getSuperclass();
         }
         if (method == null) {
             GTCEu.LOGGER.error("couldn't find the listener method {} for synced field {}", methodName, rawField.getName());
         }
         return method;
     });

    public void initEnhancedFeature() {
        for (IRef syncField : getSyncFields()) {
            var rawField = syncField.getKey().getRawField();
            if (rawField.isAnnotationPresent(RequireRerender.class)) {
                addSyncUpdateListener(syncField.getKey(),  enhancedManaged::scheduleRender);
            }
            if (rawField.isAnnotationPresent(UpdateListener.class)) {
                final var method = METHOD_CACHES.apply(rawField, enhancedManaged.getClass());
                if (method != null) {
                    addSyncUpdateListener(syncField.getKey(), (name, newValue, oldValue) -> {
                        try {
                            method.invoke(enhancedManaged, newValue, oldValue);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }
    }
}
