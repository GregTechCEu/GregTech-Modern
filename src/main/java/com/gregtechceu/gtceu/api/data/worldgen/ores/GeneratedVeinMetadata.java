package com.gregtechceu.gtceu.api.data.worldgen.ores;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
public final class GeneratedVeinMetadata {

    public static final Codec<ChunkPos> CHUNK_POS_CODEC = Codec.LONG.xmap(ChunkPos::new, ChunkPos::toLong);
    // spotless:off
    public static final Codec<GeneratedVeinMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    CHUNK_POS_CODEC.fieldOf("origin_chunk").forGetter(GeneratedVeinMetadata::originChunk),
                    BlockPos.CODEC.fieldOf("center").forGetter(GeneratedVeinMetadata::center),
                    GTOreDefinition.CODEC.fieldOf("definition").forGetter(GeneratedVeinMetadata::definition),
                    Codec.BOOL.optionalFieldOf("depleted", false).forGetter(GeneratedVeinMetadata::depleted)
    ).apply(instance, GeneratedVeinMetadata::new));
    // spotless:on
    @Getter
    @NotNull
    private final ChunkPos originChunk;
    @Getter
    @NotNull
    private final BlockPos center;
    @Getter
    @Setter
    @NotNull
    private Holder<GTOreDefinition> definition;
    @Getter
    @Setter
    private boolean depleted;

    public GeneratedVeinMetadata(@NotNull ChunkPos originChunk, @NotNull BlockPos center,
                                 @NotNull Holder<GTOreDefinition> definition) {
        this(originChunk, center, definition, false);
    }

    public GeneratedVeinMetadata(@NotNull ChunkPos originChunk, @NotNull BlockPos center,
                                 @NotNull Holder<GTOreDefinition> definition,
                                 boolean depleted) {
        this.originChunk = originChunk;
        this.center = center;
        this.definition = definition;
        this.depleted = depleted;
    }

    public static GeneratedVeinMetadata readFromPacket(RegistryFriendlyByteBuf buf) {
        ChunkPos origin = new ChunkPos(buf.readVarLong());
        BlockPos center = BlockPos.of(buf.readVarLong());
        Holder<GTOreDefinition> def = GTOreDefinition.STREAM_CODEC.decode(buf);
        return new GeneratedVeinMetadata(origin, center, def, false);
    }

    public void writeToPacket(RegistryFriendlyByteBuf buf) {
        buf.writeVarLong(this.originChunk.toLong());
        buf.writeVarLong(this.center.asLong());
        GTOreDefinition.STREAM_CODEC.encode(buf, this.definition);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GeneratedVeinMetadata that)) return false;

        return originChunk.equals(that.originChunk) && center.equals(that.center) &&
                definition == that.definition;
    }

    @Override
    public int hashCode() {
        int result = originChunk.hashCode();
        result = 31 * result + center.hashCode();
        result = 31 * result + definition.hashCode();
        return result;
    }
}
