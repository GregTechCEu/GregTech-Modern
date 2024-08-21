package com.gregtechceu.gtceu.api.graphnet.logic;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public final class NetLogicRegistry {

    private static final Map<String, Supplier<NetLogicEntry<?, ?>>> REGISTRY = new Object2ObjectOpenHashMap<>();

    static void register(NetLogicEntry<?, ?> entry) {
        REGISTRY.putIfAbsent(entry.getName(), entry::getNew);
    }

    public static @Nullable Supplier<@NotNull NetLogicEntry<?, ?>> getSupplierNullable(String name) {
        return REGISTRY.get(name);
    }

    public static @NotNull Supplier<@Nullable NetLogicEntry<?, ?>> getSupplierNotNull(String name) {
        return REGISTRY.getOrDefault(name, () -> null);
    }

    public static @NotNull Supplier<@NotNull NetLogicEntry<?, ?>> getSupplierErroring(String name) {
        Supplier<NetLogicEntry<?, ?>> supplier = REGISTRY.get(name);
        if (supplier == null) throwNonexistenceError();
        return supplier;
    }

    public static void throwNonexistenceError() {
        throw new RuntimeException("Could not find a matching supplier for an encoded NetLogicEntry. " +
                "This suggests that the server and client have different GT versions or modifications.");
    }
}
