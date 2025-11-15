package com.gregtechceu.gtceu.client.mui.schemarenderer;

import com.gregtechceu.gtceu.api.mui.schema.ISchema;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelTimeAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RenderLevel implements LevelTimeAccess {

    private final ISchema schema;
    private final Level level;

    private final Thread thread;

    public RenderLevel(ISchema schema) {
        this.schema = schema;
        this.level = schema.getLevel();

        this.thread = Thread.currentThread();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        if (!this.schema.getRenderFilter().test(pos, state)) {
            return null;
        }
        // avoid the level
        if (Thread.currentThread() != this.thread) {
            int chunkX = SectionPos.blockToSectionCoord(pos.getX());
            int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
            var chunk = this.level.getChunkForCollisions(chunkX, chunkZ);
            if (chunk == null) {
                return null;
            }
            return chunk.getBlockEntity(pos);
        }
        return this.level.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        if (!this.schema.getRenderFilter().test(pos, state)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        if (!this.schema.getRenderFilter().test(pos, state)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return this.level.getFluidState(pos);
    }

    @Override
    public @Nullable ChunkAccess getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return level.getChunk(x, z, requiredStatus, nonnull);
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ) {
        return level.hasChunk(chunkX, chunkZ);
    }

    @Override
    public int getHeight(Heightmap.Types heightmapType, int x, int z) {
        return level.getHeight(heightmapType, x, z);
    }

    @Override
    public int getSkyDarken() {
        return level.getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return level.getBiomeManager();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return level.getUncachedNoiseBiome(x, y, z);
    }

    @Override
    public boolean isClientSide() {
        return level.isClientSide();
    }

    @Override
    public int getSeaLevel() {
        return level.getSeaLevel();
    }

    @Override
    public DimensionType dimensionType() {
        return level.dimensionType();
    }

    @Override
    public RegistryAccess registryAccess() {
        return level.registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return level.enabledFeatures();
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return level.getShade(direction, shade);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return level.getLightEngine();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return level.getWorldBorder();
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB collisionBox) {
        return level.getEntityCollisions(entity, collisionBox);
    }

    @Override
    public long dayTime() {
        return level.dayTime();
    }
}
