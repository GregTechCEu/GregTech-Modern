package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.placeholder.Placeholder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PlaceholderSavedData extends SavedData {

    private final ServerLevel level;
    private final CompoundTag tag;

    public static PlaceholderSavedData getOrCreate(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<PlaceholderSavedData>(() -> new PlaceholderSavedData(level),
                                (tag, provider) -> new PlaceholderSavedData(level, tag, provider)),
                        "gtceu_placeholder_data");
    }

    public PlaceholderSavedData(ServerLevel level) {
        this(level, new CompoundTag(), (HolderLookup.Provider) null);
    }

    public PlaceholderSavedData(ServerLevel level, CompoundTag tag, @Nullable HolderLookup.Provider provider) {
        this.level = level;
        this.tag = tag.getCompound("data");
    }

    public CompoundTag getPlaceholderData(Placeholder placeholder) {
        if (!tag.contains(placeholder.getName()))
            tag.put(placeholder.getName(), new CompoundTag());
        return tag.getCompound(placeholder.getName());
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("data", this.tag);
        return tag;
    }
}
