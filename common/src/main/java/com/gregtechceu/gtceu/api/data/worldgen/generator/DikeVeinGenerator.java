package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeature;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Accessors(fluent = true, chain = true)
@AllArgsConstructor
public class DikeVeinGenerator extends VeinGenerator {
    public static final Codec<DikeVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(DikeBlockDefinition.CODEC).fieldOf("blocks").forGetter(it -> it.blocks),
            Codec.INT.fieldOf("min_y").forGetter(it -> it.minYLevel),
            Codec.INT.fieldOf("max_y").forGetter(it -> it.maxYLevel)
    ).apply(instance, DikeVeinGenerator::new));

    public List<DikeBlockDefinition> blocks;
    @Setter
    public int minYLevel;
    @Setter
    public int maxYLevel;

    public DikeVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

    @Override
    public List<Map.Entry<Either<BlockState, Material>, Integer>> getAllEntries() {
        return this.blocks.stream()
                .flatMap(definition ->
                        definition.block.map(state ->
                                state.stream().map(target -> Map.entry(Either.<BlockState, Material>left(target.state), definition.weight)),
                                material -> Stream.of(Map.entry(Either.<BlockState, Material>right(material), definition.weight))))
                .collect(Collectors.toList());
    }

    @Override
    public boolean generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry, BlockPos origin) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(level.getSeed()));
        NormalNoise normalNoise = NormalNoise.create(worldgenRandom, -2, 4.0D);
        ChunkPos chunkPos = new ChunkPos(origin);

        float density = entry.getDensity();
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
                    if (dist > size * 2) {
                        continue;
                    }
                    if (normalNoise.getValue(dX, dY, dZ) >= 0.5 && random.nextFloat() <= density) {
                        if (placeBlock(level, random, new BlockPos(basePos.getX() + dX, dY, basePos.getZ() + dZ), entry)) {
                            ++blocksPlaced;
                        }
                    }
                }
            }
        }
        return blocksPlaced > 0;
    }

    private boolean placeBlock(WorldGenLevel level, RandomSource rand, BlockPos pos, GTOreDefinition entry) {
        List<? extends Map.Entry<Integer, DikeBlockDefinition>> entries = blocks.stream().map(b -> Map.entry(b.weight, b)).toList();
        DikeBlockDefinition blockDefinition = blocks.get(GTUtil.getRandomItem(rand, entries, entries.size()));
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
                if (!GTOreFeature.canPlaceOre(current, level::getBlockState, rand, entry, pos.mutable()))
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
    public VeinGenerator copy() {
        return new DikeVeinGenerator(blocks, minYLevel, maxYLevel);
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public DikeVeinGenerator withBlock(DikeBlockDefinition block) {
        if (this.blocks == null) this.blocks = new ArrayList<>();
        this.blocks.add(block);
        return this;
    }

    public record DikeBlockDefinition(Either<List<OreConfiguration.TargetBlockState>, Material> block, int weight, int minY, int maxY) {
        public static final Codec<DikeBlockDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.either(OreConfiguration.TargetBlockState.CODEC.listOf(), GTRegistries.MATERIALS.codec()).fieldOf("block").forGetter(x -> x.block),
                Codec.INT.fieldOf("weight").forGetter(x -> x.weight),
                Codec.INT.fieldOf("min_y").orElse(320).forGetter(x -> x.minY),
                Codec.INT.fieldOf("max_y").orElse(-64).forGetter(x -> x.maxY)
        ).apply(instance, DikeBlockDefinition::new));

        public DikeBlockDefinition(Material block, int weight, int minY, int maxY) {
            this(Either.right(block), weight, minY, maxY);
        }

        public DikeBlockDefinition(List<OreConfiguration.TargetBlockState> block, int weight, int minY, int maxY) {
            this(Either.left(block), weight, minY, maxY);
        }
    }
}
