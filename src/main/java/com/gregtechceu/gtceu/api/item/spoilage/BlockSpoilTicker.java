package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.common.data.GTAttachmentTypes;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.LevelAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.ticks.*;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Handles spoilage updates for block entities that aren't GT machines.<br>
 */
public class BlockSpoilTicker {

    private static final LevelAttached<BlockSpoilTicker> TICKERS = new LevelAttached<>(
            BlockSpoilTicker::new);

    public static final int MAX_TICKS_PER_UPDATE = 128;
    public static final long UPDATE_FREQUENCY = 10;
    public static final long INITIAL_TICK_DELAY = 5;

    private static final Set<Class<? extends Block>> BLACKLISTED_BLOCKS = ReferenceOpenHashSet
            .of(MetaMachineBlock.class);

    public static BlockSpoilTicker get(ServerLevel level) {
        return TICKERS.get(level);
    }

    private final ServerLevel level;
    @Getter
    private final LevelTicks<Block> blockTicks;

    private BlockSpoilTicker(LevelAccessor levelAccessor) {
        if (!(levelAccessor instanceof ServerLevel sLvl)) {
            throw new IllegalArgumentException("BlockSpoilTicker only works on servers!");
        }

        this.level = sLvl;
        this.blockTicks = new LevelTicks<>(this::isPositionTickingWithEntitiesLoaded, level.getProfilerSupplier());
    }

    public static void blacklistBlockClass(Class<? extends Block> clazz) {
        BLACKLISTED_BLOCKS.add(clazz);
    }

    private ScheduledTick<Block> createTick(Block block, BlockPos pos, long delay, TickPriority priority) {
        return new ScheduledTick<>(block, pos, level.getGameTime() + delay, priority, level.nextSubTickCount());
    }

    @ApiStatus.Internal
    public void scheduleInitialTick(Block block, BlockPos pos, long delay, TickPriority priority) {
        if (BLACKLISTED_BLOCKS.stream().anyMatch(v -> v.isAssignableFrom(block.getClass()))) return;

        if (this.blockTicks.hasScheduledTick(pos, block)) {
            return;
        }

        this.scheduleTick(block, pos, delay, priority);
    }

    @ApiStatus.Internal
    public void scheduleTick(Block block, BlockPos pos, long delay, TickPriority priority) {
        this.blockTicks.schedule(this.createTick(block, pos, delay, priority));
    }

    // region event listeners

    @ApiStatus.Internal
    public void tick() {
        long gameTime = this.level.getGameTime();
        this.blockTicks.tick(gameTime, MAX_TICKS_PER_UPDATE, (pos, block) -> {

            IItemHandlerModifiable handler;
            boolean anyHandlerPresent = false;

            for (Direction side : GTUtil.DIRECTIONS) {
                handler = resolveHandler(level, pos, side);
                if (handler != null) {
                    anyHandlerPresent = true;
                    SpoilUtils.updateBlockItemHandler(handler, level, pos);
                }
            }

            handler = resolveHandler(level, pos, null);
            if (handler != null) {
                anyHandlerPresent = true;
                SpoilUtils.updateBlockItemHandler(handler, level, pos);
            }

            if (!anyHandlerPresent) return;

            // reschedule another update
            this.scheduleTick(block, pos, UPDATE_FREQUENCY, TickPriority.NORMAL);
        });
    }

    private @Nullable IItemHandlerModifiable resolveHandler(Level level, BlockPos pos, @Nullable Direction side) {
        var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        return cap instanceof IItemHandlerModifiable m ? m : null;
    }

    @ApiStatus.Internal
    public void chunkLoaded(LevelChunk chunk) {
        this.blockTicks.addContainer(chunk.getPos(), chunk.getData(GTAttachmentTypes.SPOILAGE_CHUNK_TICKS));
    }

    @ApiStatus.Internal
    public void chunkUnloaded(LevelChunk chunk) {
        this.blockTicks.removeContainer(chunk.getPos());
    }

    private boolean isPositionTickingWithEntitiesLoaded(long chunkPos) {
        return level.areEntitiesLoaded(chunkPos) && level.getChunkSource().isPositionTicking(chunkPos);
    }

    public static class ChunkTickSerialiser implements IAttachmentSerializer<ListTag, LevelChunkTicks<Block>> {

        public static final ChunkTickSerialiser INSTANCE = new ChunkTickSerialiser();

        @Override
        public LevelChunkTicks<Block> read(IAttachmentHolder holder, ListTag tag, HolderLookup.Provider provider) {
            if (!(holder instanceof LevelChunk chunk)) return new LevelChunkTicks<>();
            return LevelChunkTicks.load(tag, v -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(v)), chunk.getPos());
        }

        @Override
        public @Nullable ListTag write(LevelChunkTicks<Block> attachment, HolderLookup.Provider provider) {
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return null;
            return attachment.save(server.overworld().getGameTime(), v -> BuiltInRegistries.BLOCK.getKey(v).toString());
        }
    }
}
