package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WorldIDSaveData extends SavedData {

    private static WorldIDSaveData instance;
    private static final SavedDataType<WorldIDSaveData> TYPE = new SavedDataType<>(
            GTCEu.id("world_id"),
            WorldIDSaveData::new,
            WorldIDSaveData::codec);

    private String worldID;

    @SuppressWarnings("unused")
    public WorldIDSaveData(ServerLevel level) {
        worldID = level.getServer().getWorldData().getLevelName() + "_" + UUID.randomUUID();
        this.setDirty();
    }

    public WorldIDSaveData(CompoundTag tag) {
        this.worldID = tag.getStringOr("id", "");
    }

    private static Codec<WorldIDSaveData> codec(ServerLevel level) {
        return CompoundTag.CODEC.xmap(
                WorldIDSaveData::new,
                data -> data.save(new CompoundTag(), level.registryAccess()));
    }

    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.putString("id", worldID);
        return compoundTag;
    }

    public static void init(ServerLevel level) {
        instance = level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static String getWorldID() {
        return instance.worldID;
    }
}
