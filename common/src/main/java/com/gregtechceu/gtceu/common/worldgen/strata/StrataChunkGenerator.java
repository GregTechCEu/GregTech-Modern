package com.gregtechceu.gtceu.common.worldgen.strata;

import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.strata.IStrataLayer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public class StrataChunkGenerator extends ChunkGenerator {
    public static final Codec<StrataChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(IStrataLayer.CODEC).fieldOf("layers").forGetter(it -> it.strataLayers),
            ChunkGenerator.CODEC.fieldOf("base_generator").forGetter(it -> it.baseGenerator)
    ).apply(instance, StrataChunkGenerator::new));

    public List<IStrataLayer> strataLayers;
    public ChunkGenerator baseGenerator;

    private BlockState defaultBlock = Blocks.STONE.defaultBlockState();

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
        return baseGenerator.fillFromNoise(executor, blender, random, structureManager, chunk).thenApply(chunkAccess -> this.stratify(executor, blender, random, structureManager, chunkAccess));
    }

    public ChunkAccess stratify(Executor executor, Blender blender, RandomState random, StructureManager structureManager, ChunkAccess chunk) {
        NormalNoise noise = random.getOrCreateNoise(Noises.EROSION_LARGE);
        Heightmap heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        int minY = -64;
        int minCellY = Mth.intFloorDiv(minY, QuartPos.toBlock(2));
        int cellCountY = Mth.intFloorDiv(384, QuartPos.toBlock(2));
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        NoiseChunk noiseChunk = createNoiseChunk(chunk, structureManager, blender, random);
        ChunkPos chunkPos = chunk.getPos();
        int minBlockX = chunkPos.getMinBlockX();
        int minBlockZ = chunkPos.getMinBlockZ();
        int cellWidth = noiseChunk.cellWidth();
        int cellHeight = noiseChunk.cellHeight();
        int blockWidth = 16 / cellWidth;

        for (int x = 0; x < blockWidth; ++x) {
            noiseChunk.advanceCellX(x);
            for (int z = 0; z < blockWidth; ++z) {
                LevelChunkSection levelChunkSection = chunk.getSection(chunk.getSectionsCount() - 1);
                for (int q = cellCountY - 1; q >= 0; --q) {
                    noiseChunk.selectCellYZ(q, z);
                    for (int r = cellHeight - 1; r >= 0; --r) {
                        int s = (minCellY + q) * cellHeight + r;
                        int yPosFinal = s & 0xF;
                        int u = chunk.getSectionIndex(s);
                        if (chunk.getSectionIndex(levelChunkSection.bottomBlockY()) != u) {
                            levelChunkSection = chunk.getSection(u);
                        }
                        double d = (double) r / (double) cellHeight;

                        noiseChunk.updateForY(s, d);
                        for (int v = 0; v < cellWidth; ++v) {
                            int w = minBlockX + x * cellWidth + v;
                            int xPosFinal = w & 0xF;
                            double e = (double) v / (double) cellWidth;
                            noiseChunk.updateForX(w, e);
                            for (int aa = 0; aa < cellWidth; ++aa) {
                                int idk1 = minBlockZ + z * cellWidth + aa;
                                int zPosFinal = idk1 & 0xF;
                                double f = (double) aa / (double) cellWidth;
                                noiseChunk.updateForZ(idk1, f);
                                BlockState current = levelChunkSection.getBlockState(xPosFinal, yPosFinal, zPosFinal);

                                List<IStrataLayer> candidates = WorldGeneratorUtils.STRATA_LAYER_BLOCK_MAP.getOrDefault(current, new ArrayList<>());
                                if (candidates.isEmpty()) {
                                    continue;
                                }

                                IStrataLayer strata;
                                if (candidates.size() == 1) {
                                    strata = candidates.get(0);
                                } else {
                                    final int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, xPosFinal, zPosFinal);
                                    strata = getStateForPos(noise, candidates, xPosFinal, yPosFinal, zPosFinal, surfaceY);
                                }
                                final BlockState toSet = strata.getState().get().get();

                                if (toSet == current || toSet.isAir()) continue;

                                if (toSet.isAir() || SharedConstants.debugVoidTerrain(chunk.getPos()))
                                    continue;
                                if (toSet.getLightEmission() != 0 && chunk instanceof ProtoChunk) {
                                    mutableBlockPos.set(w, s, idk1);
                                    ((ProtoChunk) chunk).addLight(mutableBlockPos);
                                }
                                levelChunkSection.setBlockState(xPosFinal, yPosFinal, zPosFinal, toSet, false);
                                heightmap.update(xPosFinal, s, zPosFinal, toSet);
                                heightmap2.update(xPosFinal, s, zPosFinal, toSet);
                                mutableBlockPos.set(w, s, idk1);
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
        return NoiseChunk.forChunk(chunk, random, Beardifier.forStructuresInChunk(structureManager, chunk.getPos()), BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.LARGE_BIOMES), (x, y, z) -> new Aquifer.FluidStatus(128, Blocks.WATER.defaultBlockState()), blender);
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
