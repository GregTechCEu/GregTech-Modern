package com.gregtechceu.gtceu.api.graphnet.predicate;

import com.lowdragmc.lowdraglib.LDLib;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class NetPredicateRegistry {

    private static final Int2ObjectArrayMap<NetPredicateType<?>> REGISTRY;

    private static final BiMap<Integer, String> NAMES_TO_NETWORK_IDS;

    static {
        NetPredicateRegistrationEvent event = new NetPredicateRegistrationEvent();
        MinecraftForge.EVENT_BUS.post(event);
        Set<NetPredicateType<?>> gather = event.getGather();
        NAMES_TO_NETWORK_IDS = HashBiMap.create(gather.size());
        REGISTRY = new Int2ObjectArrayMap<>(gather.size());
        int id = 1;
        for (NetPredicateType<?> type : gather) {
            NAMES_TO_NETWORK_IDS.put(id, type.getSerializedName());
            REGISTRY.put(id, type);
            id++;
        }
    }

    public static String getName(int networkID) {
        return NAMES_TO_NETWORK_IDS.get(networkID);
    }

    public static int getNetworkID(@NotNull String name) {
        return NAMES_TO_NETWORK_IDS.inverse().get(name);
    }

    public static int getNetworkID(@NotNull NetPredicateType<?> type) {
        return getNetworkID(type.getSerializedName());
    }

    public static int getNetworkID(@NotNull EdgePredicate<?, ?> entry) {
        return getNetworkID(entry.getType());
    }

    public static @Nullable NetPredicateType<?> getTypeNullable(int networkID) {
        return REGISTRY.get(networkID);
    }

    public static @Nullable NetPredicateType<?> getTypeNullable(@NotNull String name) {
        return getTypeNullable(getNetworkID(name));
    }

    public static @NotNull NetPredicateType<?> getType(int networkID) {
        NetPredicateType<?> type = REGISTRY.get(networkID);
        if (type == null) throwNonexistenceError();
        assert type != null;
        return type;
    }

    public static @NotNull NetPredicateType<?> getType(@NotNull String name) {
        return getType(getNetworkID(name));
    }

    public static void throwNonexistenceError() {
        if (LDLib.isRemote()) disconnect();
        throw new RuntimeException("Could not find the type of an encoded EdgePredicate. " +
                "This suggests that the server and client have different GT versions or modifications.");
    }

    public static void throwDecodingError() {
        if (LDLib.isRemote()) disconnect();
        throw new RuntimeException("Failed to decode an encoded EdgePredicate. " +
                "This suggests that the server and client have different GT versions or modifications.");
    }

    private static void disconnect() {
        if (Minecraft.getInstance().getConnection() != null)
            Minecraft.getInstance().getConnection()
                    .onDisconnect(Component.translatable("gtceu.universal.net_predicate_disconnect"));
    }

    private NetPredicateRegistry() {}
}
