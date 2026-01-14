package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

import appeng.api.networking.*;
import appeng.api.networking.security.IActionSource;
import lombok.Getter;

import java.util.EnumSet;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MEHatchPartMachine extends FluidHatchPartMachine implements IGridConnectedMachine {

    protected final static int CONFIG_SIZE = 16;

    @SaveField
    protected final GridNodeHolder nodeHolder;

    @SyncToClient
    @Getter
    protected boolean isOnline;

    protected final IActionSource actionSource;

    public MEHatchPartMachine(BlockEntityCreationInfo info, IO io) {
        super(info, GTValues.UHV, io, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_1X, CONFIG_SIZE);
        this.nodeHolder = createNodeHolder();
        this.actionSource = IActionSource.ofMachine(nodeHolder.getMainNode()::getNode);
    }

    public void setOnline(boolean online) {
        isOnline = online;
        syncDataHolder.markClientSyncFieldDirty("isOnline");
    }

    protected GridNodeHolder createNodeHolder() {
        return new GridNodeHolder(this);
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        IGridConnectedMachine.super.onMainNodeStateChanged(reason);
        this.updateTankSubscription();
    }

    @Override
    protected void updateTankSubscription() {
        if (shouldSubscribe()) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    protected boolean shouldSubscribe() {
        return isWorkingEnabled() && isOnline();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        getMainNode().setExposedOnSides(EnumSet.of(newFacing));
    }

    // By returning false here, we don't allow shift-clicking
    // with a screwdriver to swap the IO.
    @Override
    public boolean swapIO() {
        return false;
    }
}
