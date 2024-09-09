package com.gregtechceu.gtceu.api.graphnet.logic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class NetLogicRegistry {

    private static final Map<String, NetLogicEntryType<?>> REGISTRY = new Object2ObjectOpenHashMap<>();

    static void register(NetLogicEntryType<?> entry) {
        REGISTRY.putIfAbsent(entry.getSerializedName(), entry);
    }

    public static @Nullable NetLogicEntryType<?> getTypeNullable(String name) {
        return REGISTRY.get(name);
    }

    public static @NotNull NetLogicEntryType<?> getTypeNotNull(String name) {
        return REGISTRY.getOrDefault(name, EmptyLogicEntry.TYPE);
    }

    public static @NotNull NetLogicEntryType<?> getTypeErroring(String name) {
        NetLogicEntryType<?> type = REGISTRY.get(name);
        if (type == null) throwNonexistenceError();
        return type;
    }

    public static void throwNonexistenceError() {
        throw new RuntimeException("Could not find a matching supplier for an encoded NetLogicEntry. " +
                "This suggests that the server and client have different GT versions or modifications.");
    }

    private static class EmptyLogicEntry extends NetLogicEntry<EmptyLogicEntry, CompoundTag> {

        private static final NetLogicEntryType<EmptyLogicEntry> TYPE = new NetLogicEntryType<>("Empty",
                EmptyLogicEntry::new);

        protected EmptyLogicEntry() {
            super(TYPE);
        }

        @Override
        public @Nullable CompoundTag serializeNBT() {
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(CompoundTag arg) {}

        @Override
        public void encode(FriendlyByteBuf buf, boolean fullChange) {}

        @Override
        public void decode(FriendlyByteBuf buf, boolean fullChange) {}
    }
}
