package com.gregtechceu.gtceu.common.worldgen.strata;

import com.google.common.collect.Sets;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.strata.IStrataLayer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Unused. might fix at some point in the future. makes a lava ocean for some reason.
 */
public class StrataChunkGenerator extends ChunkGenerator {
    public static final Codec<StrataChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(IStrataLayer.CODEC).fieldOf("layers").forGetter(it -> it.strataLayers),
            ChunkGenerator.CODEC.fieldOf("base_generator").forGetter(it -> it.baseGenerator)
    ).apply(instance, StrataChunkGenerator::new));

    public List<IStrataLayer> strataLayers;
    public ChunkGenerator baseGenerator;

    public StrataChunkGenerator(List<IStrataLayer> layers, ChunkGenerator baseGenerator) {
        super(baseGenerator.structureSets, Optional.empty(), baseGenerator.getBiomeSource());
        this.strataLayers = layers;
        this.baseGenerator = baseGenerator;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        baseGenerator.applyCarvers(level, seed, random, biomeManager, structureManager, chunk, step);
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
        baseGenerator.buildSurface(level, structureManager, random, chunk);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
        baseGenerator.spawnOriginalMobs(level);
    }

    @Override
    public int getGenDepth() {
        return baseGenerator.getGenDepth();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random, StructureManager structureManager, ChunkAccess chunk) {
        int minY = -64;
        int cellHeight = QuartPos.toBlock(2);
        int height = Mth.intFloorDiv(384, cellHeight);
        HashSet<LevelChunkSection> acquiredSections = Sets.newHashSet();
        return baseGenerator.fillFromNoise(executor, blender, random, structureManager, chunk).whenCompleteAsync((chunkAccess, throwable) -> {
            int maxIndex = chunk.getSectionIndex(height * cellHeight - 1 + minY);
            int minIndex = chunk.getSectionIndex(minY);
            for (int i = maxIndex; i >= minIndex; --i) {
                LevelChunkSection levelChunkSection = chunk.getSection(i);
                levelChunkSection.acquire();
                acquiredSections.add(levelChunkSection);
            }
        }, executor).thenApplyAsync(chunkAccess -> this.stratify(executor, blender, random, structureManager, chunkAccess), executor).whenCompleteAsync((chunkAccess, throwable) -> {
            for (LevelChunkSection levelChunkSection : acquiredSections) {
                levelChunkSection.release();
            }
        }, executor);
    }

    public ChunkAccess stratify(Executor executor, Blender blender, RandomState random, StructureManager structureManager, ChunkAccess chunk) {
        NormalNoise noise = random.getOrCreateNoise(Noises.EROSION_LARGE);
        Heightmap heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        int minY = -64;
        int minCellY = Mth.intFloorDiv(minY, QuartPos.toBlock(2));
        int cellCountY = Mth.intFloorDiv(384, QuartPos.toBlock(2));
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        NoiseChunk noiseChunk = chunk.getOrCreateNoiseChunk(chunkAccess -> this.createNoiseChunk(chunkAccess, structureManager, blender, random));
        ChunkPos chunkPos = chunk.getPos();
        int minBlockX = chunkPos.getMinBlockX();
        int minBlockZ = chunkPos.getMinBlockZ();
        int cellWidth = noiseChunk.cellWidth();
        int cellHeight = noiseChunk.cellHeight();
        int blockWidth = 16 / cellWidth;

        for (int blockX = 0; blockX < blockWidth; ++blockX) {
            noiseChunk.advanceCellX(blockX);
            for (int blockZ = 0; blockZ < blockWidth; ++blockZ) {
                LevelChunkSection levelChunkSection = chunk.getSection(chunk.getSectionsCount() - 1);
                for (int cellY = cellCountY - 1; cellY >= 0; --cellY) {
                    noiseChunk.selectCellYZ(cellY, blockZ);
                    for (int cellY2 = cellHeight - 1; cellY2 >= 0; --cellY2) {
                        int yPos = (minCellY + cellY) * cellHeight + cellY2;
                        int sectionY = yPos & 0xF;
                        int sectionIndex = chunk.getSectionIndex(yPos);
                        if (chunk.getSectionIndex(levelChunkSection.bottomBlockY()) != sectionIndex) {
                            levelChunkSection = chunk.getSection(sectionIndex);
                        }
                        double deltaY = (double) cellY2 / (double) cellHeight;

                        noiseChunk.updateForY(yPos, deltaY);
                        for (int cellX = 0; cellX < cellWidth; ++cellX) {
                            int xPos = minBlockX + blockX * cellWidth + cellX;
                            int sectionX = xPos & 0xF;
                            double deltaX = (double) cellX / (double) cellWidth;
                            noiseChunk.updateForX(xPos, deltaX);
                            for (int cellZ = 0; cellZ < cellWidth; ++cellZ) {
                                int zPos = minBlockZ + blockZ * cellWidth + cellZ;
                                int sectionZ = zPos & 0xF;
                                double deltaZ = (double) cellZ / (double) cellWidth;
                                noiseChunk.updateForZ(zPos, deltaZ);
                                BlockState current = levelChunkSection.getBlockState(sectionX, sectionY, sectionZ);

                                List<IStrataLayer> candidates = WorldGeneratorUtils.STRATA_LAYER_BLOCK_MAP.getOrDefault(current, new ArrayList<>());
                                if (candidates.isEmpty()) {
                                    continue;
                                }

                                IStrataLayer strata;
                                if (candidates.size() == 1) {
                                    strata = candidates.get(0);
                                } else {
                                    final int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, sectionX, sectionZ);
                                    strata = getStateForPos(noise, candidates, sectionX, yPos, sectionZ, surfaceY);
                                }
                                final BlockState toSet = strata.getState().get().get();

                                if (toSet == current || toSet.isAir()) continue;

                                if (toSet.isAir() || SharedConstants.debugVoidTerrain(chunk.getPos()))
                                    continue;
                                if (toSet.getLightEmission() != 0 && chunk instanceof ProtoChunk) {
                                    mutableBlockPos.set(xPos, yPos, zPos);
                                    ((ProtoChunk) chunk).addLight(mutableBlockPos);
                                }
                                levelChunkSection.setBlockState(sectionX, sectionY, sectionZ, toSet, false);
                                heightmap.update(sectionX, yPos, sectionZ, toSet);
                                heightmap2.update(sectionX, yPos, sectionZ, toSet);
                                mutableBlockPos.set(xPos, yPos, zPos);
                                chunk.markPosForPostprocessing(mutableBlockPos);
                            }
                        }
                    }
                }
            }
        }
        return chunk;
    }

    private NoiseChunk createNoiseChunk(ChunkAccess chunk, StructureManager structureManager, Blender blender, RandomState random) {
        return NoiseChunk.forChunk(chunk, random, Beardifier.forStructuresInChunk(structureManager, chunk.getPos()), BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.OVERWORLD), (x, y, z) -> new Aquifer.FluidStatus(128, Blocks.WATER.defaultBlockState()), blender);
    }

    /**
     *
     * @param candidates the candidates to select from
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @param surfaceY the y value of the world surface
     * @return the selected BlockState to place
     */
    @Nonnull
    private static IStrataLayer getStateForPos(NormalNoise noise, @Nonnull List<IStrataLayer> candidates, int x, int y, int z, int surfaceY) {
        // need abs() for x and y, when generating blobs between - and + coords
        double noiseValue = noise.getValue(Math.abs(x * 0.01F), y * 1.0F / surfaceY, Math.abs(z * 0.01F));
        return candidates.get((int) (candidates.size() * noiseValue));
    }

    @Override
    public int getSeaLevel() {
        return baseGenerator.getSeaLevel();
    }

    @Override
    public int getMinY() {
        return baseGenerator.getMinY();
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return baseGenerator.getBaseHeight(x, z, type, level, random);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        return baseGenerator.getBaseColumn(x, z, height, random);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        baseGenerator.addDebugScreenInfo(info, random, pos);
    }
}
