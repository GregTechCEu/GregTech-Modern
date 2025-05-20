package com.gregtechceu.gtceu.api.pattern;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.pattern.error.PatternError;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.api.pattern.util.PatternMatchContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A modifiable data container mused for multiblock structure predicates.
 */

public class MultiblockState {

    public final static PatternError UNLOAD_ERROR = new PatternStringError("multiblocked.pattern.error.chunk");
    public final static PatternError UNINIT_ERROR = new PatternStringError("multiblocked.pattern.error.init");

    /** The <b>active</b> position */
    private BlockPos pos;
    /** The {@code BlockState} at {@code pos} */
    private BlockState blockState;
    /** The {@code BlockEntity} at {@code pos} */
    private BlockEntity tileEntity;
    private boolean tileEntityInitialized;

    @Getter
    private final PatternMatchContext matchContext;
    /**
     * A map counting the number of global matches for each SimplePredicate in this MultiblockState. Used for validating
     * Predicate count restrictions.
     * <br>
     * {@see SimplePredicate#testGlobal}
     */
    @Getter
    private Object2IntOpenHashMap<SimplePredicate> globalCount;
    /**
     * A map counting the number of matches for each SimplePredicate in the current layer. Used for validating Predicate
     * count restrictions.
     * <br>
     * {@see SimplePredicate#testLayer}
     */
    @Getter
    private Object2IntOpenHashMap<SimplePredicate> layerCount;
    /** The condition ({@link TraceabilityPredicate}) that must be matched at this position. */
    public TraceabilityPredicate predicate;
    /** The IO capability of the block at {@link MultiblockState#pos} */
    public IO io;
    /** If the check at {@link MultiblockState#pos} fails, this error explains why. */
    public PatternError error;
    /** Whether the check should assume the structure is flipped. */
    @Getter
    @Setter
    private boolean neededFlip = false;
    /** The {@code Level} the multi is in. */
    @Getter
    public final Level world;
    /** The controller's position */
    public final BlockPos controllerPos;

    /** Multiblock's controller. */
    public IMultiController lastController;

    /**
     * A set containing {@link BlockPos} converted to long
     * Allows for quickly checking if a specific position has been added before
     */
    // persist
    private LongOpenHashSet cache;

    /**
     * Instantiates a new {@link MultiblockState}. Each multi controller has one.
     * 
     * @param world         the {@link Level} (dimension + side) the multi is in.
     * @param controllerPos the position of the multi controller.
     */
    public MultiblockState(Level world, BlockPos controllerPos) {
        this.world = world;
        this.controllerPos = controllerPos;
        this.error = UNINIT_ERROR;
        this.matchContext = new PatternMatchContext();
    }

    protected void clean() {
        this.matchContext.reset();
        this.globalCount = new Object2IntOpenHashMap<>();
        this.layerCount = new Object2IntOpenHashMap<>();
        cache = new LongOpenHashSet();
    }

    /**
     * Updates the state for checking a new block. Clears obsolete data.
     * Also checks if the position is loaded. If unloaded, updates error.
     * 
     * @param posIn     the new position being checked
     * @param predicate the condition ({@link TraceabilityPredicate}) that must be matched at this position.
     * @return whether the block at the position is loaded.
     */
    protected boolean update(BlockPos posIn, TraceabilityPredicate predicate) {
        this.pos = posIn;
        this.blockState = null;
        this.tileEntity = null;
        this.tileEntityInitialized = false;
        this.predicate = predicate;
        this.error = null;
        if (!world.isLoaded(posIn)) {
            error = UNLOAD_ERROR;
            return false;
        }
        return true;
    }

    /**
     * {@return The {@link IMultiController} associated with this State}
     * {@code null} if not loaded
     */
    public IMultiController getController() {
        if (world.isLoaded(controllerPos)) {
            if (world.getBlockEntity(controllerPos) instanceof IMachineBlockEntity machineBlockEntity &&
                    machineBlockEntity.getMetaMachine() instanceof IMultiController controller) {
                return lastController = controller;
            }
        } else {
            error = UNLOAD_ERROR;
        }
        return null;
    }

    public boolean hasError() {
        return error != null;
    }

    public void setError(PatternError error) {
        this.error = error;
        if (error != null) {
            error.setWorldState(this);
        }
    }

    /**
     * {@return the BlockState at the active position}
     * {@code null} if the BlockState was unable to be gotten for some reason.
     */
    public BlockState getBlockState() {
        if (this.blockState == null) {
            this.blockState = this.world.getBlockState(this.pos);
        }
        if (this.blockState == null) {
            GTCEu.LOGGER.error("could not get BlockState at " + this.pos + " in MultiblockState");
        }
        return this.blockState;
    }

    /**
     * {@return the BlockEntity at the active position}
     * {@code null} if the BlockState is not a BlockEntity
     */
    @Nullable
    public BlockEntity getTileEntity() {
        if (!getBlockState().hasBlockEntity()) {
            return null;
        }
        if (this.tileEntity == null && !this.tileEntityInitialized) {
            this.tileEntity = this.world.getBlockEntity(this.pos);
            this.tileEntityInitialized = true;
        }

        return this.tileEntity;
    }

    /**
     * @return the active position
     */
    public BlockPos getPos() {
        return this.pos.immutable();
    }

    // TODO: Unused, review method's utility
    public BlockState getOffsetState(Direction face) {
        if (pos instanceof BlockPos.MutableBlockPos) {
            ((BlockPos.MutableBlockPos) pos).move(face);
            BlockState blockState = world.getBlockState(pos);
            ((BlockPos.MutableBlockPos) pos).move(face.getOpposite());
            return blockState;
        }
        return world.getBlockState(this.pos.relative(face));
    }

    /**
     * Adds given {@link BlockPos} to the cache
     * 
     * @param pos the position to add
     */
    public void addPosCache(BlockPos pos) {
        cache.add(pos.asLong());
    }

    /**
     * @param pos the BlockPos to check
     *            {@return {@code true} if the cache contains the given pos}
     */
    public boolean isPosInCache(BlockPos pos) {
        return cache.contains(pos.asLong());
    }

    /**
     * {@return a collection containing the cached BlockPos objects}
     */
    public Collection<BlockPos> getCache() {
        return cache.stream().map(BlockPos::of).collect(Collectors.toList());
    }

    /**
     * Notifies this MultiblockState that a BlockState has changed.
     * <br>
     * Rechecks the pattern as necessary and updates the structure as necessary.
     * 
     * @param pos   The {@link BlockPos} to check at
     * @param state The new {@link BlockState}
     */
    public void onBlockStateChanged(BlockPos pos, BlockState state) {
        if (world instanceof ServerLevel serverLevel) {
            if (pos.equals(controllerPos)) {
                if (lastController != null) {
                    if (!state.is(lastController.self().getBlockState().getBlock())) {
                        lastController.onStructureInvalid();
                        var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
                        mwsd.removeMapping(this);
                    }
                }
            } else {
                IMultiController controller = getController();
                if (controller != null) {
                    if (controller.isFormed() && state.getBlock() instanceof ActiveBlock) {
                        LongSet activeBlocks = getMatchContext().getOrDefault("vaBlocks", LongSets.emptySet());
                        if (activeBlocks.contains(pos.asLong())) {
                            // fine! it's caused by active blocks.
                            // speed up here!
                            return;
                        }
                    }
                    if (controller.checkPatternWithLock()) {
                        // refresh structure
                        controller.self().setFlipped(this.neededFlip);
                        controller.onStructureFormed();
                    } else {
                        // invalid structure
                        controller.self().setFlipped(false);
                        controller.onStructureInvalid();
                        var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
                        mwsd.removeMapping(this);
                        mwsd.addAsyncLogic(controller);
                    }
                }
            }
        }
    }
}
