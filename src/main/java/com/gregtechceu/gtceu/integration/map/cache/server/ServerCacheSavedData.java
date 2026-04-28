package com.gregtechceu.gtceu.integration.map.cache.server;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.map.cache.DimensionCache;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

public class ServerCacheSavedData extends SavedData {

    public static final String DATA_NAME = "gtceu_ore_vein_cache";
    public static final SavedDataType<ServerCacheSavedData> TYPE = new SavedDataType<>(
            GTCEu.id(DATA_NAME),
            level -> new ServerCacheSavedData(null),
            ServerCacheSavedData::codec);

    private DimensionCache backingCache;
    private CompoundTag toRead;
    private HolderLookup.Provider toReadProvider;

    public static ServerCacheSavedData init(ServerLevel world, final DimensionCache backingCache) {
        ServerCacheSavedData instance = world.getDataStorage().computeIfAbsent(TYPE);

        instance.backingCache = backingCache;
        if (backingCache.dirty) {
            instance.setDirty();
        }
        if (instance.toRead != null) {
            backingCache.fromNBT(instance.toRead, instance.toReadProvider);
            instance.toRead = null;
            instance.toReadProvider = null;
        }

        return instance;
    }

    public ServerCacheSavedData(DimensionCache backingCache) {
        this.backingCache = backingCache;
    }

    public ServerCacheSavedData(DimensionCache backingCache,
                                CompoundTag compoundTag, HolderLookup.Provider registries) {
        this.backingCache = backingCache;
        if (backingCache != null) {
            backingCache.fromNBT(compoundTag, registries);
        } else {
            toRead = compoundTag;
            toReadProvider = registries;
        }
    }

    private static Codec<ServerCacheSavedData> codec(ServerLevel level) {
        return CompoundTag.CODEC.xmap(
                tag -> new ServerCacheSavedData(null, tag, level.registryAccess()),
                data -> data.save(new CompoundTag(), level.registryAccess()));
    }

    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        if (backingCache == null) {
            return tag;
        }
        return backingCache.toNBT(tag, registries);
    }
}
