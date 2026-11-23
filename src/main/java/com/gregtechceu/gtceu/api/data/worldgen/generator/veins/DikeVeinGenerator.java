package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.WeightedEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.core.SectionPos.blockToSectionCoord;
import static net.minecraft.core.SectionPos.sectionRelative;

@Accessors(fluent = true, chain = true)
@AllArgsConstructor
public class DikeVeinGenerator extends VeinGenerator {

    // spotless:off
    public static final Codec<DikeVeinGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(DikeBlockDefinition.CODEC).fieldOf("blocks").forGetter(it -> it.blocks),
            Codec.INT.fieldOf("min_y").forGetter(it -> it.minYLevel),
            Codec.INT.fieldOf("max_y").forGetter(it -> it.maxYLevel)
    ).apply(instance, DikeVeinGenerator::new));
    // spotless:on

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
    public Long2ObjectMap<OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                   BlockPos origin) {
        Long2ObjectMap<OreBlockPlacer> generatedSections = new Long2ObjectOpenHashMap<>();

        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(level.getSeed()));
        NormalNoise normalNoise = NormalNoise.create(worldgenRandom, -2, 4.0D);

        int size = entry.clusterSize().sample(random);
        int radius = Mth.ceil(size / 2f);

        int xPos = origin.getX() + level.getRandom().nextInt(16);
        int zPos = origin.getZ() + level.getRandom().nextInt(16);
        int sectionX = blockToSectionCoord(xPos);
        int sectionZ = blockToSectionCoord(zPos);

        SectionPos.betweenClosedStream(sectionX - radius, blockToSectionCoord(minYLevel), sectionZ - radius,
                sectionX + radius, blockToSectionCoord(maxYLevel), sectionZ + radius)
                .forEach(pos -> {
                    final long randomSeed = random.nextLong(); // Fully deterministic regardless of chunk order
                    generatedSections.put(pos.asLong(), (worldGenLevel, access, section, layerTarget, sectionPos) -> {
                        placeSection(access, section, layerTarget, sectionPos, randomSeed, entry, normalNoise, radius,
                                xPos, zPos);
                    });
                });

        return generatedSections;
    }

    private void placeSection(BulkSectionAccess access, LevelChunkSection section, RuleTest layerTarget,
                              SectionPos sectionPos, long randomSeed, GTOreDefinition entry, NormalNoise normalNoise,
                              int radius, int globalX, int globalZ) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int sectionMinY = Mth.clamp(minYLevel, sectionPos.minBlockY(), sectionPos.maxBlockY());
        int sectionMaxY = Mth.clamp(maxYLevel, sectionPos.minBlockY(), sectionPos.maxBlockY());

        for (int dy = sectionMinY; dy <= sectionMaxY; dy++) {
            // the Y coordinate is already clamped to valid values only

            for (int dx = -radius; dx <= radius; dx++) {
                int inSectionX = globalX + dx - sectionPos.minBlockX();
                if (inSectionX != sectionRelative(inSectionX)) continue;

                for (int dz = -radius; dz <= radius; dz++) {
                    int inSectionZ = globalZ + dz - sectionPos.minBlockZ();
                    if (inSectionZ != sectionRelative(inSectionZ)) continue;

                    float dist = (dx * dx) + (dz * dz);
                    if (dist > (2f * radius)) continue;

                    pos.set(globalX + dx, dy, globalZ + dz);
                    if (normalNoise.getValue(dx, dy, dz) >= 0.5 && random.nextFloat() <= entry.density()) {
                        placeBlock(access, section, layerTarget, random, pos, entry);
                    }
                }
            }
        }
    }

    private void placeBlock(BulkSectionAccess access, LevelChunkSection section, RuleTest layerTarget,
                            RandomSource random, BlockPos pos, GTOreDefinition entry) {
        DikeBlockDefinition blockDefinition = GTUtil.getRandomItem(random, this.blocks);
        if (blockDefinition == null) return;
        if (pos.getY() < blockDefinition.minY() || pos.getY() > blockDefinition.maxY()) return;

        OreVeinUtil.placeOreBlock(blockDefinition.block, access, section, layerTarget, random, entry, pos);
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
                Codec.either(TargetBlockState.CODEC.listOf(), GTRegistries.MATERIALS.codec()).fieldOf("block")
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
