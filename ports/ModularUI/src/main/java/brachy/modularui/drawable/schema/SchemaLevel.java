package brachy.modularui.drawable.schema;

import brachy.modularui.ModularUI;
import brachy.modularui.utils.BlockPosUtil;
import brachy.modularui.utils.RegistryAccessContainer;
import brachy.modularui.utils.sides.SidedAccessHelper;

import org.jspecify.annotations.NullMarked;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.block.state.BlockState;
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
import net.minecraft.world.level.saveddata.maps.MapId;
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
import org.joml.Vector3fc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@NullMarked
public class SchemaLevel extends Level implements ISchema {

    private static final ResourceKey<Level> LEVEL_ID = ResourceKey.create(Registries.DIMENSION,
            ModularUI.id("fake_level"));

    private final TransientEntitySectionManager<Entity> entityStorage = new TransientEntitySectionManager<>(
            Entity.class, new EntityCallbacks());

    private final LongSet filledBlocks = new LongOpenHashSet();

    /**
     * Sections for which we prepared lighting.
     */
    private final LongSet litSections = new LongOpenHashSet();
    private final BlockPos.MutableBlockPos min = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos max = new BlockPos.MutableBlockPos();

    private final TickRateManager tickRateManager = new TickRateManager();
    @Getter
    private final WorldBorder worldBorder = new WorldBorder();
    @Getter
    private final Scoreboard scoreboard = new Scoreboard();
    @Getter
    private final DummyChunkSource chunkSource = new DummyChunkSource(this);
    private final Holder<Biome> biome;
    private final DataLayer defaultDataLayer;
    private final EnvironmentAttributeSystem environmentAttributes;
    private final ClockManager clocks = definition -> 6000L;

    public static final SchemaLevel INSTANCE = new SchemaLevel();

    public SchemaLevel() {
        this(RegistryAccessContainer.current());
    }

    public SchemaLevel(RegistryAccess registryAccess) {
        super(
                createLevelData(),
                LEVEL_ID,
                registryAccess,
                registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE)
                        .getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                true,
                false,
                0,
                1000000);
        this.biome = registryAccess.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        this.environmentAttributes = EnvironmentAttributeSystem.builder().addDefaultLayers(this).build();

        // the argument named "size" is actually the default value.
        // We want all blocks to have max light, so we create a pre-filled light data layer
        defaultDataLayer = new DataLayer(LightEngine.MAX_LEVEL);
    }

    @Override
    public int getSeaLevel() {
        // Preview dimensions use the Overworld definition and its standard sea level.
        return 63;
    }

    private static ClientLevel.ClientLevelData createLevelData() {
        var levelData = new ClientLevel.ClientLevelData(Difficulty.NORMAL, false, false);
        // The preview's clockManager keeps its environment at noon.
        return levelData;
    }

    /**
     * Ensures lighting is set to skylight level 15 in the entire chunk and adjacent chunks whenever a block is first
     * changed in that chunk.
     */
    protected void prepareLighting(BlockPos pos) {
        ChunkPos minChunk = ChunkPos.containing(pos.offset(-1, -1, -1));
        ChunkPos maxChunk = ChunkPos.containing(pos.offset(1, 1, 1));
        ChunkPos.rangeClosed(minChunk, maxChunk).forEach(chunkPos -> {
            if (litSections.add(chunkPos.pack())) {
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
    public Iterator<Map.Entry<BlockPos, BlockState>> iterator() {
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

    public Iterable<Entity> getAllEntities() {
        return this.getEntities().getAll();
    }

    public void addEntity(Entity entity) {
        // no event hook here tyvm
        // if (NeoForge.EVENT_BUS.post(new EntityJoinLevelEvent(entity, this)).isCanceled()) return;
        this.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
        this.entityStorage.addEntity(entity);
        entity.onAddedToLevel();
    }

    public void removeEntity(int entityId, Entity.RemovalReason reason) {
        Entity entity = this.getEntities().get(entityId);
        if (entity != null) {
            entity.setRemoved(reason);
            entity.onClientRemoval();
        }
    }

    @Override
    public Level getLevel() {
        return this;
    }

    @Override
    public Vector3fc getFocus() {
        return BlockPosUtil.getCenterF(this.min, this.max);
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
    public TickRateManager tickRateManager() {
        return this.tickRateManager;
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return entityStorage.getEntityGetter();
    }

    @Override
    public boolean addFreshEntity(Entity entity) {
        this.addEntity(entity);
        return true;
    }

    @Override
    public PotionBrewing potionBrewing() {
        return SidedAccessHelper.getPotionBrewing();
    }

    @Override
    public void playSeededSound(@Nullable Entity player, double x, double y, double z, Holder<SoundEvent> sound,
                                SoundSource source, float volume, float pitch, long seed) {}

    @Override
    public void playSeededSound(@Nullable Entity player, Entity entity, Holder<SoundEvent> sound, SoundSource category,
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
    public @Nullable MapItemSavedData getMapData(MapId mapId) {
        return null;
    }

    @Override
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {}

    @Override
    public RecipeAccess recipeAccess() {
        return SidedAccessHelper.getRecipeAccess();
    }

    @Override public FuelValues fuelValues() { return SidedAccessHelper.getFuelValues(); }
    @Override public ClockManager clockManager() { return clocks; }
    @Override public EnvironmentAttributeSystem environmentAttributes() { return environmentAttributes; }
    @Override public LevelData.RespawnData getRespawnData() { return levelData.getRespawnData(); }
    @Override public void setRespawnData(LevelData.RespawnData data) { levelData.setSpawn(data); }

    @Override
    public java.util.Collection<net.minecraft.world.entity.boss.enderdragon.EnderDragonPart> dragonParts() {
        var parts = new java.util.ArrayList<net.minecraft.world.entity.boss.enderdragon.EnderDragonPart>();
        for (Entity entity : getAllEntities()) {
            if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon) {
                java.util.Collections.addAll(parts, dragon.getSubEntities());
            }
        }
        return parts;
    }

    /** Like ClientLevel, this display-only level does not simulate explosions. */
    @Override
    public void explode(@Nullable Entity source, @Nullable net.minecraft.world.damagesource.DamageSource damage,
                        @Nullable net.minecraft.world.level.ExplosionDamageCalculator calculator,
                        double x, double y, double z, float radius, boolean fire, ExplosionInteraction interaction,
                        net.minecraft.core.particles.ParticleOptions smallParticles,
                        net.minecraft.core.particles.ParticleOptions largeParticles,
                        net.minecraft.util.random.WeightedList<net.minecraft.core.particles.ExplosionParticleInfo> particles,
                        Holder<SoundEvent> sound) {}

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public void levelEvent(@Nullable Entity player, int type, BlockPos pos, int data) {}

    @Override
    public void gameEvent(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context) {}

    public float getShade(Direction direction, boolean shade) {
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

    // Neo: Variable day time code

    @Getter
    @Setter
    private float dayTimeFraction = 0.0f;
    @Getter
    @Setter
    private float dayTimePerTick = -1.0f;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof SchemaLevel entries)) return false;

        return filledBlocks.equals(entries.filledBlocks) &&
                litSections.equals(entries.litSections) && min.equals(entries.min) && max.equals(entries.max) &&
                biome.equals(entries.biome) && defaultDataLayer.equals(entries.defaultDataLayer);
    }

    @Override
    public int hashCode() {
        int result = filledBlocks.hashCode();
        result = 31 * result + litSections.hashCode();
        result = 31 * result + min.hashCode();
        result = 31 * result + max.hashCode();
        result = 31 * result + biome.hashCode();
        result = 31 * result + defaultDataLayer.hashCode();
        return result;
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
