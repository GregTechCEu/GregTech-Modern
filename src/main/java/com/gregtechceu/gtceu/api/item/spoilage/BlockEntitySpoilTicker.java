package com.gregtechceu.gtceu.api.item.spoilage;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.ticks.*;

import lombok.Getter;

import java.util.Set;

/**
 * Handles spoilage updates for block entities that aren't GT machines.<br>
 * Only ticks MC containers ({@link BaseContainerBlockEntity}) by default, other BE classes can be added to the BLOCK_ENTITY_CLASSES_TO_TICK set to tick them.
 */
public class BlockEntitySpoilTicker extends SavedData {

    public static int MAX_TICKS_PER_UPDATE = ServerLevel.MAX_SCHEDULED_TICKS_PER_TICK;
    public static long UPDATE_FREQUENCY = 2;

    public static final Set<Class<? extends BlockEntity>> BLOCK_ENTITY_CLASSES_TO_TICK = ObjectOpenHashSet.of(
            BaseContainerBlockEntity.class
    );

    public static BlockEntitySpoilTicker get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent((tag) -> new BlockEntitySpoilTicker(level),
                () -> new BlockEntitySpoilTicker(level), "gtceu:block_entity_spoil_ticker");
    }

    private final ServerLevel level;
    @Getter
    private final LevelTicks<BlockEntityType<?>> blockEntityTicks;

    private BlockEntitySpoilTicker(LevelAccessor levelAccessor) {
        if (!(levelAccessor instanceof ServerLevel sLvl)) {
            throw new IllegalArgumentException("SpoilageContainerTracker only works on servers!");
        }

        this.level = sLvl;
        this.blockEntityTicks = new LevelTicks<>(this::isPositionTickingWithEntitiesLoaded, level.getProfilerSupplier());
    }

    private <T> ScheduledTick<T> createTick(BlockPos pos, T type, long delay, TickPriority priority) {
        return new ScheduledTick<>(type, pos, level.getLevelData().getGameTime() + delay, priority,
                level.nextSubTickCount());
    }

    public void scheduleTick(BlockPos pos, BlockEntityType<?> type, long delay, TickPriority priority) {
        this.blockEntityTicks.schedule(this.createTick(pos, type, delay, priority));
    }

    public void scheduleTick(BlockEntity blockEntity, long delay, TickPriority priority) {
        scheduleTick(blockEntity.getBlockPos(), blockEntity.getType(), delay, priority);
    }

    // region event listeners

    public void tick() {
        long gameTime = this.level.getGameTime();
        this.blockEntityTicks.tick(gameTime, MAX_TICKS_PER_UPDATE, (pos, blockEntityType) -> {
            BlockEntity blockEntity = this.level.getBlockEntity(pos);

            if (blockEntity == null || blockEntity.isRemoved() || blockEntity.getType() != blockEntityType) {
                // if the block entity no longer exists, exit without doing anything (including rescheduling)
                return;
            }

            if (!BLOCK_ENTITY_CLASSES_TO_TICK.contains(blockEntity.getClass())) return;

            SpoilUtils.updateBlock(blockEntity);

            // reschedule another update for the next tick
            this.scheduleTick(pos, blockEntityType, UPDATE_FREQUENCY, TickPriority.NORMAL);
        });
    }

    public void chunkLoaded(LevelChunk chunk) {
        // we won't bother with saving these because it's easier and safer to just recreate them
        this.blockEntityTicks.addContainer(chunk.getPos(), new LevelChunkTicks<>());
    }

    public void chunkUnloaded(LevelChunk chunk) {
        this.blockEntityTicks.removeContainer(chunk.getPos());
    }

    private boolean isPositionTickingWithEntitiesLoaded(long chunkPos) {
        return level.areEntitiesLoaded(chunkPos) && level.getChunkSource().isPositionTicking(chunkPos);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        return compoundTag;
    }
}