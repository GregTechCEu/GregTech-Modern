package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeature;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class DikeVeinGenerator extends VeinGenerator {
    public static final Codec<DikeVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(DikeBlockDefinition.CODEC).fieldOf("blocks").forGetter(it -> it.blocks),
            Codec.INT.fieldOf("width").forGetter(it -> it.width),
            Codec.INT.fieldOf("min_y").forGetter(it -> it.minYLevel),
            Codec.INT.fieldOf("max_y").forGetter(it -> it.maxYLevel)
    ).apply(instance, DikeVeinGenerator::new));

    public List<DikeBlockDefinition> blocks;
    @Setter
    public int width;
    @Setter
    public int minYLevel;
    @Setter
    public int maxYLevel;

    public DikeVeinGenerator(GTOreFeatureEntry entry) {
        super(entry);
    }

    @Override
    public Map<Either<BlockState, Material>, Integer> getAllEntries() {
        return this.blocks.stream().flatMap(definition ->
                        definition.block.map(state ->
                                state.stream().map(target -> Either.<BlockState, Material>left(target.state)),
                                material -> Stream.of(Either.<BlockState, Material>right(material))))
                .collect(Collectors.toMap(Function.identity(), value -> 1));
    }

    @Override
    public boolean generate(WorldGenLevel level, RandomSource random, GTOreFeatureEntry entry, BlockPos origin) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(level.getSeed()));
        NormalNoise normalNoise = NormalNoise.create(worldgenRandom, -2, 4.0D); // INT Sparseness - DOUBLE ARRAY Density
        ChunkPos chunkPos = new ChunkPos(origin);
        int size = entry.getClusterSize();
        int xPos = chunkPos.getMinBlockX() + level.getRandom().nextInt(16);
        int zPos = chunkPos.getMinBlockZ() + level.getRandom().nextInt(16);

        int yTop = maxYLevel;
        int yBottom = minYLevel;

        BlockPos basePos = new BlockPos(xPos, yBottom, zPos);

        int blocksPlaced = 0;

        for (int dY = yBottom; dY <= yTop; dY++) {
            for (int dX = -size; dX <= size; dX++) {
                for (int dZ = -size; dZ <= size; dZ++) {
                    float dist = (dX * dX) + (dZ * dZ);
                    if (dist > size) {
                        continue;
                    }
                    if (normalNoise.getValue(dX, dY, dZ) >= 0.5) {
                        if (placeBlock(level, random, new BlockPos(basePos.getX() + dX, dY, basePos.getZ() + dZ), entry)) {
                            ++blocksPlaced;
                        }
                    }
                }
            }
        }
        return blocksPlaced > 0;
    }

    private boolean placeBlock(WorldGenLevel level, RandomSource rand, BlockPos pos, GTOreFeatureEntry entry) {
        int index = rand.nextInt(blocks.size());
        DikeBlockDefinition blockDefinition = blocks.get(index);
        BlockState current = level.getBlockState(pos);
        MutableBoolean returnValue = new MutableBoolean(false);

        if (pos.getY() >= blockDefinition.minY() && pos.getY() <= blockDefinition.maxY()) {
            blockDefinition.block.ifLeft(blockStates -> {
                for (OreConfiguration.TargetBlockState targetState : blockStates) {
                    if (!GTOreFeature.canPlaceOre(current, level::getBlockState, rand, entry, targetState, pos.mutable()))
                        continue;
                    if (targetState.state.isAir())
                        continue;
                    level.setBlock(pos, targetState.state, 2);
                    returnValue.setTrue();
                    break;
                }
            }).ifRight(material -> {
                if (!GTOreFeature.canPlaceOre(current, level::getBlockState, rand, this.entry, material, pos.mutable()))
                    return;
                BlockState currentState = level.getBlockState(pos);
                var prefix = ChemicalHelper.ORES_INVERSE.get(currentState);
                if (prefix == null) return;
                Block toPlace = ChemicalHelper.getBlock(prefix, material);
                if (toPlace == null || toPlace.defaultBlockState().isAir())
                    return;
                level.setBlock(pos, toPlace.defaultBlockState(), 2);
                returnValue.setTrue();
            });
        }
        return returnValue.isTrue();
    }

    @Override
    public VeinGenerator build() {
        return this;
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public void withBlock(DikeBlockDefinition block) {
        if (this.blocks == null) this.blocks = new ArrayList<>();
        this.blocks.add(block);
    }

    public record DikeBlockDefinition(Either<List<OreConfiguration.TargetBlockState>, Material> block, int minY, int maxY) {
        public static final Codec<DikeBlockDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.either(OreConfiguration.TargetBlockState.CODEC.listOf(), GTRegistries.MATERIALS.codec()).fieldOf("block").forGetter(x -> x.block),
                Codec.INT.fieldOf("min_y").forGetter(x -> x.minY),
                Codec.INT.fieldOf("max_y").forGetter(x -> x.maxY)
        ).apply(instance, DikeBlockDefinition::new));
    }
}
