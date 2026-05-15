package com.gregtechceu.gtceu.utils;

import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ManagedFieldHolderMap {

    private static final Map<Class<?>, ManagedFieldHolder> CLASS_HOLDER_MAP = new ConcurrentHashMap<>();

    public static ManagedFieldHolder getManagedFieldHolder(Class<? extends IManaged> clazz) {
        var holder = CLASS_HOLDER_MAP.get(clazz);
        if (holder == null) {
            Class sc = clazz.getSuperclass();
            if (sc != null && sc != Object.class) {
                var sh = getManagedFieldHolder(sc);
                holder = new ManagedFieldHolder(clazz, sh);
                if (holder.getFields().length == sh.getFields().length) {
                    holder = sh;
                }
            }
            if (holder == null) {
                holder = new ManagedFieldHolder(clazz);
            }
            CLASS_HOLDER_MAP.put(clazz, holder);
        }
        return holder;
    }

    public static ManagedFieldHolder createManagedFieldHolder(Class<? extends IManaged> clazz) {
        var holder = CLASS_HOLDER_MAP.get(clazz);
        if (holder == null) {
            holder = new ManagedFieldHolder(clazz);
            CLASS_HOLDER_MAP.put(clazz, holder);
        }
        return holder;
    }
}
