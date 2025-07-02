package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

import appeng.api.networking.*;
import appeng.api.networking.security.IActionSource;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumSet;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MEHatchPartMachine extends FluidHatchPartMachine implements IGridConnectedMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEHatchPartMachine.class,
            FluidHatchPartMachine.MANAGED_FIELD_HOLDER);

    protected final static int CONFIG_SIZE = 16;

    @Persisted
    protected final GridNodeHolder nodeHolder;

    @DescSynced
    @Getter
    @Setter
    protected boolean isOnline;

    protected final IActionSource actionSource;

    public MEHatchPartMachine(IMachineBlockEntity holder, IO io, Object... args) {
        super(holder, GTValues.UHV, io, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_1X, CONFIG_SIZE, args);
        this.nodeHolder = createNodeHolder();
        this.actionSource = IActionSource.ofMachine(nodeHolder.getMainNode()::getNode);
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

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // By returning false here, we don't allow shift-clicking
    // with a screwdriver to swap the IO.
    @Override
    public boolean swapIO() {
        return false;
    }
}
