package com.gregtechceu.gtceu.integration.map.cache.server;

import com.gregtechceu.gtceu.integration.map.cache.DimensionCache;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

public class ServerCacheSavedData extends SavedData {

    public static final String DATA_NAME = "gtceu_ore_vein_cache";

    private DimensionCache backingCache;
    private CompoundTag toRead;

    public static ServerCacheSavedData init(ServerLevel world, final DimensionCache backingCache) {
        ServerCacheSavedData instance = world.getDataStorage()
                .computeIfAbsent(tag -> new ServerCacheSavedData(backingCache, tag),
                        () -> new ServerCacheSavedData(backingCache), DATA_NAME);

        instance.backingCache = backingCache;
        if (backingCache.dirty) {
            instance.setDirty();
        }
        if (instance.toRead != null) {
            backingCache.fromNBT(instance.toRead, false);
            instance.toRead = null;
        }

        return instance;
    }

    public ServerCacheSavedData(DimensionCache backingCache) {
        this.backingCache = backingCache;
    }

    public ServerCacheSavedData(DimensionCache backingCache, CompoundTag compoundTag) {
        this.backingCache = backingCache;
        if (backingCache != null) {
            backingCache.fromNBT(compoundTag, false);
        } else {
            toRead = compoundTag;
        }
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        return backingCache.toNBT(tag, false);
    }
}
