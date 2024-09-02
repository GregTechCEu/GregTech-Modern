package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public record GeneratedVeinMetadata(
                                    @NotNull ResourceLocation id,
                                    @NotNull ChunkPos originChunk,
                                    @NotNull BlockPos center,
                                    @NotNull GTOreDefinition definition) {

    public static final Codec<ChunkPos> CHUNK_POS_CODEC = Codec.LONG.xmap(ChunkPos::new, ChunkPos::toLong);

    public static final Codec<GeneratedVeinMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(GeneratedVeinMetadata::id),
            CHUNK_POS_CODEC.fieldOf("origin_chunk").forGetter(GeneratedVeinMetadata::originChunk),
            BlockPos.CODEC.fieldOf("center").forGetter(GeneratedVeinMetadata::center),
            GTRegistries.ORE_VEINS.codec().fieldOf("definition").forGetter(GeneratedVeinMetadata::definition))
            .apply(instance, GeneratedVeinMetadata::new));

    public static GeneratedVeinMetadata readFromPacket(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        ChunkPos origin = new ChunkPos(buf.readVarLong());
        BlockPos center = BlockPos.of(buf.readVarLong());
        GTOreDefinition def = GTRegistries.ORE_VEINS.get(buf.readResourceLocation());
        return new GeneratedVeinMetadata(id, origin, center, def);
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeVarLong(this.originChunk.toLong());
        buf.writeVarLong(this.center.asLong());
        buf.writeResourceLocation(GTRegistries.ORE_VEINS.getKey(this.definition));
    }
}
