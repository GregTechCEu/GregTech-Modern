package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.lowdragmc.lowdraglib.Platform;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DummyPlacementContext extends PlacementContext {
    private final Level world;
    private final long seed;

    public DummyPlacementContext(Level world, long seed, GTOreDefinition definition) {
        super(null, new FlatLevelSource(new FlatLevelGeneratorSettings(Optional.empty(), definition.getBiomes().get().get(0), List.of())), Optional.empty());
        this.world = world;
        this.seed = seed;
    }

    public int getHeight(Heightmap.Types heightmap, int x, int z) {
        return this.world.getHeight(heightmap, x, z);
    }

    public CarvingMask getCarvingMask(ChunkPos pos, GenerationStep.Carving carver) {
        ProtoChunk chunk = new ProtoChunk(pos, UpgradeData.EMPTY, world, world.registryAccess().registryOrThrow(Registries.BIOME), null);
        return chunk.getOrCreateCarvingMask(carver);
    }

    public BlockState getBlockState(BlockPos pos) {
        return this.world.getBlockState(pos);
    }

    public int getMinBuildHeight() {
        return this.world.getMinBuildHeight();
    }

    public WorldGenLevel getLevel() {
        return new WorldGenLevel() {
            @Override
            public long getSeed() {
                return DummyPlacementContext.this.seed;
            }

            @Override
            public ServerLevel getLevel() {
                return null;
            }

            @Override
            public long nextSubTickCount() {
                return 0;
            }

            @Override
            public LevelTickAccess<Block> getBlockTicks() {
                return world.getBlockTicks();
            }

            @Override
            public LevelTickAccess<Fluid> getFluidTicks() {
                return world.getFluidTicks();
            }

            @Override
            public LevelData getLevelData() {
                return world.getLevelData();
            }

            @Override
            public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
                return new DifficultyInstance(world.getDifficulty(), world.getGameTime(), 0, 0);
            }

            @Nullable
            @Override
            public MinecraftServer getServer() {
                return Platform.getMinecraftServer();
            }

            @Override
            public ChunkSource getChunkSource() {
                return world.getChunkSource();
            }

            @Override
            public RandomSource getRandom() {
                return world.getRandom();
            }

            @Override
            public void playSound(@Nullable Player player, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {

            }

            @Override
            public void addParticle(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {

            }

            @Override
            public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {

            }

            @Override
            public void gameEvent(GameEvent event, Vec3 position, GameEvent.Context context) {

            }

            @Override
            public float getShade(Direction direction, boolean shade) {
                return 0;
            }

            @Override
            public LevelLightEngine getLightEngine() {
                return world.getLightEngine();
            }

            @Override
            public WorldBorder getWorldBorder() {
                return world.getWorldBorder();
            }

            @Nullable
            @Override
            public BlockEntity getBlockEntity(BlockPos pos) {
                return world.getBlockEntity(pos);
            }

            @Override
            public BlockState getBlockState(BlockPos pos) {
                return world.getBlockState(pos);
            }

            @Override
            public FluidState getFluidState(BlockPos pos) {
                return world.getFluidState(pos);
            }

            @Override
            public List<Entity> getEntities(@Nullable Entity entity, AABB area, Predicate<? super Entity> predicate) {
                return world.getEntities(entity, area, predicate);
            }

            @Override
            public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB bounds, Predicate<? super T> predicate) {
                return world.getEntities(entityTypeTest, bounds, predicate);
            }

            @Override
            public List<? extends Player> players() {
                return world.players();
            }

            @Nullable
            @Override
            public ChunkAccess getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
                return world.getChunk(x, z, requiredStatus, nonnull);
            }

            @Override
            public int getHeight(Heightmap.Types heightmapType, int x, int z) {
                return world.getMaxBuildHeight();
            }

            @Override
            public int getSkyDarken() {
                return world.getSkyDarken();
            }

            @Override
            public BiomeManager getBiomeManager() {
                return world.getBiomeManager();
            }

            @Override
            public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
                return world.getUncachedNoiseBiome(x, y, z);
            }

            @Override
            public boolean isClientSide() {
                return world.isClientSide;
            }

            @Override
            public int getSeaLevel() {
                return world.getSeaLevel();
            }

            @Override
            public DimensionType dimensionType() {
                return world.dimensionType();
            }

            @Override
            public RegistryAccess registryAccess() {
                return world.registryAccess();
            }

            @Override
            public FeatureFlagSet enabledFeatures() {
                return world.enabledFeatures();
            }

            @Override
            public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> state) {
                return world.isStateAtPosition(pos, state);
            }

            @Override
            public boolean isFluidAtPosition(BlockPos pos, Predicate<FluidState> predicate) {
                return world.isFluidAtPosition(pos, predicate);
            }

            @Override
            public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
                return world.setBlock(pos, state, flags, recursionLeft);
            }

            @Override
            public boolean removeBlock(BlockPos pos, boolean isMoving) {
                return world.removeBlock(pos, isMoving);
            }

            @Override
            public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
                return world.destroyBlock(pos, dropBlock, entity, recursionLeft);
            }
        };
    }
}
