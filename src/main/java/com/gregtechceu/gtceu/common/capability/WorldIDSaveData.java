package com.gregtechceu.gtceu.common.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.UUID;

public class WorldIDSaveData extends SavedData {

    private static WorldIDSaveData instance;
    private static final String DATA_NAME = "gtceu_world_id";

    private String worldID;

    @SuppressWarnings("unused")
    public WorldIDSaveData(ServerLevel level) {
        worldID = level.getServer().getWorldData().getLevelName() + "_" + UUID.randomUUID();
        this.setDirty();
    }

    public WorldIDSaveData(CompoundTag tag) {
        this.worldID = tag.getString("id");
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putString("id", worldID);
        return compoundTag;
    }

    public static void init(ServerLevel world) {
        instance = world.getDataStorage()
                .computeIfAbsent(WorldIDSaveData::new, () -> new WorldIDSaveData(world), DATA_NAME);
    }

    public static String getWorldID() {
        return instance.worldID;
    }
}
