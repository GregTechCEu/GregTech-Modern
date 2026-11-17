package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreBlockPlacer;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.worldgen.GTDensityFunctions;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.WeightedEntry;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.GTCEu.LOGGER;
import static net.minecraft.core.SectionPos.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
public class VeinedVeinGenerator extends VeinGenerator {

    // Recommended default parameters for veined veins.
    // check out the constants in net.minecraft.world.level.levelgen.OreVeinifier for vanilla's.

    public static final float DEFAULT_VEININESS_THRESHOLD = 0.01f;
    public static final int DEFAULT_EDGE_ROUNDOFF_BEGIN = 3;
    public static final float DEFAULT_MAX_EDGE_ROUNDOFF = 0.1f;
    public static final float DEFAULT_VEIN_DENSITY = 1.0f;
    public static final float DEFAULT_MIN_RICHNESS = 0.7f;
    public static final float DEFAULT_MAX_RICHNESS = 1.0f;
    public static final float DEFAULT_MAX_RICHNESS_THRESHOLD = 0.175f;
    public static final float DEFAULT_RARE_BLOCK_CHANCE = 0.02f;

    public static final Codec<Either<List<TargetBlockState>, Material>> BLOCK_ENTRY_CODEC = Codec
            .either(TargetBlockState.CODEC.listOf(), GTRegistries.MATERIALS.codec());
    private static final Codec<Float> ZERO_TO_ONE_CODEC = Codec.floatRange(0.0f, 1.0f);

    // spotless:off
    public static final Codec<VeinedVeinGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            VeinBlockDefinition.CODEC.listOf().fieldOf("ore_blocks").forGetter(it -> it.oreBlocks),
            VeinBlockDefinition.CODEC.listOf().fieldOf("rare_blocks").forGetter(it -> it.rareBlocks),
            BlockState.CODEC.fieldOf("filler_block").orElse(Blocks.AIR.defaultBlockState()).forGetter(it -> it.fillerBlock),
            Codec.INT.fieldOf("min_y").forGetter(it -> it.minYLevel),
            Codec.INT.fieldOf("max_y").forGetter(it -> it.maxYLevel),
            Codec.FLOAT.fieldOf("veininess_threshold").orElse(DEFAULT_VEININESS_THRESHOLD).forGetter(it -> it.veininessThreshold),
            Codec.INT.fieldOf("edge_roundoff_begin").orElse(DEFAULT_EDGE_ROUNDOFF_BEGIN).forGetter(it -> it.edgeRoundoffBegin),
            ZERO_TO_ONE_CODEC.fieldOf("max_edge_roundoff").orElse(DEFAULT_MAX_EDGE_ROUNDOFF).forGetter(it -> it.maxEdgeRoundoff),
            ZERO_TO_ONE_CODEC.fieldOf("min_richness").orElse(DEFAULT_MIN_RICHNESS).forGetter(it -> it.minRichness),
            ZERO_TO_ONE_CODEC.fieldOf("max_richness").orElse(DEFAULT_MAX_RICHNESS).forGetter(it -> it.maxRichness),
            Codec.FLOAT.fieldOf("max_richness_threshold").orElse(DEFAULT_MAX_RICHNESS_THRESHOLD).forGetter(it -> it.maxRichnessThreshold),
            ZERO_TO_ONE_CODEC.fieldOf("rare_block_chance").orElse(DEFAULT_RARE_BLOCK_CHANCE).forGetter(it -> it.rareBlockChance)
    ).apply(instance, VeinedVeinGenerator::new));
    // spotless:on

    private static final NoiseSettings DEFAULT_NOISE_SETTINGS = new NoiseSettings(DimensionType.MIN_Y, DimensionType.MAX_Y, 1, 2);

    public @NotNull List<VeinBlockDefinition> oreBlocks = new ArrayList<>();
    public @NotNull List<VeinBlockDefinition> rareBlocks = new ArrayList<>();
    @Setter
    public BlockState fillerBlock = Blocks.AIR.defaultBlockState();
    @Setter
    public int minYLevel;
    @Setter
    public int maxYLevel;
    /**
     * Defines how "sharp" the edges of the vein are. Higher values result in more "blurry" edges.<br>
     * In practice, this gates the vein noise's minimum output value. No position with a value below this will
     * have an ore.
     * <p>
     * Must be {@code <= maxRichnessThreshold}.
     */
    @Setter
    public float veininessThreshold = DEFAULT_VEININESS_THRESHOLD;
    /**
     * The distance (in blocks) from the nearest edge of the vein that the edges of the vein will become "blurred" at to
     * make them softer.
     */
    @Setter
    public int edgeRoundoffBegin = DEFAULT_EDGE_ROUNDOFF_BEGIN;
    /**
     * Maximum amount of edge rounding that can be done. Applied on top of {@link #veininessThreshold} when filtering
     * valid placement locations.
     */
    @Setter
    public float maxEdgeRoundoff = DEFAULT_MAX_EDGE_ROUNDOFF;
    /**
     * Richness is the ratio of filler blocks to ore blocks. higher value means more ore blocks.<br>
     * Must be {@code <= maxRichness}
     */
    @Setter
    public float minRichness = DEFAULT_MIN_RICHNESS;
    /**
     * Richness is the ratio of filler blocks to ore blocks. higher value means more ore blocks.<br>
     * Must be {@code >= minRichness}
     */
    @Setter
    public float maxRichness = DEFAULT_MAX_RICHNESS;
    /**
     * Defines how "dense" the vein is. Higher values result in less "filled" veins.<br>
     * In practice, this tunes the "assumed maximum" value of veininess noise and raising this lowers the real richness
     * of the vein.
     * <p>
     * Must be {@code >= veininessThreshold}.
     */
    @Setter
    public float maxRichnessThreshold = DEFAULT_MAX_RICHNESS_THRESHOLD;
    @Setter
    public float rareBlockChance = DEFAULT_RARE_BLOCK_CHANCE;

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
    public Long2ObjectMap<OreBlockPlacer> generate(WorldGenLevel level, RandomSource random, GTOreDefinition entry,
                                                   BlockPos origin) {
        Long2ObjectMap<OreBlockPlacer> generatedSections = new Long2ObjectOpenHashMap<>();

        int size = entry.clusterSize().sample(random);
        int radius = Mth.ceil(size / 2f);

        int sampleOffsetX = random.nextInt(16);
        int sampleOffsetY = random.nextInt(16);
        int sampleOffsetZ = random.nextInt(16);

        final NoiseSettings noiseSettings = DEFAULT_NOISE_SETTINGS.clampToHeightAccessor(level);

        int x0 = origin.getX() - radius, x1 = origin.getX() + radius;
        int z0 = origin.getZ() - radius, z1 = origin.getY() + radius;
        int y0 = Math.max(origin.getY() - radius, this.minYLevel);
        int y1 = Math.min(origin.getY() + radius, this.maxYLevel);
        final BoundingBox veinBounds = new BoundingBox(x0, y0, z0, z1, y1, z1);

        int sectionX0 = blockToSectionCoord(x0), sectionX1 = blockToSectionCoord(x1);
        int sectionY0 = blockToSectionCoord(y0), sectionY1 = blockToSectionCoord(y1);
        int sectionZ0 = blockToSectionCoord(z0), sectionZ1 = blockToSectionCoord(z1);
        SectionPos.betweenClosedStream(sectionX0, sectionY0, sectionZ0, sectionX1, sectionY1, sectionZ1)
                .forEach(pos -> {
                    int sectionRelativeMinY = veinBounds.minY() - pos.minBlockY();
                    if (sectionRelativeMinY > SECTION_MAX_INDEX) {
                        // bottom Y is above this section, skip
                        // I don't think this can actually happen, but better safe than sorry
                        if (ConfigHolder.INSTANCE.dev.debugWorldgen) {
                            LOGGER.warn("Tried to place part of veined vein '{}' in section {} when start Y is above section (section top: {}, vein start: {})!",
                                    GTRegistries.ORE_VEINS.getKey(entry), pos, pos.maxBlockY(), veinBounds.minY());
                        }
                        return;
                    }
                    int sectionRelativeMaxY = veinBounds.maxY() - pos.maxBlockY();
                    if (sectionRelativeMaxY < 0) {
                        // top Y is below this section, skip
                        // I don't think this can actually happen, but better safe than sorry
                        if (ConfigHolder.INSTANCE.dev.debugWorldgen) {
                            LOGGER.warn("Tried to place part of veined vein '{}' in section {} when end Y is below section (section bottom: {}, vein end: {})!",
                                    GTRegistries.ORE_VEINS.getKey(entry), pos, pos.minBlockY(), veinBounds.maxY());
                        }
                        return;
                    }
                    int cellHeight = noiseSettings.getCellHeight();
                    final int sectionRelativeMinCellY = Mth.floorDiv(sectionRelativeMinY, cellHeight);
                    final int sectionRelativeCellCountY = Mth.floorDiv(sectionRelativeMaxY, cellHeight) - sectionRelativeMinCellY;


                    final long randomSeed = random.nextLong(); // Fully deterministic regardless of chunk order
                    generatedSections.put(pos.asLong(), (worldGenLevel, access, section, sectionPos) -> {
                        placeSection(worldGenLevel, access, section, entry, randomSeed, sectionPos, veinBounds,
                                noiseSettings, sectionRelativeMinCellY, sectionRelativeCellCountY,
                                sampleOffsetX, sampleOffsetY, sampleOffsetZ);
                    });
                });
        return generatedSections;
    }

    private void placeSection(WorldGenLevel level, BulkSectionAccess access, LevelChunkSection section,
                              GTOreDefinition entry, long randomSeed, SectionPos sectionPos, BoundingBox veinBounds,
                              NoiseSettings noiseSettings, int minYCell, int yCells,
                              int sampleOffsetX, int sampleOffsetY, int sampleOffsetZ) {
        RandomSource random = new XoroshiroRandomSource(randomSeed);

        final TrimmedNoiseChunk noiseChunk = TrimmedNoiseChunk.forChunk(level, noiseSettings,
                sectionPos.x(), sectionPos.z(), sampleOffsetX, sampleOffsetY, sampleOffsetZ);

        Registry<DensityFunction> densityFunctions = level.registryAccess()
                .registryOrThrow(Registries.DENSITY_FUNCTION);
        DensityFunction veinToggle = noiseChunk.wrap(densityFunctions.get(GTDensityFunctions.NEW_ORE_VEIN_TOGGLE));
        DensityFunction veinMask = noiseChunk.wrap(densityFunctions.get(GTDensityFunctions.NEW_ORE_VEIN_MASK));

        int sectionMinX = sectionPos.minBlockX();
        int sectionMinY = sectionPos.minBlockY();
        int sectionMinZ = sectionPos.minBlockZ();

        noiseChunk.initializeForFirstCellX();
        int cellWidth = noiseChunk.cellWidth();
        int cellHeight = noiseChunk.cellHeight();
        int xCells = SECTION_SIZE / cellWidth;
        int zCells = SECTION_SIZE / cellWidth;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int cellX = 0; cellX < xCells; ++cellX) {
            // X and Z are always within the section, so we don't need to check them
            noiseChunk.advanceCellX(cellX);
            for (int cellZ = 0; cellZ < zCells; ++cellZ) {
                for (int cellY = 0; cellY < yCells; ++cellY) {
                    noiseChunk.selectCellYZ(cellY, cellZ);

                    for (int inCellY = 0; inCellY < cellHeight; ++inCellY) {
                        // section relative Y
                        int sectionY = (minYCell + cellY) * cellHeight + inCellY;
                        if (sectionY != sectionRelative(sectionY)) {
                            printBlockPlacementWarning(entry, "veined", sectionPos, Direction.Axis.Y, sectionY);
                            continue;
                        }
                        int blockY = sectionMinY + sectionY;
                        if (blockY < veinBounds.minY() || blockY > veinBounds.maxY()) continue;

                        noiseChunk.updateForY(inCellY, inCellY / (double) cellHeight);

                        int distanceFromYEdge = Math.min(
                                // Distance to top
                                blockY - veinBounds.minY(),
                                // Distance from bottom
                                veinBounds.maxY() - blockY);

                        for (int inCellX = 0; inCellX < cellWidth; ++inCellX) {
                            // section relative X
                            int sectionX = cellX * cellWidth + inCellX;
                            if (sectionX != sectionRelative(sectionX)) {
                                printBlockPlacementWarning(entry, "veined", sectionPos, Direction.Axis.X, sectionX);
                                continue;
                            }
                            int blockX = sectionMinX + sectionX;
                            if (blockX < veinBounds.minX() || blockX > veinBounds.maxX()) continue;

                            noiseChunk.updateForX(inCellX, inCellX / (double) cellWidth);

                            int distanceFromXEdge = Math.min(
                                    // Distance from west edge
                                    blockX - veinBounds.minX(),
                                    // Distance from east edge
                                    veinBounds.maxX() - blockX);

                            for (int inCellZ = 0; inCellZ < cellWidth; ++inCellZ) {
                                // section relative Z
                                int sectionZ = cellZ * cellWidth + inCellZ;
                                if (sectionZ != sectionRelative(sectionZ)) {
                                    printBlockPlacementWarning(entry, "veined", sectionPos, Direction.Axis.Z, sectionZ);
                                    continue;
                                }
                                int blockZ = sectionMinZ + sectionZ;
                                if (blockZ < veinBounds.minZ() || blockZ > veinBounds.maxZ()) continue;

                                noiseChunk.updateForZ(inCellZ, inCellZ / (double) cellWidth);

                                int distanceFromZEdge = Math.min(
                                        // Distance from north edge
                                        blockZ - veinBounds.minZ(),
                                        // Distance from south edge
                                        veinBounds.maxZ() - blockZ);

                                mutable.set(blockX, blockY, blockZ);
                                placeSingleBlock(access, section, entry, random,
                                        sectionX, sectionY, sectionZ, mutable,
                                        noiseChunk, veinToggle, veinMask,
                                        GTMath.min(distanceFromXEdge, distanceFromYEdge, distanceFromZEdge));
                            }
                        }
                    }
                }
            }

            noiseChunk.swapSlices();
        }

        noiseChunk.stopInterpolation();
    }

    protected void placeSingleBlock(BulkSectionAccess access, LevelChunkSection section, GTOreDefinition entry,
                                    RandomSource random, int sectionX, int sectionY, int sectionZ, BlockPos worldPos,
                                    DensityFunction.FunctionContext functionContext,
                                    DensityFunction veinToggle, DensityFunction veinMask,
                                    int distanceFromNearestEdge) {
        if (entry.density() < 1.0f && random.nextFloat() > entry.density()) return;
        if (veinMask.compute(functionContext) >= 0.0) return;

        float veininess = Math.abs((float) veinToggle.compute(functionContext));

        float edgeRoundoff = Mth.clampedMap(distanceFromNearestEdge, 0.0f, edgeRoundoffBegin, -maxEdgeRoundoff, 0.0f);
        if ((veininess - veininessThreshold + edgeRoundoff) < 0.0) return;

        float richness;
        if (minRichness >= 1.0f && maxRichness >= 1.0f) {
            // don't bother with filler blocks if min and max richness are both 1
            richness = 1.0f;
        } else {
            richness = Mth.clampedMap(veininess, veininessThreshold, maxRichnessThreshold,
                    minRichness, maxRichness);
        }

        if (richness > 0.0f && (richness >= 1.0f || random.nextFloat() < richness)) {
            if (!rareBlocks.isEmpty() && rareBlockChance > 0.0f && random.nextFloat() < rareBlockChance) {
                VeinBlockDefinition ore = GTUtil.getRandomItem(random, rareBlocks);
                if (ore == null) return;
                OreVeinUtil.placeOreBlock(ore.block, access, section, random, entry, worldPos);
            } else {
                VeinBlockDefinition ore = GTUtil.getRandomItem(random, oreBlocks);
                if (ore == null) return;
                OreVeinUtil.placeOreBlock(ore.block, access, section, random, entry, worldPos);
            }
        } else {
            if (fillerBlock == null || fillerBlock.isAir()) return;
            BlockState current = section.getBlockState(sectionX, sectionY, sectionZ);
            if (!OreVeinUtil.canPlaceOre(current, access::getBlockState, random, entry, worldPos)) return;

            section.setBlockState(sectionX, sectionY, sectionZ, fillerBlock, false);
        }
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

        // spotless:off
        public static final Codec<VeinBlockDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BLOCK_ENTRY_CODEC.fieldOf("block").forGetter(x -> x.block),
                Codec.INT.fieldOf("weight").forGetter(x -> x.weight)
        ).apply(instance, VeinBlockDefinition::new));
        // spotless:on

        public VeinBlockDefinition(Material block, int weight) {
            this(Either.right(block), weight);
        }

        public VeinBlockDefinition(List<TargetBlockState> block, int weight) {
            this(Either.left(block), weight);
        }
    }

    private static class TrimmedNoiseChunk implements DensityFunction.ContextProvider, DensityFunction.FunctionContext {

        private final RandomState randomState;
        private final int sampleOffsetX, sampleOffsetY, sampleOffsetZ;

        @Getter
        private final int cellWidth, cellHeight;
        private final int cellCountXZ, cellCountY;
        private final int cellNoiseMinY;
        private final int firstCellX, firstCellZ;
        private final int firstNoiseX, firstNoiseZ;
        private final int noiseSizeXZ;

        private boolean interpolating;
        private boolean fillingCell;
        private int cellStartBlockX, cellStartBlockY, cellStartBlockZ;
        private int inCellX, inCellY, inCellZ;

        private final List<NoiseInterpolator> interpolators;
        private final List<CacheAllInCell> cellCaches;
        private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();

        private long interpolationCounter;
        private long arrayInterpolationCounter;
        private int arrayIndex;

        private final DensityFunction.ContextProvider sliceFillingContextProvider = new DensityFunction.ContextProvider() {

            public DensityFunction.FunctionContext forIndex(int arrayIndex) {
                cellStartBlockY = (arrayIndex + cellNoiseMinY) * cellHeight;
                interpolationCounter++;
                inCellY = 0;
                TrimmedNoiseChunk.this.arrayIndex = arrayIndex;
                return TrimmedNoiseChunk.this;
            }

            public void fillAllDirectly(double[] values, DensityFunction function) {
                for (int cellY = 0; cellY < cellCountY + 1; ++cellY) {
                    cellStartBlockY = (cellY + cellNoiseMinY) * cellHeight;
                    interpolationCounter++;
                    inCellY = 0;
                    TrimmedNoiseChunk.this.arrayIndex = cellY;
                    values[cellY] = function.compute(TrimmedNoiseChunk.this);
                }
            }
        };

        public static TrimmedNoiseChunk forChunk(ServerLevelAccessor level, NoiseSettings noiseSettings,
                                                 int chunkX, int chunkZ,
                                                 int sampleOffsetX, int sampleOffsetY, int sampleOffsetZ) {
            int cellCountXZ = 16 / noiseSettings.getCellWidth();
            return new TrimmedNoiseChunk(noiseSettings, level.getLevel().getChunkSource().randomState(),
                    cellCountXZ, sectionToBlockCoord(chunkX), sectionToBlockCoord(chunkZ),
                    sampleOffsetX, sampleOffsetY, sampleOffsetZ);
        }

        public TrimmedNoiseChunk(NoiseSettings noiseSettings, RandomState randomState,
                                 int cellCountXZ, int firstNoiseX, int firstNoiseZ,
                                 int sampleOffsetX, int sampleOffsetY, int sampleOffsetZ) {
            this.randomState = randomState;
            this.sampleOffsetX = sampleOffsetX;
            this.sampleOffsetY = sampleOffsetY;
            this.sampleOffsetZ = sampleOffsetZ;

            this.cellWidth = noiseSettings.getCellWidth();
            this.cellHeight = noiseSettings.getCellHeight();
            this.cellCountXZ = cellCountXZ;
            this.cellCountY = Mth.floorDiv(noiseSettings.height(), this.cellHeight);
            this.cellNoiseMinY = Mth.floorDiv(noiseSettings.minY(), this.cellHeight);
            this.firstCellX = Math.floorDiv(firstNoiseX, this.cellWidth);
            this.firstCellZ = Math.floorDiv(firstNoiseZ, this.cellWidth);
            this.firstNoiseX = QuartPos.fromBlock(firstNoiseX);
            this.firstNoiseZ = QuartPos.fromBlock(firstNoiseZ);
            this.noiseSizeXZ = QuartPos.fromBlock(cellCountXZ * this.cellWidth);

            this.interpolators = Lists.newArrayList();
            this.cellCaches = Lists.newArrayList();
        }

        @Override
        public int blockX() {
            return this.cellStartBlockX + this.inCellX + this.sampleOffsetX;
        }

        @Override
        public int blockY() {
            return this.cellStartBlockY + this.inCellY + this.sampleOffsetY;
        }

        @Override
        public int blockZ() {
            return this.cellStartBlockZ + this.inCellZ + this.sampleOffsetZ;
        }

        private void fillSlice(boolean isSlice0, int startX) {
            this.cellStartBlockX = startX * this.cellWidth;
            this.inCellX = 0;

            for (int i = 0; i < this.cellCountXZ + 1; ++i) {
                int cellZ = this.firstCellZ + i;
                this.cellStartBlockZ = cellZ * this.cellWidth;
                this.inCellZ = 0;
                ++this.arrayInterpolationCounter;

                for (NoiseInterpolator interpolator : this.interpolators) {
                    double[] slice = (isSlice0 ? interpolator.slice0 : interpolator.slice1)[i];
                    interpolator.fillArray(slice, this.sliceFillingContextProvider);
                }
            }

            ++this.arrayInterpolationCounter;
        }

        public void initializeForFirstCellX() {
            if (this.interpolating) {
                throw new IllegalStateException("Staring interpolation twice");
            } else {
                this.interpolating = true;
                this.interpolationCounter = 0L;
                this.fillSlice(true, this.firstCellX);
            }
        }

        public void advanceCellX(int increment) {
            this.fillSlice(false, this.firstCellX + increment + 1);
            this.cellStartBlockX = (this.firstCellX + increment) * this.cellWidth;
        }

        @Override
        public TrimmedNoiseChunk forIndex(int arrayIndex) {
            int z = Math.floorMod(arrayIndex, this.cellWidth);
            int tmp = Math.floorDiv(arrayIndex, this.cellWidth);
            int x = Math.floorMod(tmp, this.cellWidth);
            int y = this.cellHeight - 1 - Math.floorDiv(tmp, this.cellWidth);
            this.inCellX = x;
            this.inCellY = y;
            this.inCellZ = z;
            this.arrayIndex = arrayIndex;
            return this;
        }

        @Override
        public void fillAllDirectly(double[] values, DensityFunction function) {
            this.arrayIndex = 0;

            for (int y = this.cellHeight - 1; y >= 0; --y) {
                this.inCellY = y;

                for (int x = 0; x < this.cellWidth; ++x) {
                    this.inCellX = x;

                    for (int z = 0; z < this.cellWidth; ++z) {
                        this.inCellZ = z;
                        values[this.arrayIndex++] = function.compute(this);
                    }
                }
            }
        }

        public void selectCellYZ(int y, int z) {
            this.interpolators.forEach(interpolator -> interpolator.selectCellYZ(y, z));
            this.fillingCell = true;
            this.cellStartBlockY = (y + this.cellNoiseMinY) * this.cellHeight;
            this.cellStartBlockZ = (this.firstCellZ + z) * this.cellWidth;
            ++this.arrayInterpolationCounter;

            for (CacheAllInCell cellCache : this.cellCaches) {
                cellCache.noiseFiller.fillArray(cellCache.values, this);
            }

            ++this.arrayInterpolationCounter;
            this.fillingCell = false;
        }

        public void updateForY(int inCellY, double y) {
            this.inCellY = inCellY;
            this.interpolators.forEach(interpolator -> interpolator.updateForY(y));
        }

        public void updateForX(int inCellX, double x) {
            this.inCellX = inCellX;
            this.interpolators.forEach(interpolator -> interpolator.updateForX(x));
        }

        public void updateForZ(int inCellZ, double z) {
            this.inCellZ = inCellZ;
            ++this.interpolationCounter;
            this.interpolators.forEach(interpolator -> interpolator.updateForZ(z));
        }

        public void stopInterpolation() {
            if (!this.interpolating) {
                throw new IllegalStateException("Stopping interpolation twice");
            } else {
                this.interpolating = false;
            }
        }

        public void swapSlices() {
            this.interpolators.forEach(NoiseInterpolator::swapSlices);
        }

        public DensityFunction wrap(DensityFunction densityFunction) {
            return this.wrapped.computeIfAbsent(densityFunction, this::wrapNew);
        }

        private DensityFunction wrapNew(DensityFunction densityFunction) {
            return densityFunction.mapAll(new DensityFunction.Visitor() {

                @Override
                public DensityFunction apply(DensityFunction densityFunction) {
                    if (densityFunction instanceof DensityFunctions.Marker marker) {
                        return switch (marker.type()) {
                            case Interpolated -> new NoiseInterpolator(marker.wrapped());
                            case FlatCache -> new FlatCache(marker.wrapped(), true);
                            case Cache2D -> new Cache2D(marker.wrapped());
                            case CacheOnce -> new CacheOnce(marker.wrapped());
                            case CacheAllInCell -> new CacheAllInCell(marker.wrapped());
                        };
                    } else if (densityFunction instanceof DensityFunctions.HolderHolder holderHolder) {
                        return holderHolder.function().value();
                    } else {
                        return densityFunction;
                    }
                }

                @Override
                public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder) {
                    var holder = noiseHolder.noiseData();
                    var noise = randomState.getOrCreateNoise(holder.unwrapKey().orElseThrow());
                    return new DensityFunction.NoiseHolder(holder, noise);
                }
            });
        }

        static class Cache2D implements DensityFunctions.MarkerOrMarked, NoiseChunkDensityFunction {

            private final DensityFunction function;
            private long lastPos2D = ChunkPos.INVALID_CHUNK_POS;
            private double lastValue;

            Cache2D(DensityFunction function) {
                this.function = function;
            }

            @Override
            public double compute(DensityFunction.FunctionContext context) {
                int x = context.blockX();
                int z = context.blockZ();
                long packed = ChunkPos.asLong(x, z);
                if (this.lastPos2D == packed) {
                    return this.lastValue;
                } else {
                    this.lastPos2D = packed;
                    double value = this.function.compute(context);
                    this.lastValue = value;
                    return value;
                }
            }

            @Override
            public void fillArray(double[] array, DensityFunction.ContextProvider contextProvider) {
                this.function.fillArray(array, contextProvider);
            }

            @Override
            public DensityFunction wrapped() {
                return this.function;
            }

            @Override
            public DensityFunctions.Marker.Type type() {
                return DensityFunctions.Marker.Type.Cache2D;
            }
        }

        private class CacheAllInCell implements DensityFunctions.MarkerOrMarked, NoiseChunkDensityFunction {

            final DensityFunction noiseFiller;
            final double[] values;

            private CacheAllInCell(DensityFunction noiseFilter) {
                this.noiseFiller = noiseFilter;
                this.values = new double[cellWidth * cellWidth * cellHeight];
                cellCaches.add(this);
            }

            @Override
            public double compute(DensityFunction.FunctionContext context) {
                if (context != TrimmedNoiseChunk.this) {
                    return this.noiseFiller.compute(context);
                } else if (!interpolating) {
                    throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
                } else {
                    int x = inCellX;
                    int y = inCellY;
                    int z = inCellZ;
                    if (x >= 0 && y >= 0 && z >= 0 && x < cellWidth && y < cellHeight && z < cellWidth) {
                        return this.values[((cellHeight - 1 - y) * cellWidth + x) * cellWidth + z];
                    } else {
                        return this.noiseFiller.compute(context);
                    }
                }
            }

            @Override
            public void fillArray(double[] array, DensityFunction.ContextProvider contextProvider) {
                contextProvider.fillAllDirectly(array, this);
            }

            @Override
            public DensityFunction wrapped() {
                return this.noiseFiller;
            }

            @Override
            public DensityFunctions.Marker.Type type() {
                return DensityFunctions.Marker.Type.CacheAllInCell;
            }
        }

        class CacheOnce implements DensityFunctions.MarkerOrMarked, NoiseChunkDensityFunction {

            private final DensityFunction function;
            private long lastCounter;
            private long lastArrayCounter;
            private double lastValue;
            @Nullable
            private double[] lastArray;

            CacheOnce(DensityFunction function) {
                this.function = function;
            }

            @Override
            public double compute(DensityFunction.FunctionContext context) {
                if (context != TrimmedNoiseChunk.this) {
                    return this.function.compute(context);
                } else if (this.lastArray != null && this.lastArrayCounter == arrayInterpolationCounter) {
                    return this.lastArray[arrayIndex];
                } else if (this.lastCounter == interpolationCounter) {
                    return this.lastValue;
                } else {
                    this.lastCounter = interpolationCounter;
                    double value = this.function.compute(context);
                    this.lastValue = value;
                    return value;
                }
            }

            @Override
            public void fillArray(double[] array, DensityFunction.ContextProvider contextProvider) {
                if (this.lastArray != null && this.lastArrayCounter == arrayInterpolationCounter) {
                    System.arraycopy(this.lastArray, 0, array, 0, array.length);
                } else {
                    this.wrapped().fillArray(array, contextProvider);
                    if (this.lastArray != null && this.lastArray.length == array.length) {
                        System.arraycopy(array, 0, this.lastArray, 0, array.length);
                    } else {
                        this.lastArray = array.clone();
                    }

                    this.lastArrayCounter = arrayInterpolationCounter;
                }
            }

            @Override
            public DensityFunction wrapped() {
                return this.function;
            }

            @Override
            public DensityFunctions.Marker.Type type() {
                return DensityFunctions.Marker.Type.CacheOnce;
            }
        }

        class FlatCache implements DensityFunctions.MarkerOrMarked, NoiseChunkDensityFunction {

            private final DensityFunction noiseFiller;
            final double[][] values;

            FlatCache(DensityFunction noiseFiller, boolean computeValues) {
                this.noiseFiller = noiseFiller;

                this.values = new double[noiseSizeXZ + 1][noiseSizeXZ + 1];
                if (computeValues) {
                    for (int xIdx = 0; xIdx <= noiseSizeXZ; ++xIdx) {
                        int quartX = firstNoiseX + xIdx;
                        int blockX = QuartPos.toBlock(quartX);

                        for (int zIdx = 0; zIdx <= noiseSizeXZ; ++zIdx) {
                            int quartZ = firstNoiseZ + zIdx;
                            int blockZ = QuartPos.toBlock(quartZ);
                            this.values[xIdx][zIdx] = noiseFiller
                                    .compute(new DensityFunction.SinglePointContext(blockX, 0, blockZ));
                        }
                    }
                }
            }

            @Override
            public double compute(DensityFunction.FunctionContext context) {
                int quartX = QuartPos.fromBlock(context.blockX());
                int quartZ = QuartPos.fromBlock(context.blockZ());
                int xIdx = quartX - firstNoiseX;
                int zIdx = quartZ - firstNoiseZ;
                int max = this.values.length;
                return xIdx >= 0 && zIdx >= 0 && xIdx < max && zIdx < max ?
                        this.values[xIdx][zIdx] :
                        this.noiseFiller.compute(context);
            }

            @Override
            public void fillArray(double[] array, DensityFunction.ContextProvider contextProvider) {
                contextProvider.fillAllDirectly(array, this);
            }

            @Override
            public DensityFunction wrapped() {
                return this.noiseFiller;
            }

            @Override
            public DensityFunctions.Marker.Type type() {
                return DensityFunctions.Marker.Type.FlatCache;
            }
        }

        interface NoiseChunkDensityFunction extends DensityFunction {

            DensityFunction wrapped();

            default double minValue() {
                return this.wrapped().minValue();
            }

            default double maxValue() {
                return this.wrapped().maxValue();
            }
        }

        public class NoiseInterpolator implements DensityFunctions.MarkerOrMarked, NoiseChunkDensityFunction {

            private double[][] slice0;
            private double[][] slice1;
            private final DensityFunction noiseFiller;

            // 1st number = slice; 2nd = Y offset; 3rd = Z offset
            private double noise000, noise001;
            private double noise100, noise101;
            private double noise010, noise011;
            private double noise110, noise111;
            // 1st number = slice; 2nd = Z offset
            private double valueXZ00, valueXZ01;
            private double valueXZ10, valueXZ11;
            // number = Z offset
            private double valueZ0, valueZ1;

            private double value;

            NoiseInterpolator(DensityFunction noiseFilter) {
                this.noiseFiller = noiseFilter;
                this.slice0 = this.allocateSlice(cellCountY, cellCountXZ);
                this.slice1 = this.allocateSlice(cellCountY, cellCountXZ);
                interpolators.add(this);
            }

            private double[][] allocateSlice(int cellCountY, int cellCountXZ) {
                int xzMax = cellCountXZ + 1;
                int yMax = cellCountY + 1;

                double[][] slice = new double[xzMax][yMax];
                for (int xz = 0; xz < xzMax; ++xz) {
                    slice[xz] = new double[yMax];
                }

                return slice;
            }

            private void selectCellYZ(int y, int z) {
                this.noise000 = this.slice0[z][y];
                this.noise001 = this.slice0[z + 1][y];
                this.noise100 = this.slice1[z][y];
                this.noise101 = this.slice1[z + 1][y];
                this.noise010 = this.slice0[z][y + 1];
                this.noise011 = this.slice0[z + 1][y + 1];
                this.noise110 = this.slice1[z][y + 1];
                this.noise111 = this.slice1[z + 1][y + 1];
            }

            private void updateForY(double y) {
                this.valueXZ00 = Mth.lerp(y, this.noise000, this.noise010);
                this.valueXZ10 = Mth.lerp(y, this.noise100, this.noise110);
                this.valueXZ01 = Mth.lerp(y, this.noise001, this.noise011);
                this.valueXZ11 = Mth.lerp(y, this.noise101, this.noise111);
            }

            private void updateForX(double x) {
                this.valueZ0 = Mth.lerp(x, this.valueXZ00, this.valueXZ10);
                this.valueZ1 = Mth.lerp(x, this.valueXZ01, this.valueXZ11);
            }

            private void updateForZ(double z) {
                this.value = Mth.lerp(z, this.valueZ0, this.valueZ1);
            }

            @Override
            public double compute(DensityFunction.FunctionContext context) {
                if (context != TrimmedNoiseChunk.this) {
                    return this.noiseFiller.compute(context);
                } else if (!interpolating) {
                    throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
                } else if (fillingCell) {
                    return Mth.lerp3((double) inCellX / (double) cellWidth,
                            (double) inCellY / (double) cellHeight,
                            (double) inCellZ / (double) cellWidth,
                            this.noise000, this.noise100,
                            this.noise010, this.noise110,
                            this.noise001, this.noise101,
                            this.noise011, this.noise111);
                } else {
                    return this.value;
                }
            }

            @Override
            public void fillArray(double[] array, DensityFunction.ContextProvider contextProvider) {
                if (fillingCell) {
                    contextProvider.fillAllDirectly(array, this);
                } else {
                    this.wrapped().fillArray(array, contextProvider);
                }
            }

            @Override
            public DensityFunction wrapped() {
                return this.noiseFiller;
            }

            private void swapSlices() {
                double[][] tmp = this.slice0;
                this.slice0 = this.slice1;
                this.slice1 = tmp;
            }

            @Override
            public DensityFunctions.Marker.Type type() {
                return DensityFunctions.Marker.Type.Interpolated;
            }
        }
    }
}
