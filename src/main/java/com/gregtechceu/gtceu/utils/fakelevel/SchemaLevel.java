package com.gregtechceu.gtceu.utils.fakelevel;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.schema.ISchema;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.BlockPosUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SchemaLevel extends Level implements ISchema {

    private static final ResourceKey<Level> LEVEL_ID = ResourceKey.create(Registries.DIMENSION,
            GTCEu.id("fake_level"));

    private final TransientEntitySectionManager<Entity> entityStorage = new TransientEntitySectionManager<>(
            Entity.class, new EntityCallbacks());

    private final LongSet filledBlocks = new LongOpenHashSet();
    @Getter
    @Setter
    private BiPredicate<BlockPos, BlockState> renderFilter = (pos, state) -> true;
    /**
     * Sections for which we prepared lighting.
     */
    private final LongSet litSections = new LongOpenHashSet();
    private final BlockPos.MutableBlockPos min = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos max = new BlockPos.MutableBlockPos();

    @Getter
    private final Scoreboard scoreboard = new Scoreboard();
    @Getter
    private final ChunkSource chunkSource = new DummyChunkSource(this);
    private final Holder<Biome> biome;
    private final DataLayer defaultDataLayer;

    public static final SchemaLevel INSTANCE = new SchemaLevel();

    public SchemaLevel() {
        this(GTRegistries.builtinRegistry());
    }

    public SchemaLevel(RegistryAccess registryAccess) {
        super(
                createLevelData(),
                LEVEL_ID,
                registryAccess,
                registryAccess.registryOrThrow(Registries.DIMENSION_TYPE)
                        .getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
                () -> InactiveProfiler.INSTANCE,
                true,
                false,
                0,
                1000000);
        this.biome = registryAccess.registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);

        // the argument named "size" is actually the default value.
        // We want all blocks to have max light, so we create a pre-filled light data layer
        defaultDataLayer = new DataLayer(LightEngine.MAX_LEVEL);
    }

    private static ClientLevel.ClientLevelData createLevelData() {
        var levelData = new ClientLevel.ClientLevelData(Difficulty.NORMAL, false, false);
        // set time of day to noon (from TimeCommand noon)
        levelData.setDayTime(6000);
        return levelData;
    }

    /**
     * Ensures lighting is set to skylight level 15 in the entire chunk and adjacent chunks whenever a block is first
     * changed in that chunk.
     */
    protected void prepareLighting(BlockPos pos) {
        ChunkPos minChunk = new ChunkPos(pos.offset(-1, -1, -1));
        ChunkPos maxChunk = new ChunkPos(pos.offset(1, 1, 1));
        ChunkPos.rangeClosed(minChunk, maxChunk).forEach(chunkPos -> {
            if (litSections.add(chunkPos.toLong())) {
                LevelLightEngine lightEngine = getLightEngine();
                for (int i = 0; i < getSectionsCount(); ++i) {
                    int y = getSectionYFromSectionIndex(i);
                    SectionPos sectionPos = SectionPos.of(chunkPos, y);
                    lightEngine.updateSectionStatus(sectionPos, false);
                    lightEngine.queueSectionData(LightLayer.BLOCK, sectionPos, defaultDataLayer);
                    lightEngine.queueSectionData(LightLayer.SKY, sectionPos, defaultDataLayer);
                }

                lightEngine.setLightEnabled(chunkPos, true);
                lightEngine.propagateLightSources(chunkPos);
                lightEngine.retainData(chunkPos, false);
            }
        });
    }

    public boolean hasFilledBlocks() {
        return !filledBlocks.isEmpty();
    }

    public boolean isFilledBlock(BlockPos blockPos) {
        return filledBlocks.contains(blockPos.asLong());
    }

    /**
     * Do NOT store/cache the returned stream's elements for later! They're all a <strong>single</strong>
     * {@link BlockPos.MutableBlockPos MutableBlockPos} instance that WILL change every iteration.
     * 
     * @return stream of all non-air blocks in this {@link SchemaLevel} instance
     */
    public Stream<BlockPos> getFilledBlocks() {
        var mutablePos = new BlockPos.MutableBlockPos();
        return filledBlocks.longStream()
                .sequential()
                .mapToObj(pos -> {
                    mutablePos.set(pos);
                    return mutablePos;
                });
    }

    @Override
    public @NotNull Iterator<Map.Entry<BlockPos, BlockState>> iterator() {
        return getFilledBlocks()
                .map(pos -> Map.entry(pos, this.getBlockState(pos)))
                .iterator();
    }

    protected void removeFilledBlock(BlockPos pos) {
        filledBlocks.remove(pos.asLong());
    }

    protected void addFilledBlock(BlockPos pos) {
        filledBlocks.add(pos.asLong());
    }

    @Override
    public Level getLevel() {
        return this;
    }

    @Override
    public Vec3 getFocus() {
        return BlockPosUtil.getCenterD(this.min, this.max);
    }

    @Override
    public BlockPos getOrigin() {
        return min;
    }

    @Override
    public boolean isLoaded(BlockPos pos) {
        int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        return chunkSource.hasChunk(chunkX, chunkZ);
    }

    @Override
    public @Nullable Entity getEntity(int id) {
        return getEntities().get(id);
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return entityStorage.getEntityGetter();
    }

    @Override
    public void playSeededSound(@Nullable Player player, double x, double y, double z, Holder<SoundEvent> sound,
                                SoundSource source, float volume, float pitch, long seed) {}

    @Override
    public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> sound, SoundSource category,
                                float volume, float pitch, long seed) {}

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {}

    @Override
    public void updateNeighborsAt(BlockPos pos, Block block) {}

    @Override
    public void updateNeighbourForOutputSignal(BlockPos pos, Block block) {}

    @Override
    public void markAndNotifyBlock(BlockPos pos, @Nullable LevelChunk chunk, BlockState setState,
                                   BlockState newState, int flags, int recursionLeft) {}

    @Override
    public String gatherChunkSourceStats() {
        return "";
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
            return ClientCallWrapper.getClientRecipeManager();
        } else {
            return GTCEu.getMinecraftServer().getRecipeManager();
        }
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
    public float getShade(@NotNull Direction direction, boolean shade) {
        if (!shade) {
            return 1.0f;
        } else {
            return switch (direction) {
                case DOWN -> 0.5f;
                case UP -> 1.0f;
                case NORTH, SOUTH -> 0.8f;
                case WEST, EAST -> 0.6f;
            };
        }
    }

    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return biome;
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return FeatureFlags.VANILLA_SET;
    }

    private static class ClientCallWrapper {

        private static RecipeManager getClientRecipeManager() {
            return Minecraft.getInstance().level.getRecipeManager();
        }
    }

    private static class EntityCallbacks implements LevelCallback<Entity> {

        @Override
        public void onCreated(Entity entity) {}

        @Override
        public void onDestroyed(Entity entity) {}

        @Override
        public void onTickingStart(Entity entity) {}

        @Override
        public void onTickingEnd(Entity entity) {}

        @Override
        public void onTrackingStart(Entity entity) {}

        @Override
        public void onTrackingEnd(Entity entity) {}

        @Override
        public void onSectionChange(Entity object) {}
    }
}
