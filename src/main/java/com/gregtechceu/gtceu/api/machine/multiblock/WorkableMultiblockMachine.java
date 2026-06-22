package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;

import com.gregtechceu.gtceu.api.machine.trait.WorkLogic;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class WorkableMultiblockMachine extends MultiblockControllerMachine
                                                implements IWorkableMultiController, IMufflableMachine {

    @Getter
    @Persisted
    @DescSynced
    public final WorkLogic workLogic;
    @Getter
    @Setter
    @Persisted
    @DescSynced
    protected boolean isMuffled;
    protected boolean previouslyMuffled = true;
    @Nullable
    @Getter
    protected LongSet activeBlocks;
    protected final List<ISubscription> traitSubscriptions;

    public WorkableMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder);
        this.workLogic = createWorkLogic(args);
        this.traitSubscriptions = new ArrayList<>();
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public void onUnload() {
        super.onUnload();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
    }

    protected WorkLogic createWorkLogic(Object... args) {
        return new WorkLogic(this);
    }

    //////////////////////////////////////
    // *** Multiblock LifeCycle ***//
    //////////////////////////////////////
    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        activeBlocks = getMultiblockState().getMatchContext().getOrDefault("vaBlocks", LongSets.emptySet());
        workLogic.updateTickSubscription();
    }

    @Override
    public void onStructureInvalid() {
        //multi machine will not unsubscribe tick when structure invalid by default
        //reset first to ensure part work state are changed
        workLogic.reset();
        super.onStructureInvalid();
        updateActiveBlocks(false);
        activeBlocks = null;
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        updateActiveBlocks(false);
        activeBlocks = null;
        traitSubscriptions.forEach(ISubscription::unsubscribe);
        traitSubscriptions.clear();
        workLogic.updateTickSubscription();
    }

    //////////////////////////////////////
    // ****** RECIPE LOGIC *******//
    //////////////////////////////////////

    @Override
    public void clientTick() {
        super.clientTick();
        if (previouslyMuffled != isMuffled) {
            previouslyMuffled = isMuffled;

            if (workLogic != null)
                workLogic.updateSound();
        }
    }

    public void updateActiveBlocks(boolean active) {
        if (activeBlocks != null) {
            for (long pos : activeBlocks) {
                var blockPos = BlockPos.of(pos);
                var blockState = getLevel().getBlockState(blockPos);
                if (blockState.hasProperty(GTBlockStateProperties.ACTIVE)) {
                    var newState = blockState.setValue(GTBlockStateProperties.ACTIVE, active);
                    if (newState != blockState) {
                        getLevel().setBlock(blockPos, newState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    }
                }
            }
        }
    }

    @Override
    public void notifyWorkStatusChanged(WorkLogic.Status oldStatus, WorkLogic.Status newStatus) {
        IWorkableMultiController.super.notifyWorkStatusChanged(oldStatus, newStatus);
        if (newStatus == WorkLogic.Status.WORKING || oldStatus == WorkLogic.Status.WORKING) {
            updateActiveBlocks(newStatus == WorkLogic.Status.WORKING);
        }
        for (IMultiPart part : getParts()) {
            MachineRenderState state = part.self().getRenderState();
            if (state.hasProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS)) {
                part.self().setRenderState(state.setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS, newStatus));
            }
        }
    }

    public void afterWorking() {
        for (IMultiPart part : getParts()) {
            part.afterWorking(this);
        }
    }

    @Nullable
    public Component beforeWorking() {
        for (IMultiPart part : getParts()) {
            Component failReason = part.beforeWorking(this);
            if (failReason != null) {
                return failReason;
            }
        }
        return null;
    }

    public boolean onWorking() {
        for (IMultiPart part : getParts()) {
            if (!part.onWorking(this)) {
                return false;
            }
        }
        return true;
    }

    public void onWaiting() {
        for (IMultiPart part : getParts()) {
            part.onWaiting(this);
        }
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (!isWorkingAllowed) {
            for (IMultiPart part : getParts()) {
                part.onPaused(this);
            }
        }
        getWorkLogic().setWorkingEnabled(isWorkingAllowed);
    }

    @Override
    public boolean isWorkLogicAvailable() {
        return isFormed && !getMultiblockState().hasError();
    }
}
