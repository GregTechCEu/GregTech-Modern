package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.renderer.MultiblockInWorldPreviewRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockControllerMachine extends MetaMachine {

    private MultiblockState multiblockState;
    private final List<IMultiPart> parts = new ArrayList<>();
    private @Nullable ParallelHatchPartMachine parallelHatch = null;
    @Getter
    @SyncToClient
    private BlockPos[] partPositions = new BlockPos[0];

    /**
     * Whether Multiblock Formed.
     * <br>
     * NOTE: even machine is formed, it doesn't mean to workable!
     * Its parts maybe invalid due to chunk unload.
     */
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean isFormed;
    @Getter
    @SaveField
    @SyncToClient
    protected boolean isFlipped;

    public MultiblockControllerMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    //////////////////////////////////////
    // *** Multiblock Lifecycle ***//
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            MultiblockWorldSavedData.getOrCreate(serverLevel).addAsyncLogic(this);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (getLevel() instanceof ServerLevel serverLevel) {
            MultiblockWorldSavedData.getOrCreate(serverLevel).removeAsyncLogic(this);
        }
    }

    /**
     * Called when structure is formed, have to be called after {@link #checkPattern()}. (server-side / fake scene only)
     * <br>
     * Trigger points:
     * <br>
     * 1 - Blocks in structure changed but still formed.
     * <br>
     * 2 - Literally, structure formed.
     */
    public void onStructureFormed() {
        isFormed = true;
        syncDataHolder.markClientSyncFieldDirty("isFormed");
        MachineRenderState renderState = getRenderState();
        if (renderState.hasProperty(GTMachineModelProperties.IS_FORMED)) {
            setRenderState(renderState.setValue(GTMachineModelProperties.IS_FORMED, true));
        }

        this.parts.clear();
        Set<IMultiPart> set = getMultiblockState().getMatchContext().getOrCreate("parts", Collections::emptySet);
        for (IMultiPart part : set) {
            if (shouldAddPartToController(part)) {
                this.parts.add(part);
            }
        }
        this.parts.sort(getPartSorter());
        for (var part : parts) {
            if (part instanceof ParallelHatchPartMachine pHatch) {
                parallelHatch = pHatch;
            }
            part.addedToController(this);
        }
        updatePartPositions();
    }

    /**
     * Called when structure is invalid. (server-side / fake scene only)
     * <br>
     * Trigger points:
     * <br>
     * 1 - Blocks in structure changed.
     * <br>
     * 2 - Before controller machine removed.
     */
    public void onStructureInvalid() {
        isFormed = false;
        MachineRenderState renderState = getRenderState();
        if (renderState.hasProperty(GTMachineModelProperties.IS_FORMED)) {
            setRenderState(renderState.setValue(GTMachineModelProperties.IS_FORMED, false));
        }

        for (IMultiPart part : parts) {
            part.removedFromController(this);
        }
        parallelHatch = null;
        parts.clear();
        updatePartPositions();
    }

    /**
     * Called from part, when part is invalid due to chunk unload or broken.
     */
    public void onPartUnload() {
        parts.removeIf(part -> part.self().isRemoved());
        getMultiblockState().setError(MultiblockState.UNLOAD_ERROR);
        if (getLevel() instanceof ServerLevel serverLevel) {
            MultiblockWorldSavedData.getOrCreate(serverLevel).addAsyncLogic(this);
        }
        updatePartPositions();
    }

    //////////////////////////////////////
    // ***** Getters ******//
    /// ///////////////////////////////////

    @Override
    public MultiblockMachineDefinition getDefinition() {
        return (MultiblockMachineDefinition) super.getDefinition();
    }

    /**
     * Get MultiblockState. It records all structure-related information.
     */
    @NotNull
    public MultiblockState getMultiblockState() {
        if (multiblockState == null) {
            multiblockState = new MultiblockState(getLevel(), getBlockPos());
        }
        return multiblockState;
    }

    public @Nullable BlockState getPartAppearance(IMultiPart part, Direction side, BlockState sourceState,
                                                  BlockPos sourcePos) {
        if (isFormed()) {
            return getDefinition().getPartAppearance().apply(this, part, side);
        }
        return null;
    }

    public Comparator<IMultiPart> getPartSorter() {
        return getDefinition().getPartSorter().apply(this);
    }

    /**
     * Get all parts
     */
    public List<IMultiPart> getParts() {
        // for the client side, when the chunk unloaded
        if (parts.size() != this.partPositions.length) {
            parts.clear();
            for (var pos : this.partPositions) {
                if (getMachine(getLevel(), pos) instanceof IMultiPart part) {
                    parts.add(part);
                }
            }
        }
        return this.parts;
    }

    /**
     * The instance of {@link ParallelHatchPartMachine} attached to this Controller.
     * <p>
     * Note that this will return a singular instance, and will not account for multiple attached IParallelHatches
     *
     * @return an {@link Optional} of the attached IParallelHatch, empty if one is not attached
     */
    public Optional<ParallelHatchPartMachine> getParallelHatch() {
        return Optional.ofNullable(parallelHatch);
    }

    /**
     *
     * @return Whether batching is enabled on this multiblock
     */
    public boolean isBatchEnabled() {
        return false;
    }

    public void setFlipped(boolean flipped) {
        isFlipped = flipped;
        syncDataHolder.markClientSyncFieldDirty("isFlipped");
    }

    @SuppressWarnings("unused")
    @ClientFieldChangeListener(fieldName = "partPositions")
    protected void onPartsUpdated() {
        parts.clear();
        for (var pos : partPositions) {
            if (getMachine(getLevel(), pos) instanceof IMultiPart part) {
                parts.add(part);
            }
        }
    }

    protected void updatePartPositions() {
        this.partPositions = this.parts.isEmpty() ? new BlockPos[0] :
                this.parts.stream().map(part -> part.self().getBlockPos()).toArray(BlockPos[]::new);
        syncDataHolder.markClientSyncFieldDirty("partPositions");
    }

    public void setBatchEnabled(boolean batch) {}

    /**
     * should add part to the part list.
     */
    public boolean shouldAddPartToController(IMultiPart part) {
        return true;
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        if (oldFacing != newFacing && getLevel() instanceof ServerLevel serverLevel) {
            // invalid structure
            this.onStructureInvalid();
            var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
            mwsd.removeMapping(getMultiblockState());
            mwsd.addAsyncLogic(this);
        }
    }

    public boolean allowFlip() {
        return getDefinition().isAllowFlip();
    }

    @Override
    public void setUpwardsFacing(@NotNull Direction upwardsFacing) {
        if (!getDefinition().isAllowExtendedFacing()) {
            return;
        }
        if (upwardsFacing.getAxis() == Direction.Axis.Y) {
            GTCEu.LOGGER.error("Tried to set upwards facing to invalid facing {}! Skipping", upwardsFacing);
            return;
        }
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock &&
                blockState.getValue(GTBlockStateProperties.UPWARDS_FACING) != upwardsFacing) {
            getLevel().setBlockAndUpdate(getBlockPos(),
                    blockState.setValue(GTBlockStateProperties.UPWARDS_FACING, upwardsFacing));
            if (getLevel() != null && !getLevel().isClientSide) {
                notifyBlockUpdate();
                checkPattern();
            }
        }
    }

    @Override
    public void setFrontFacing(Direction facing) {
        super.setFrontFacing(facing);

        if (getLevel() != null && !getLevel().isClientSide) {
            checkPattern();
        }
    }

    /**
     * Show the preview of structure.
     */
    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (!isFormed() && player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            if (world.isClientSide()) {
                MultiblockInWorldPreviewRenderer.showPreview(pos, this,
                        ConfigHolder.INSTANCE.client.inWorldPreviewDuration * 20);
            }
            return InteractionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    public boolean allowCircuitSlots() {
        return true;
    }

    //////////////////////////////////////
    // *** Pattern checking ***//
    //////////////////////////////////////

    /**
     * Get structure pattern.
     * You can override it to create dynamic patterns.
     */
    public BlockPattern getPattern() {
        return getDefinition().getPatternFactory().get();
    }

    /**
     * Get lock for pattern checking.
     */
    @Getter
    private final Lock patternLock = new ReentrantLock();

    /**
     * Called in an async thread. It's unsafe, Don't modify anything of world but checking information.
     * It will be called per 5 tick.
     *
     * @param periodID period Tick
     */
    public void asyncCheckPattern(long periodID) {
        if ((getMultiblockState().hasError() || !isFormed) && (getOffset() + periodID) % 4 == 0 &&
                checkPatternWithTryLock()) { // per second
            if (getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().execute(() -> {
                    patternLock.lock();
                    if (checkPatternWithLock()) { // formed
                        setFlipped(getMultiblockState().isNeededFlip());
                        onStructureFormed();
                        var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
                        mwsd.addMapping(getMultiblockState());
                        mwsd.removeAsyncLogic(this);
                    }
                    patternLock.unlock();
                });
            }
        }
    }

    /**
     * Check MultiBlock Pattern. Just checking pattern without any other logic.
     * You can override it but it's unsafe for calling. because it will also be called in an async thread.
     * <br>
     * you should always use {@link MultiblockControllerMachine#checkPatternWithLock()} )} and
     * {@link MultiblockControllerMachine#checkPatternWithTryLock()} instead.
     *
     * @return whether it can be formed.
     */
    public boolean checkPattern() {
        BlockPattern pattern = getPattern();
        return pattern != null && pattern.checkPatternAt(getMultiblockState(), false);
    }

    /**
     * Check pattern with a lock.
     */
    public boolean checkPatternWithLock() {
        var lock = getPatternLock();
        lock.lock();
        try {
            return checkPattern();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Check pattern with a try lock
     *
     * @return false - checking failed or cant get the lock.
     */
    public boolean checkPatternWithTryLock() {
        var lock = getPatternLock();
        if (lock.tryLock()) {
            try {
                return checkPattern();
            } finally {
                lock.unlock();
            }
        } else {
            return false;
        }
    }
}
