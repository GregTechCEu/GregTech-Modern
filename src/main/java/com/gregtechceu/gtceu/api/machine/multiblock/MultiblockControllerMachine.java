package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.annotation.UpdateListener;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/3
 * @implNote MultiblockControllerMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockControllerMachine extends MetaMachine implements IMultiController {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MultiblockControllerMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);
    private MultiblockState multiblockState;
    private final List<IMultiPart> parts = new ArrayList<>();
    @Getter
    @DescSynced
    @UpdateListener(methodName = "onPartsUpdated")
    private BlockPos[] partPositions = new BlockPos[0];
    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    protected boolean isFormed;
    @Getter
    @Setter
    @Persisted
    @DescSynced
    protected boolean isFlipped;

    public MultiblockControllerMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public MultiblockMachineDefinition getDefinition() {
        return (MultiblockMachineDefinition) super.getDefinition();
    }

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

    @Override
    @NotNull
    public MultiblockState getMultiblockState() {
        if (multiblockState == null) {
            multiblockState = new MultiblockState(getLevel(), getPos());
        }
        return multiblockState;
    }

    @SuppressWarnings("unused")
    protected void onPartsUpdated(BlockPos[] newValue, BlockPos[] oldValue) {
        parts.clear();
        for (var pos : newValue) {
            if (getMachine(getLevel(), pos) instanceof IMultiPart part) {
                parts.add(part);
            }
        }
    }

    protected void updatePartPositions() {
        this.partPositions = this.parts.isEmpty() ? new BlockPos[0] :
                this.parts.stream().map(part -> part.self().getPos()).toArray(BlockPos[]::new);
    }

    @Override
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

    //////////////////////////////////////
    // *** Multiblock LifeCycle ***//
    //////////////////////////////////////
    @Getter
    private final Lock patternLock = new ReentrantLock();

    @Override
    public void asyncCheckPattern(long periodID) {
        if ((getMultiblockState().hasError() || !isFormed) && (getHolder().getOffset() + periodID) % 4 == 0 &&
                checkPatternWithTryLock()) { // per second
            if (getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().execute(() -> {
                    patternLock.lock();
                    if (checkPatternWithLock()) { // formed
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

    @Override
    public void onStructureFormed() {
        isFormed = true;
        this.parts.clear();
        Set<IMultiPart> set = getMultiblockState().getMatchContext().getOrCreate("parts", Collections::emptySet);
        for (IMultiPart part : set) {
            if (shouldAddPartToController(part)) {
                this.parts.add(part);
            }
        }
        this.parts.sort(getDefinition().getPartSorter());
        for (var part : parts) {
            part.addedToController(this);
        }
        updatePartPositions();
    }

    @Override
    public void onStructureInvalid() {
        isFormed = false;
        for (IMultiPart part : parts) {
            part.removedFromController(this);
        }
        parts.clear();
        updatePartPositions();
    }

    /**
     * mark multiblockState as unload error first.
     * if it's actually cuz by block breaking.
     * {@link #onStructureInvalid()} will be called from
     * {@link MultiblockState#onBlockStateChanged(BlockPos, BlockState)}
     */
    @Override
    public void onPartUnload() {
        parts.removeIf(part -> part.self().isInValid());
        getMultiblockState().setError(MultiblockState.UNLOAD_ERROR);
        if (getLevel() instanceof ServerLevel serverLevel) {
            MultiblockWorldSavedData.getOrCreate(serverLevel).addAsyncLogic(this);
        }
        updatePartPositions();
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

    public boolean allowExtendedFacing() {
        return getDefinition().isAllowExtendedFacing();
    }

    public boolean allowFlip() {
        return getDefinition().isAllowFlip();
    }

    @Override
    public boolean isFacingValid(Direction facing) {
        return allowExtendedFacing() || super.isFacingValid(facing);
    }

    public Direction getUpwardsFacing() {
        return this.allowExtendedFacing() ? this.getBlockState().getValue(IMachineBlock.UPWARDS_FACING_PROPERTY) :
                Direction.NORTH;
    }

    public void setUpwardsFacing(@NotNull Direction upwardsFacing) {
        if (!getDefinition().isAllowExtendedFacing()) return;
        if (upwardsFacing == null || upwardsFacing == Direction.UP || upwardsFacing == Direction.DOWN) {
            GTCEu.LOGGER.error("Tried to set upwards facing to invalid facing {}! Skipping", upwardsFacing);
            return;
        }
        BlockState blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock metaMachineBlock &&
                blockState.getValue(IMachineBlock.UPWARDS_FACING_PROPERTY) != upwardsFacing) {
            getLevel().setBlockAndUpdate(getPos(),
                    blockState.setValue(IMachineBlock.UPWARDS_FACING_PROPERTY, upwardsFacing));
            if (getLevel() != null && !getLevel().isClientSide) {
                notifyBlockUpdate();
                markDirty();
                checkPattern();
            }
        }
    }

    @Override
    protected InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                              BlockHitResult hitResult) {
        if (gridSide == getFrontFacing() && allowExtendedFacing()) {
            setUpwardsFacing(playerIn.isShiftKeyDown() ? getUpwardsFacing().getCounterClockWise() :
                    getUpwardsFacing().getClockWise());
            return InteractionResult.CONSUME;
        }
        if (playerIn.isShiftKeyDown()) {
            if (gridSide == getFrontFacing() || !isFacingValid(gridSide)) {
                return InteractionResult.FAIL;
            }
            if (!isRemote()) {
                setFrontFacing(gridSide);
            }
            return InteractionResult.CONSUME;
        }
        return super.onWrenchClick(playerIn, hand, gridSide, hitResult);
    }

    @Override
    public void setFrontFacing(Direction facing) {
        Direction oldFacing = getFrontFacing();

        if (allowExtendedFacing()) {
            Direction newUpwardsFacing = RelativeDirection.simulateAxisRotation(facing, oldFacing, getUpwardsFacing());
            setUpwardsFacing(newUpwardsFacing);
        }
        super.setFrontFacing(facing);

        if (getLevel() != null && !getLevel().isClientSide) {
            notifyBlockUpdate();
            markDirty();
            checkPattern();
        }
    }
}
