package com.gregtechceu.gtceu.client.model.ctm;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.utils.TriState;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;

public record GTTextureMetadata(@Nullable ResourceLocation connectionTexture, TriState bloom) {

    public static final String SECTION_NAME = GTCEu.MOD_ID;
    public static final MetadataSectionSerializer<GTTextureMetadata> SERIALIZER = new Serializer();

    public static final GTTextureMetadata EMPTY = new GTTextureMetadata(null, TriState.DEFAULT);

    /**
     * @apiNote This method throws {@link IOException} even though it isn't specified in the method definition.
     */
    @SneakyThrows(IOException.class)
    public static Optional<GTTextureMetadata> getForResourceUnsafe(Resource resource) {
        return resource.metadata().getSection(SERIALIZER);
    }

    public GTTextureMetadata {
        // Optional codec fields can't have null as the default value, so we do this instead.
        // It's impossible to define an entirely empty ResourceLocation in a resource file,
        // as even ":" is converted to "minecraft:". Thus, this should be entirely safe.
        if (connectionTexture == Serializer.EMPTY_CONNECTION) connectionTexture = null;
    }

    public static class Serializer implements MetadataSectionSerializer<GTTextureMetadata> {

        protected static final ResourceLocation EMPTY_CONNECTION = ResourceLocation.fromNamespaceAndPath("", "");

        // spotless:off
        public static final Codec<GTTextureMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("connection_texture", EMPTY_CONNECTION).forGetter(GTTextureMetadata::connectionTexture),
                TriState.CODEC.optionalFieldOf("bloom", TriState.DEFAULT).forGetter(GTTextureMetadata::bloom)
        ).apply(instance, GTTextureMetadata::new));
        // spotless:on

        @Override
        public GTTextureMetadata fromJson(@Nullable JsonObject json) throws JsonParseException {
            return CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(JsonParseException::new);
        }

        @Override
        public String getMetadataSectionName() {
            return SECTION_NAME;
        }
    }
}
