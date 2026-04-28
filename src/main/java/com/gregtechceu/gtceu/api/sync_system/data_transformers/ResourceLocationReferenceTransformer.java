package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ResourceLocationReferenceTransformer<T> implements ValueTransformer<T> {

    private final Function<T, Identifier> getResourceLocation;
    private final Function<Identifier, T> loadFromLocation;

    public ResourceLocationReferenceTransformer(Function<T, Identifier> getResourceLocation,
                                                Function<Identifier, T> loadFromLocation) {
        this.getResourceLocation = getResourceLocation;
        this.loadFromLocation = loadFromLocation;
    }

    @Override
    public Tag serializeNBT(T value, ValueTransformer.TransformerContext<T> context) {
        return StringTag.valueOf(getResourceLocation.apply(value).toString());
    }

    @Override
    public @Nullable T deserializeNBT(Tag tag, ValueTransformer.TransformerContext<T> context) {
        Identifier location = Identifier
                .tryParse(ValueTransformer.assertTagType(StringTag.class, tag, context).value());
        if (location == null) return null;
        return loadFromLocation.apply(location);
    }
}
