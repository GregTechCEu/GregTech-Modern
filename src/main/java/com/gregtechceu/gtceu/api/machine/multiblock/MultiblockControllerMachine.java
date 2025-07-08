package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockControllerMachine extends MetaMachine implements IMultiController {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MultiblockControllerMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);
    private MultiblockState multiblockState;
    private final List<IMultiPart> parts = new ArrayList<>();
    private @Nullable IParallelHatch parallelHatch = null;
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
    @Nullable
    private TickableSubscription patternRefreshSubscription;

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
        if (!isRemote()) {
            updatePatternRefreshSubscription();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();

        unsubscribe(patternRefreshSubscription);
        patternRefreshSubscription = null;
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

    @Override
    public Optional<IParallelHatch> getParallelHatch() {
        return Optional.ofNullable(parallelHatch);
    }

    //////////////////////////////////////
    // *** Multiblock LifeCycle ***//
    //////////////////////////////////////

    public final void updatePatternRefreshSubscription() {
        if (getMultiblockState().hasError() && !isFormed()) {
            patternRefreshSubscription = subscribeServerTick(patternRefreshSubscription, this::refreshPatternState);
        } else {
            unsubscribe(patternRefreshSubscription);
            patternRefreshSubscription = null;
        }
    }

    protected final void refreshPatternState() {
        // check the multiblock pattern every 40 ticks, or 2 seconds
        if (getOffsetTimer() % 40 != 0) {
            return;
        }
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (checkPattern()) {
            setFlipped(getMultiblockState().isNeededFlip());
            onStructureFormed();
            var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
            mwsd.addMapping(getMultiblockState());

            updatePatternRefreshSubscription();
        }
    }

    @Override
    public void onStructureFormed() {
        isFormed = true;
        MachineRenderState renderState = getRenderState();
        if (renderState.hasProperty(IMultiController.IS_FORMED_PROPERTY)) {
            setRenderState(renderState.setValue(IMultiController.IS_FORMED_PROPERTY, true));
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
            if (part instanceof IParallelHatch pHatch) {
                parallelHatch = pHatch;
            }
            part.addedToController(this);
        }
        updatePartPositions();
    }

    @Override
    public void onStructureInvalid() {
        isFormed = false;
        MachineRenderState renderState = getRenderState();
        if (renderState.hasProperty(IMultiController.IS_FORMED_PROPERTY)) {
            setRenderState(renderState.setValue(IMultiController.IS_FORMED_PROPERTY, false));
        }

        for (IMultiPart part : parts) {
            part.removedFromController(this);
        }
        parallelHatch = null;
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
        if (!isRemote()) {
            updatePatternRefreshSubscription();
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

            updatePatternRefreshSubscription();
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
            getLevel().setBlockAndUpdate(getPos(),
                    blockState.setValue(GTBlockStateProperties.UPWARDS_FACING, upwardsFacing));
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
            return InteractionResult.sidedSuccess(playerIn.level().isClientSide);
        }
        if (playerIn.isShiftKeyDown()) {
            if (gridSide == getFrontFacing() || !isFacingValid(gridSide)) {
                return InteractionResult.FAIL;
            }
            if (!isRemote()) {
                setFrontFacing(gridSide);
            }
            return InteractionResult.sidedSuccess(playerIn.level().isClientSide);
        }
        return super.onWrenchClick(playerIn, hand, gridSide, hitResult);
    }

    @Override
    public void setFrontFacing(Direction facing) {
        super.setFrontFacing(facing);

        if (getLevel() != null && !getLevel().isClientSide) {
            checkPattern();
        }
    }
}
