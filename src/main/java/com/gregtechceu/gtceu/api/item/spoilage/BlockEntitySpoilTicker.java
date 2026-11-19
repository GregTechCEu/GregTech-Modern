package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.LevelAttached;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.ticks.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

/**
 * Handles spoilage updates for block entities that aren't GT machines.<br>
 */
public class BlockEntitySpoilTicker {

    private static final LevelAttached<BlockEntitySpoilTicker> TICKERS = new LevelAttached<>(
            BlockEntitySpoilTicker::new);

    public static final int MAX_TICKS_PER_UPDATE = ServerLevel.MAX_SCHEDULED_TICKS_PER_TICK;
    public static final long UPDATE_FREQUENCY = 2;

    private static final Set<Class<? extends BlockEntity>> BLACKLISTED_BLOCK_ENTITIES = new ReferenceOpenHashSet<>();

    public static BlockEntitySpoilTicker get(ServerLevel level) {
        return TICKERS.get(level);
    }

    private final ServerLevel level;
    @Getter
    private final LevelTicks<BlockEntityType<?>> blockEntityTicks;

    private BlockEntitySpoilTicker(LevelAccessor levelAccessor) {
        if (!(levelAccessor instanceof ServerLevel sLvl)) {
            throw new IllegalArgumentException("BlockEntitySpoilTicker only works on servers!");
        }

        this.level = sLvl;
        this.blockEntityTicks = new LevelTicks<>(this::isPositionTickingWithEntitiesLoaded,
                level.getProfilerSupplier());
    }

    public static void blacklistBlockEntityClass(Class<? extends BlockEntity> clazz) {
        BLACKLISTED_BLOCK_ENTITIES.add(clazz);
    }

    private ScheduledTick<BlockEntityType<?>> createTick(BlockEntity be, long delay, TickPriority priority) {
        return new ScheduledTick<>(be.getType(), be.getBlockPos(), level.getLevelData().getGameTime() + delay, priority,
                level.nextSubTickCount());
    }

    @ApiStatus.Internal
    public void scheduleTickIfValid(BlockEntity be, long delay, TickPriority priority) {
        if (BLACKLISTED_BLOCK_ENTITIES.stream().anyMatch(v -> v.isAssignableFrom(be.getClass()))) return;

        if (!this.blockEntityTicks.allContainers.containsKey(ChunkPos.asLong(be.getBlockPos()))) {
            this.blockEntityTicks.addContainer(new ChunkPos(be.getBlockPos()), new LevelChunkTicks<>());
        }

        this.scheduleTick(be, delay, priority);
    }

    @ApiStatus.Internal
    public void scheduleTick(BlockEntity be, long delay, TickPriority priority) {
        this.blockEntityTicks.schedule(this.createTick(be, delay, priority));
    }

    // region event listeners

    @ApiStatus.Internal
    public void tick() {
        long gameTime = this.level.getGameTime();
        this.blockEntityTicks.tick(gameTime, MAX_TICKS_PER_UPDATE, (pos, blockEntityType) -> {
            BlockEntity blockEntity = this.level.getBlockEntity(pos);

            if (blockEntity == null || blockEntity.isRemoved() || blockEntity.getType() != blockEntityType ||
                    !hasModifiableItemHandler(blockEntity)) {
                // if the block entity no longer exists or doesn't have an item handler, exit without doing anything
                // (including rescheduling)
                return;
            }

            SpoilUtils.updateBlock(blockEntity);

            // reschedule another update
            this.scheduleTick(blockEntity, UPDATE_FREQUENCY, TickPriority.NORMAL);
        });
    }

    private boolean hasModifiableItemHandler(BlockEntity blockEntity) {
        LazyOptional<IItemHandler> value;
        for (Direction side : GTUtil.DIRECTIONS) {
            value = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side);
            if (value.isPresent()) {
                return value.resolve().map(v -> v instanceof IItemHandlerModifiable).orElse(false);
            }
        }
        value = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
        return value.isPresent() && value.resolve().map(v -> v instanceof IItemHandlerModifiable).orElse(false);
    }

    @ApiStatus.Internal
    public void chunkLoaded(LevelChunk chunk) {
        // we won't bother with saving these because it's easier and safer to just recreate them
        this.blockEntityTicks.addContainer(chunk.getPos(), new LevelChunkTicks<>());
    }

    @ApiStatus.Internal
    public void chunkUnloaded(LevelChunk chunk) {
        this.blockEntityTicks.removeContainer(chunk.getPos());
    }

    private boolean isPositionTickingWithEntitiesLoaded(long chunkPos) {
        return level.areEntitiesLoaded(chunkPos) && level.getChunkSource().isPositionTicking(chunkPos);
    }
}
