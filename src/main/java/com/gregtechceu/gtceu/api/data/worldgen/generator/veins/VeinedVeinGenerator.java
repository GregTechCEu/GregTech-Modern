package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.WeightedEntry;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;

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

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
public class VeinedVeinGenerator extends VeinGenerator {

    public static final Codec<Either<List<TargetBlockState>, Material>> BLOCK_ENTRY_CODEC = Codec
            .either(TargetBlockState.CODEC.listOf(), GTCEuAPI.materialManager.codec());

    public static final Codec<VeinedVeinGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            VeinBlockDefinition.CODEC.listOf().fieldOf("ore_blocks").forGetter(it -> it.oreBlocks),
            VeinBlockDefinition.CODEC.listOf().fieldOf("rare_blocks").forGetter(it -> it.rareBlocks),
            BlockState.CODEC.fieldOf("filler_block").orElse(Blocks.AIR.defaultBlockState())
                    .forGetter(it -> it.fillerBlock),
            Codec.INT.fieldOf("min_y").forGetter(it -> it.minYLevel),
            Codec.INT.fieldOf("max_y").forGetter(it -> it.maxYLevel),
            Codec.FLOAT.fieldOf("veininess_threshold").orElse(0.4f).forGetter(it -> it.veininessThreshold),
            Codec.INT.fieldOf("edge_roundoff_begin").orElse(20).forGetter(it -> it.edgeRoundoffBegin),
            Codec.DOUBLE.fieldOf("max_edge_roundoff").orElse(0.2).forGetter(it -> it.maxEdgeRoundoff),
            Codec.FLOAT.fieldOf("min_richness").orElse(0.1f).forGetter(it -> it.minRichness),
            Codec.FLOAT.fieldOf("max_richness").orElse(0.3f).forGetter(it -> it.maxRichness),
            Codec.FLOAT.fieldOf("max_richness_threshold").orElse(0.6f).forGetter(it -> it.maxRichnessThreshold),
            Codec.FLOAT.fieldOf("rare_block_chance").orElse(0.02f).forGetter(it -> it.rareBlockChance)// ,
    ).apply(instance, VeinedVeinGenerator::new));

    public List<VeinBlockDefinition> oreBlocks = new ArrayList<>();
    public List<VeinBlockDefinition> rareBlocks = new ArrayList<>();
    @Setter
    public BlockState fillerBlock = Blocks.AIR.defaultBlockState();
    @Setter
    public int minYLevel;
    @Setter
    public int maxYLevel;
    @Setter
    public float veininessThreshold = 0.4f;
    @Setter
    public int edgeRoundoffBegin = 20;
    @Setter
    public double maxEdgeRoundoff = 0.2;
    @Setter
    public float minRichness = 0.1f;
    @Setter
    public float maxRichness = 0.3f;
    @Setter
    public float maxRichnessThreshold = 0.6f;
    @Setter
    public float rareBlockChance = 0.02f;

    public VeinedVeinGenerator(GTOreDefinition entry) {
        super(entry);
    }

    @Override
    public List<VeinEntry> getAllEntries() {
        List<VeinEntry> entries = new ArrayList<>(oreBlocks.size() + rareBlocks.size());
        for (var def : oreBlocks) {
            VeinGenerator.mapTarget(def.block, def.weight).forEach(entries::add);
        }
        for (var def : rareBlocks) {
            VeinGenerator.mapTarget(def.block, def.weight).forEach(entries::add);
        }
        return entries;
    }

    @Override
    public Map<BlockPos, OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                  BlockPos origin) {
        Map<BlockPos, OreBlockPlacer> generatedBlocks = new Object2ObjectOpenHashMap<>();

        Registry<? extends DensityFunction> densityFunctions = level.registryAccess()
                .registry(Registries.DENSITY_FUNCTION).get();

        RandomState randomState = level.getLevel().getChunkSource().randomState();
        Blender blender;
        if (level instanceof WorldGenRegion region) {
            blender = Blender.of(region);
        } else {
            blender = Blender.empty();
        }

        final Blender finalizedBlender = blender;
        DensityFunction veinToggle = mapToNoise(densityFunctions.get(GTFeatures.NEW_ORE_VEIN_TOGGLE), randomState);
        DensityFunction veinRidged = mapToNoise(densityFunctions.get(GTFeatures.NEW_ORE_VEIN_RIDGED), randomState);

        int size = entry.clusterSize().sample(random);

        int radius = Mth.ceil(size / 2f);

        int placedCount = 0;

        int randOffsetX = random.nextInt(16);
        int randOffsetY = random.nextInt(16);
        int randOffsetZ = random.nextInt(16);

        var posMin = origin.offset(-radius, -radius, -radius);
        var posMax = origin.offset(+radius, +radius, +radius);

        for (BlockPos chunkedPos : BlockPos.betweenClosed(posMin, posMax)) {
            final int x = chunkedPos.getX();
            final int y = chunkedPos.getY();
            final int z = chunkedPos.getZ();

            DensityFunction.FunctionContext functionContext = new DensityFunction.FunctionContext() {

                @Override
                public int blockX() {
                    return x + randOffsetX;
                }

                @Override
                public int blockY() {
                    return y + randOffsetY;
                }

                @Override
                public int blockZ() {
                    return z + randOffsetZ;
                }

                @Override
                public Blender getBlender() {
                    return finalizedBlender;
                }
            };

            double toggleNoise = veinToggle.compute(functionContext);
            int blockY = origin.getY();
            double absToggleNoise = Math.abs(toggleNoise);
            int minY = blockY - this.minYLevel;
            int maxY = this.maxYLevel - blockY;
            if (minY < 0 || maxY < 0) {
                continue;
            }
            int lowY = Math.min(maxY, minY);
            double edgeRoundoff = Mth.clampedMap(lowY, 0.0, edgeRoundoffBegin, -maxEdgeRoundoff, 0.0);
            if (absToggleNoise + edgeRoundoff < veininessThreshold) {
                continue;
            }
            if (random.nextFloat() > entry.density()) {
                continue;
            }
            if (veinRidged.compute(functionContext) >= 0.0) {
                continue;
            }
            double chance = Mth.clampedMap(absToggleNoise, veininessThreshold, maxRichnessThreshold, minRichness,
                    maxRichness);

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);

            final var randomSeed = random.nextLong(); // Fully deterministic regardless of chunk order
            generatedBlocks.put(pos, (access, section) -> placeBlock(access, section, randomSeed, entry, chance,
                    rareBlocks, pos, oreBlocks));
        }

        return generatedBlocks;
    }

    private void placeBlock(BulkSectionAccess access, LevelChunkSection section, long randomSeed, GTOreDefinition entry,
                            double chance, List<VeinBlockDefinition> rareEntries,
                            BlockPos.MutableBlockPos pos,
                            List<VeinBlockDefinition> commonEntries) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);
        int sectionX = SectionPos.sectionRelative(pos.getX());
        int sectionY = SectionPos.sectionRelative(pos.getY());
        int sectionZ = SectionPos.sectionRelative(pos.getZ());

        BlockState current = section.getBlockState(sectionX, sectionY, sectionZ);
        if (random.nextFloat() <= entry.density()) {
            if (random.nextFloat() < chance) {
                if (rareBlocks != null && !rareBlocks.isEmpty() && random.nextFloat() < rareBlockChance) {
                    var ore = GTUtil.getRandomItem(random, rareEntries);
                    if (ore == null) return;
                    placeOre(ore.block, current, access, section, random, pos, entry);
                } else {
                    var ore = GTUtil.getRandomItem(random, commonEntries);
                    if (ore == null) return;
                    placeOre(ore.block, current, access, section, random, pos, entry);
                }
            } else {
                if (fillerBlock == null || fillerBlock.isAir())
                    return;
                if (!OreVeinUtil.canPlaceOre(current, access::getBlockState, random, entry, pos))
                    return;
                section.setBlockState(sectionX, sectionY, sectionZ, fillerBlock, false);
            }
        }
    }

    protected static void placeOre(Either<List<TargetBlockState>, Material> block, BlockState current,
                                   BulkSectionAccess level, LevelChunkSection section, RandomSource random,
                                   BlockPos.MutableBlockPos pos, GTOreDefinition entry) {
        int x = SectionPos.sectionRelative(pos.getX());
        int y = SectionPos.sectionRelative(pos.getY());
        int z = SectionPos.sectionRelative(pos.getZ());

        block.ifLeft(blockStates -> {
            for (TargetBlockState targetState : blockStates) {
                if (!OreVeinUtil.canPlaceOre(current, level::getBlockState, random, entry, targetState, pos))
                    continue;
                if (targetState.state.isAir())
                    continue;
                section.setBlockState(x, y, z, targetState.state, false);
                break;
            }
        }).ifRight(material -> {
            if (!OreVeinUtil.canPlaceOre(current, level::getBlockState, random, entry, pos))
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

    @Override
    public VeinGenerator build() {
        return this;
    }

    @Override
    public VeinGenerator copy() {
        return new VeinedVeinGenerator(new ArrayList<>(this.oreBlocks), new ArrayList<>(this.rareBlocks),
                this.fillerBlock, this.minYLevel, this.maxYLevel, this.veininessThreshold, this.edgeRoundoffBegin,
                this.maxEdgeRoundoff, this.minRichness, this.maxRichness, this.maxRichnessThreshold,
                this.rareBlockChance);
    }

    @Override
    public Codec<? extends VeinGenerator> codec() {
        return CODEC;
    }

    public VeinedVeinGenerator oreBlock(Material block, int weight) {
        return this.oreBlock(new VeinBlockDefinition(block, weight));
    }

    public VeinedVeinGenerator oreBlock(BlockState blockState, int weight) {
        TargetBlockState target = OreConfiguration.target(AlwaysTrueTest.INSTANCE, blockState);
        return this.oreBlock(new VeinBlockDefinition(List.of(target), weight));
    }

    public VeinedVeinGenerator oreBlock(VeinBlockDefinition material) {
        this.oreBlocks.add(material);
        return this;
    }

    public VeinedVeinGenerator rareBlock(Material block, int weight) {
        return this.rareBlock(new VeinBlockDefinition(block, weight));
    }

    public VeinedVeinGenerator rareBlock(BlockState blockState, int weight) {
        TargetBlockState target = OreConfiguration.target(AlwaysTrueTest.INSTANCE, blockState);
        return this.rareBlock(new VeinBlockDefinition(List.of(target), weight));
    }

    public VeinedVeinGenerator rareBlock(VeinBlockDefinition material) {
        this.rareBlocks.add(material);
        return this;
    }

    public record VeinBlockDefinition(Either<List<TargetBlockState>, Material> block, int weight)
            implements WeightedEntry {

        public static final Codec<VeinBlockDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BLOCK_ENTRY_CODEC.fieldOf("block").forGetter(x -> x.block),
                Codec.INT.fieldOf("weight").forGetter(x -> x.weight)).apply(instance, VeinBlockDefinition::new));

        public VeinBlockDefinition(Material block, int weight) {
            this(Either.right(block), weight);
        }

        public VeinBlockDefinition(List<TargetBlockState> block, int weight) {
            this(Either.left(block), weight);
        }
    }

    private static DensityFunction mapToNoise(DensityFunction function, RandomState randomState) {
        return function.mapAll(new DensityFunction.Visitor() {

            @Override
            public DensityFunction apply(DensityFunction densityFunction) {
                return densityFunction;
            }

            @Override
            public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder) {
                var holder = noiseHolder.noiseData();
                var noise = randomState.getOrCreateNoise(holder.unwrapKey().orElseThrow());
                return new DensityFunction.NoiseHolder(holder, noise);
            }
        });
    }
}
