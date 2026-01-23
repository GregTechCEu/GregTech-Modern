package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.syncsystem.ISyncManaged;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ResourceLocationReferenceTransformer<T> extends ValueTransformer<T> {

    private final Function<T, ResourceLocation> getResourceLocation;
    private final Function<ResourceLocation, T> loadFromLocation;

    public ResourceLocationReferenceTransformer(Function<T, ResourceLocation> getResourceLocation,
                                                Function<ResourceLocation, T> loadFromLocation) {
        this.getResourceLocation = getResourceLocation;
        this.loadFromLocation = loadFromLocation;
    }

    @Override
    public Tag serializeNBT(T value, ISyncManaged holder) {
        return StringTag.valueOf(getResourceLocation.apply(value).toString());
    }

    @Override
    public T deserializeNBT(Tag tag, ISyncManaged holder, @Nullable T currentVal) {
        ResourceLocation location = ResourceLocation.tryParse(tag.getAsString());
        if (location == null) return null;
        return loadFromLocation.apply(ResourceLocation.tryParse(tag.getAsString()));
    }
}
