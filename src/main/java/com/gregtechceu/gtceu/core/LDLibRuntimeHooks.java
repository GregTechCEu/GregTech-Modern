package com.gregtechceu.gtceu.core;

import net.minecraft.world.level.storage.ValueInput;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class LDLibRuntimeHooks {

    private static final @Nullable Class<?> AUTO_PERSIST_BLOCK_ENTITY = findClass(
            "com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoPersistBlockEntity");
    private static final @Nullable Class<?> AUTO_SYNC_BLOCK_ENTITY = findClass(
            "com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoSyncBlockEntity");
    private static final @Nullable Class<?> DUMMY_WORLD = findClass("com.lowdragmc.lowdraglib.utils.DummyWorld");
    private static final @Nullable Method GET_SYNC_TAG = findMethod(AUTO_SYNC_BLOCK_ENTITY, "getSyncTag");
    private static final @Nullable Method IS_ASYNC_THREAD_SERVICE = findMethod(
            findClass("com.lowdragmc.lowdraglib.async.AsyncThreadData"), "isThreadService");

    private LDLibRuntimeHooks() {}

    public static boolean isAutoPersistBlockEntity(Object object) {
        return AUTO_PERSIST_BLOCK_ENTITY != null && AUTO_PERSIST_BLOCK_ENTITY.isInstance(object);
    }

    public static boolean isDummyWorld(@Nullable Object object) {
        return DUMMY_WORLD != null && DUMMY_WORLD.isInstance(object);
    }

    public static boolean hasAutoSyncTag(Object object, ValueInput input) {
        String syncTag = getSyncTag(object);
        return syncTag != null && input.child(syncTag).isPresent();
    }

    public static boolean isAsyncThreadService() {
        if (IS_ASYNC_THREAD_SERVICE == null) {
            return false;
        }
        try {
            return (boolean) IS_ASYNC_THREAD_SERVICE.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }

    private static @Nullable String getSyncTag(Object object) {
        if (AUTO_SYNC_BLOCK_ENTITY == null || GET_SYNC_TAG == null || !AUTO_SYNC_BLOCK_ENTITY.isInstance(object)) {
            return null;
        }
        try {
            Object syncTag = GET_SYNC_TAG.invoke(object);
            return syncTag instanceof String string ? string : null;
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private static @Nullable Class<?> findClass(String className) {
        try {
            return Class.forName(className, false, LDLibRuntimeHooks.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }

    private static @Nullable Method findMethod(@Nullable Class<?> type, String methodName) {
        if (type == null) {
            return null;
        }
        try {
            return type.getMethod(methodName);
        } catch (NoSuchMethodException | LinkageError e) {
            return null;
        }
    }
}
