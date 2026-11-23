package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.ClientProxy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Accessors(fluent = true)
public final class GeneratedVeinMetadata {

    // spotless:off
    public static final Codec<ChunkPos> CHUNK_POS_CODEC = Codec.LONG.xmap(ChunkPos::new, ChunkPos::toLong);
    public static final Codec<GTOreDefinition> CLIENT_DEFINITION_CODEC = ResourceLocation.CODEC.flatXmap(rl -> {
        return Optional.ofNullable(ClientProxy.CLIENT_ORE_VEINS.get(rl))
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "Unknown registry key in client ore veins: " + rl));
        }, obj -> {
        return Optional.ofNullable(ClientProxy.CLIENT_ORE_VEINS.inverse().get(obj))
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "Unknown registry element in client ore veins: " + obj));
    });

    public static final Codec<GeneratedVeinMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(GeneratedVeinMetadata::id),
            CHUNK_POS_CODEC.fieldOf("origin_chunk").forGetter(GeneratedVeinMetadata::originChunk),
            BlockPos.CODEC.fieldOf("center").forGetter(GeneratedVeinMetadata::center),
            GTRegistries.ORE_VEINS.codec().fieldOf("definition").forGetter(GeneratedVeinMetadata::definition),
            Codec.BOOL.optionalFieldOf("depleted", false).forGetter(GeneratedVeinMetadata::depleted)
    ).apply(instance, GeneratedVeinMetadata::new));
    public static final Codec<GeneratedVeinMetadata> CLIENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(GeneratedVeinMetadata::id),
            CHUNK_POS_CODEC.fieldOf("origin_chunk").forGetter(GeneratedVeinMetadata::originChunk),
            BlockPos.CODEC.fieldOf("center").forGetter(GeneratedVeinMetadata::center),
            CLIENT_DEFINITION_CODEC.fieldOf("definition").forGetter(GeneratedVeinMetadata::definition),
            Codec.BOOL.optionalFieldOf("depleted", false).forGetter(GeneratedVeinMetadata::depleted)
    ).apply(instance, GeneratedVeinMetadata::new));
    // spotless:on

    @Getter
    @NotNull
    private final ResourceLocation id;
    @Getter
    @NotNull
    private final ChunkPos originChunk;
    // TODO rename to `origin` (needs a datafixer for the NBT)
    @Getter
    @NotNull
    private final BlockPos center;
    @Getter
    @Setter
    @NotNull
    private GTOreDefinition definition;
    @Getter
    @Setter
    private boolean depleted;

    public GeneratedVeinMetadata(@NotNull ResourceLocation id,
                                 @NotNull ChunkPos originChunk,
                                 @NotNull BlockPos center,
                                 @NotNull GTOreDefinition definition) {
        this(id, originChunk, center, definition, false);
    }

    public GeneratedVeinMetadata(@NotNull ResourceLocation id,
                                 @NotNull ChunkPos originChunk,
                                 @NotNull BlockPos center,
                                 @NotNull GTOreDefinition definition,
                                 boolean depleted) {
        this.id = id;
        this.originChunk = originChunk;
        this.center = center;
        this.definition = definition;
        this.depleted = depleted;
    }

    public static GeneratedVeinMetadata readFromPacket(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        ChunkPos originChunk = buf.readChunkPos();
        BlockPos origin = buf.readBlockPos();
        GTOreDefinition def = ClientProxy.CLIENT_ORE_VEINS.get(buf.readResourceLocation());
        boolean depleted = buf.readBoolean();
        return new GeneratedVeinMetadata(id, originChunk, origin, def, depleted);
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeChunkPos(this.originChunk);
        buf.writeBlockPos(this.center);
        buf.writeResourceLocation(GTRegistries.ORE_VEINS.getKey(this.definition));
        buf.writeBoolean(this.depleted);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GeneratedVeinMetadata that)) return false;

        return this.id.equals(that.id) &&
                this.originChunk.equals(that.originChunk) &&
                this.center.equals(that.center) &&
                this.definition.equals(that.definition) &&
                this.depleted == that.depleted;
    }

    @Override
    public int hashCode() {
        int result = this.id.hashCode();
        result = 31 * result + this.originChunk.hashCode();
        result = 31 * result + this.center.hashCode();
        result = 31 * result + this.definition.hashCode();
        result = 31 * result + Boolean.hashCode(this.depleted);
        return result;
    }
}
