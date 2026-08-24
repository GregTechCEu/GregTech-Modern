package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import net.minecraft.core.Registry;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public record RegistryReferenceTransformer<T>(ResourceKey<? extends Registry<T>> registryKey,
                                              Function<T, ResourceLocation> locationGetter)
        implements ValueTransformer<T> {

    @Override
    public Tag serializeNBT(T value, TransformerContext<T> context) {
        return StringTag.valueOf(locationGetter.apply(value).toString());
    }

    @Override
    public T deserializeNBT(Tag tag, TransformerContext<T> context) {
        ResourceLocation location = ResourceLocation
                .parse(ValueTransformer.assertTagType(StringTag.class, tag, context).getAsString());
        ResourceKey<T> elementKey = ResourceKey.create(registryKey, location);
        return context.lookup().lookupOrThrow(registryKey).getOrThrow(elementKey).value();
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf, T value, TransformerContext<T> context) {
        buf.writeResourceKey(ResourceKey.create(registryKey, locationGetter.apply(value)));
    }

    @Override
    public T readFromPacket(RegistryFriendlyByteBuf buf, TransformerContext<T> context) {
        ResourceKey<T> key = buf.readResourceKey(registryKey);
        return buf.registryAccess().holderOrThrow(key).value();
    }
}
