package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TieredWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine
                                                     implements ITieredMachine, IOverclockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            TieredWorkableElectricMultiblockMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    private final int tier;
    @Persisted
    @Getter
    protected int overclockTier;

    public TieredWorkableElectricMultiblockMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, args);
        this.tier = tier;
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    // ******** OVERCLOCK *********//
    //////////////////////////////////////
    @Override
    public int getMinOverclockTier() {
        return 0;
    }

    @Override
    public void setOverclockTier(int tier) {
        if (!isRemote() && tier >= getMinOverclockTier() && tier <= getMaxOverclockTier()) {
            this.overclockTier = tier;
            this.recipeLogic.markLastRecipeDirty();
        }
    }

    @Override
    public long getOverclockVoltage() {
        return Math.min(GTValues.V[getOverclockTier()], super.getOverclockVoltage());
    }

    //////////////////////////////////////
    // ****** RECIPE LOGIC *******//
    //////////////////////////////////////
    @Override
    public int getTier() {
        return Math.min(tier, super.getTier());
    }

    @Override
    public long getMaxVoltage() {
        return Math.min(GTValues.V[tier], super.getMaxVoltage());
    }
}
