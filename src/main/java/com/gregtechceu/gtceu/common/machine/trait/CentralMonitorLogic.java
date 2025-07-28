package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.util.Mth;

import lombok.Getter;

public class CentralMonitorLogic extends RecipeLogic implements IWorkable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CentralMonitorLogic.class,
            RecipeLogic.MANAGED_FIELD_HOLDER);

    private static final int BASE_UPDATE_INTERVAL = 8 * 20;

    @Persisted
    private int tick = 0;
    @Persisted
    @Getter
    @DescSynced
    private boolean isOn = false;

    public CentralMonitorLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    public CentralMonitorMachine getMachine() {
        return (CentralMonitorMachine) machine;
    }

    private boolean consumeEnergy() {
        int tier = Mth.clamp(getMachine().getTier(), GTValues.ULV, GTValues.MAX);
        long energyToDrain = GTValues.VA[tier];
        long resultEnergy = getMachine().getEnergyContainer().getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= getMachine().getEnergyContainer().getEnergyCapacity()) {
            getMachine().getEnergyContainer().removeEnergy(energyToDrain);
            return true;
        }
        return false;
    }

    private int getUpdateInterval() {
        int interval = BASE_UPDATE_INTERVAL;
        for (int i = 0; i < getMachine().getTier(); i++) interval /= 2;
        return Math.max(interval, 1);
    }

    @Override
    public void serverTick() {
        if (!getMachine().isFormed() || !isWorkingEnabled()) {
            setStatus(Status.IDLE);
            return;
        } else setStatus(Status.WORKING);
        if (consumeEnergy()) {
            isOn = true;
            tick = (tick + 1) % getUpdateInterval();
            if (tick == 0) {
                getMachine().tick();
            }
        } else {
            isOn = false;
            tick = Math.max(tick - 2, 1);
        }
    }

    @Override
    public int getProgress() {
        return tick;
    }

    @Override
    public int getMaxProgress() {
        return getUpdateInterval();
    }

    @Override
    public boolean isActive() {
        return getMachine().isFormed();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
