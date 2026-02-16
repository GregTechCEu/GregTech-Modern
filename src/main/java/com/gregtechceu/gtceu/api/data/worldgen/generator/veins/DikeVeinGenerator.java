package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.WeightedEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Accessors(fluent = true, chain = true)
@AllArgsConstructor
public class DikeVeinGenerator extends VeinGenerator {

    public static final Codec<DikeVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(DikeBlockDefinition.CODEC).fieldOf("blocks").forGetter(it -> it.blocks),
            Codec.INT.fieldOf("min_y").forGetter(it -> it.minYLevel),
            Codec.INT.fieldOf("max_y").forGetter(it -> it.maxYLevel)).apply(instance, DikeVeinGenerator::new));

    public List<DikeBlockDefinition> blocks;
    @Setter
    public int minYLevel;
    @Setter
    public int maxYLevel;

    public DikeVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

    @Override
    public List<VeinEntry> getAllEntries() {
        List<VeinEntry> entries = new ArrayList<>(this.blocks.size());
        for (var def : this.blocks) {
            VeinGenerator.mapTarget(def.block, def.weight).forEach(entries::add);
        }
        return entries;
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();

        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(level.getSeed()));
        NormalNoise normalNoise = NormalNoise.create(worldgenRandom, -2, 4.0D);
        ChunkPos chunkPos = new ChunkPos(origin);

        float density = entry.density();
        int size = entry.clusterSize().sample(random);

        int radius = Mth.ceil(size / 2f);

        int xPos = chunkPos.getMinBlockX() + level.getRandom().nextInt(16);
        int zPos = chunkPos.getMinBlockZ() + level.getRandom().nextInt(16);

        int yTop = maxYLevel;
        int yBottom = minYLevel;

        BlockPos basePos = new BlockPos(xPos, yBottom, zPos);

        for (int dY = yBottom; dY <= yTop; dY++) {
            for (int dX = -radius; dX <= radius; dX++) {
                for (int dZ = -radius; dZ <= radius; dZ++) {
                    float dist = (dX * dX) + (dZ * dZ);
                    if (dist > radius * 2) {
                        continue;
                    }
                    BlockPos pos = new BlockPos(basePos.getX() + dX, dY, basePos.getZ() + dZ);
                    if (normalNoise.getValue(dX, dY, dZ) >= 0.5 && random.nextFloat() <= density) {
                        final var randomSeed = random.nextLong(); // Fully deterministic regardless of chunk order

                        generatedBlocks.put(pos,
                                (access, section) -> placeBlock(access, section, randomSeed, pos, entry));
                    }
                }
            }
        }

        return generatedBlocks;
    }

    private void placeBlock(BulkSectionAccess level, LevelChunkSection section, long randomSeed, BlockPos pos,
                            GTOreDefinition entry) {
        var rand = new XoroshiroRandomSource(randomSeed);
        DikeBlockDefinition blockDefinition = GTUtil.getRandomItem(rand, blocks);
        BlockState current = level.getBlockState(pos);

        int x = SectionPos.sectionRelative(pos.getX());
        int y = SectionPos.sectionRelative(pos.getY());
        int z = SectionPos.sectionRelative(pos.getZ());

        if (pos.getY() >= blockDefinition.minY() && pos.getY() <= blockDefinition.maxY()) {
            blockDefinition.block.ifLeft(blockStates -> {
                for (TargetBlockState targetState : blockStates) {
                    if (!OreVeinUtil.canPlaceOre(current, level::getBlockState, rand, entry, targetState,
                            pos.mutable()))
                        continue;
                    if (targetState.state.isAir())
                        continue;
                    section.setBlockState(x, y, z, targetState.state, false);
                    break;
                }
            }).ifRight(material -> {
                if (!OreVeinUtil.canPlaceOre(current, level::getBlockState, rand, entry, pos.mutable()))
                    return;
                BlockState currentState = level.getBlockState(pos);
                var prefix = ChemicalHelper.getOrePrefix(currentState);
                if (prefix.isEmpty()) return;
                Block toPlace = ChemicalHelper.getBlock(prefix.get(), material);
                if (toPlace == null || toPlace.defaultBlockState().isAir())
                    return;
                section.setBlockState(x, y, z, toPlace.defaultBlockState(), false);
            });
        }
    }

    @Override
    public VeinGenerator build() {
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new DikeVeinGenerator(new ArrayList<>(blocks), minYLevel, maxYLevel);
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public DikeVeinGenerator withBlock(Material block, int weight, int minY, int maxY) {
        return this.withBlock(new DikeBlockDefinition(block, weight, minY, maxY));
    }

    public DikeVeinGenerator withBlock(BlockState blockState, int weight, int minY, int maxY) {
        TargetBlockState target = OreConfiguration.target(AlwaysTrueTest.INSTANCE, blockState);
        return this.withBlock(new DikeBlockDefinition(List.of(target), weight, minY, maxY));
    }

    public DikeVeinGenerator withBlock(DikeBlockDefinition block) {
        if (this.blocks == null) this.blocks = new ArrayList<>();
        this.blocks.add(block);
        return this;
    }

    public record DikeBlockDefinition(Either<List<TargetBlockState>, Material> block, int weight, int minY, int maxY)
            implements WeightedEntry {

        public static final Codec<DikeBlockDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.either(TargetBlockState.CODEC.listOf(), GTCEuAPI.materialManager.codec()).fieldOf("block")
                        .forGetter(x -> x.block),
                Codec.INT.fieldOf("weight").forGetter(x -> x.weight),
                Codec.INT.fieldOf("min_y").orElse(320).forGetter(x -> x.minY),
                Codec.INT.fieldOf("max_y").orElse(-64).forGetter(x -> x.maxY))
                .apply(instance, DikeBlockDefinition::new));

        public DikeBlockDefinition(Material block, int weight, int minY, int maxY) {
            this(Either.right(block), weight, minY, maxY);
        }

        public DikeBlockDefinition(List<TargetBlockState> block, int weight, int minY, int maxY) {
            this(Either.left(block), weight, minY, maxY);
        }
    }
}
