package com.gregtechceu.gtceu.utils.fakelevel;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@MethodsReturnNonnullByDefault
public class RenderLevel implements LevelReader {

    private final ISchema schema;
    private final Level level;

    public RenderLevel(ISchema schema) {
        this.schema = schema;
        this.level = schema.getLevel();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(@NotNull BlockPos pos) {
        if (this.schema == null) return this.level.getBlockEntity(pos);
        BlockInfo.Mutable.SHARED.set(this.level, pos);
        return this.schema.getRenderFilter().test(pos, BlockInfo.Mutable.SHARED) ?
                BlockInfo.Mutable.SHARED.getBlockEntity() : null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (this.schema == null) return this.level.getBlockState(pos);
        BlockInfo.Mutable.SHARED.set(this.level, pos);
        return this.schema.getRenderFilter().test(pos, BlockInfo.Mutable.SHARED) ?
                BlockInfo.Mutable.SHARED.getBlockState() : Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        if (this.schema == null) return this.level.getFluidState(pos);
        BlockInfo.Mutable.SHARED.set(this.level, pos);
        return this.schema.getRenderFilter().test(pos, BlockInfo.Mutable.SHARED) ?
                BlockInfo.Mutable.SHARED.getBlockState().getFluidState() : Fluids.EMPTY.defaultFluidState();
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
}
