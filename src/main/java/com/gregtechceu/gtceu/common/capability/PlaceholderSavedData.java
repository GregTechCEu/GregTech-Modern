package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.placeholder.Placeholder;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PlaceholderSavedData extends SavedData {

    private final ServerLevel level;
    private final CompoundTag tag;
    private static final SavedDataType<PlaceholderSavedData> TYPE = new SavedDataType<>(
            GTCEu.id("placeholder_data"),
            PlaceholderSavedData::new,
            PlaceholderSavedData::codec);

    public static PlaceholderSavedData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public PlaceholderSavedData(ServerLevel level) {
        this(level, new CompoundTag(), (HolderLookup.Provider) null);
    }

    public PlaceholderSavedData(ServerLevel level, CompoundTag tag, @Nullable HolderLookup.Provider provider) {
        this.level = level;
        this.tag = tag.getCompoundOrEmpty("data");
    }

    private static Codec<PlaceholderSavedData> codec(ServerLevel level) {
        return CompoundTag.CODEC.xmap(
                tag -> new PlaceholderSavedData(level, tag, level.registryAccess()),
                data -> data.save(new CompoundTag(), level.registryAccess()));
    }

    public CompoundTag getPlaceholderData(Placeholder placeholder) {
        if (!tag.contains(placeholder.getName()))
            tag.put(placeholder.getName(), new CompoundTag());
        return tag.getCompoundOrEmpty(placeholder.getName());
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("data", this.tag);
        return tag;
    }
}
