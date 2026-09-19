package brachy.modularui.drawable.schema;

import org.jspecify.annotations.NullMarked;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@NullMarked
public class RenderLevel implements LevelReader, BlockAndTintGetter {

    @Getter private final ISchema schema;
    private final Level level;
    private final RenderFilter renderFilter;
    private final Thread thread;

    public RenderLevel(ISchema schema, RenderFilter renderFilter) {
        this.schema = schema;
        this.level = schema.getLevel();
        this.renderFilter = renderFilter;

        this.thread = Thread.currentThread();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        if (!this.renderFilter.shouldRender(pos, state)) {
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
        if (!this.renderFilter.shouldRender(pos, state)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        if (!this.renderFilter.shouldRender(pos, state)) {
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
    public CardinalLighting cardinalLighting() {
        return level.dimensionType().cardinalLightType().get();
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        if (level instanceof BlockAndTintGetter tintGetter) return tintGetter.getBlockTint(pos, resolver);
        return resolver.getColor(level.getBiome(pos).value(), pos.getX(), pos.getZ());
    }

    @Override
    public EnvironmentAttributeReader environmentAttributes() {
        return level.environmentAttributes();
    }

    @Override
    public int getHeight() {
        return level.getHeight();
    }

    @Override
    public int getMinY() {
        return level.getMinY();
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

    public long dayTime() {
        return level.getOverworldClockTime();
    }
}
