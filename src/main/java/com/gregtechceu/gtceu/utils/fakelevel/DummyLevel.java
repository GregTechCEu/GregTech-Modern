package com.gregtechceu.gtceu.utils.fakelevel;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@NotNullByDefault
public class DummyLevel extends Level {

    @Getter
    protected DummyChunkSource chunkSource = new DummyChunkSource(this);
    protected final LevelLightEngine lighter;
    @Getter
    private final Scoreboard scoreboard = new Scoreboard();

    public static final DummyLevel INSTANCE = new DummyLevel();

    public DummyLevel() {
        super(new DummyLevelData(), Level.OVERWORLD, GTRegistries.builtinRegistry(),
                GTRegistries.builtinRegistry().registryOrThrow(Registries.DIMENSION_TYPE)
                        .getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
                () -> InactiveProfiler.INSTANCE, false, false, 0, 1000000);
        this.lighter = new LevelLightEngine(chunkSource, true, false);
    }

    @Override
    public boolean isLoaded(BlockPos pos) {
        int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        return chunkSource.hasChunk(chunkX, chunkZ);
    }

    @Override
    public void neighborChanged(BlockPos pos, Block block, BlockPos fromPos) {
        // NOOP - do not trigger forge events
    }

    @Override
    public void updateNeighborsAt(BlockPos pos, Block block) {
        // NOOP - do not trigger forge events
    }

    @Override
    public void updateNeighborsAtExceptFromFacing(BlockPos pos, Block blockType, Direction skipSide) {
        // NOOP - do not trigger forge events
    }

    @Override
    public void updateNeighbourForOutputSignal(BlockPos pos, Block block) {
        // NOOP - do not trigger forge events
    }

    @Override
    public void markAndNotifyBlock(BlockPos p_46605_, @Nullable LevelChunk levelchunk, BlockState blockstate,
                                   BlockState p_46606_, int p_46607_, int p_46608_) {
        // NOOP - do not trigger forge events
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        // NOOP - do not trigger forge events
    }

    @Override
    public void playSeededSound(@Nullable Player player, double x, double y, double z, Holder<SoundEvent> sound,
                                SoundSource source, float volume, float pitch, long seed) {}

    @Override
    public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> sound, SoundSource category,
                                float volume, float pitch, long seed) {}

    @Override
    public String gatherChunkSourceStats() {
        return "";
    }

    @Override
    public @Nullable Entity getEntity(int id) {
        return null;
    }

    @Override
    public @Nullable MapItemSavedData getMapData(String mapName) {
        return null;
    }

    @Override
    public void setMapData(String mapName, MapItemSavedData data) {}

    @Override
    public int getFreeMapId() {
        return 0;
    }

    @Override
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {}

    @Override
    public RecipeManager getRecipeManager() {
        if (GTCEu.isClientThread()) {
            return ClientAccess.getClientRecipeManager();
        } else {
            return GTCEu.getMinecraftServer().getRecipeManager();
        }
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return new LevelEntityGetter<>() {

            public @Nullable Entity get(int id) {
                return null;
            }

            public @Nullable Entity get(UUID uuid) {
                return null;
            }

            public Iterable<Entity> getAll() {
                return Collections.emptyList();
            }

            public <U extends Entity> void get(EntityTypeTest<Entity, U> test,
                                               AbortableIterationConsumer<U> consumer) {}

            public void get(AABB boundingBox, Consumer<Entity> consumer) {}

            public <U extends Entity> void get(EntityTypeTest<Entity, U> test, AABB bounds,
                                               AbortableIterationConsumer<U> consumer) {}
        };
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {}

    @Override
    public void gameEvent(GameEvent event, Vec3 position, GameEvent.Context context) {}

    @Override
    public float getShade(Direction direction, boolean shade) {
        return 0;
    }

    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return this.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return FeatureFlags.VANILLA_SET;
    }

    private static final class ClientAccess {

        private static RecipeManager getClientRecipeManager() {
            return Minecraft.getInstance().getConnection().getRecipeManager();
        }
    }
}
