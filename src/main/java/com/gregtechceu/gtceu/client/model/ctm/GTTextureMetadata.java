package com.gregtechceu.gtceu.client.model.ctm;

import com.gregtechceu.gtceu.utils.TriState;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.Optional;

public record GTTextureMetadata(Optional<ResourceLocation> connectionTexture, TriState bloom) {

    // spotless:off
    public static final Codec<GTTextureMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.lenientOptionalFieldOf("connection_texture").forGetter(GTTextureMetadata::connectionTexture),
            TriState.CODEC.optionalFieldOf("bloom", TriState.DEFAULT).forGetter(GTTextureMetadata::bloom)
    ).apply(instance, GTTextureMetadata::new));
    public static final MetadataSectionType<GTTextureMetadata> TYPE = MetadataSectionType.fromCodec("gtceu", CODEC);
    // spotless:on

    public static final GTTextureMetadata EMPTY = new GTTextureMetadata(Optional.empty(), TriState.DEFAULT);

    /**
     * @apiNote This method throws {@link IOException} even though it isn't specified in the method definition.
     */
    @SneakyThrows(IOException.class)
    public static Optional<GTTextureMetadata> getForResourceUnsafe(Resource resource) {
        return resource.metadata().getSection(TYPE);
    }
}
