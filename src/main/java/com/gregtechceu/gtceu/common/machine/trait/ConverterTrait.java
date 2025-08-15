package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraftforge.energy.IEnergyStorage;

import lombok.Getter;

public class ConverterTrait extends NotifiableEnergyContainer {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ConverterTrait.class,
            NotifiableEnergyContainer.MANAGED_FIELD_HOLDER);

    /**
     * If TRUE, the front facing of the machine will OUTPUT EU, other sides INPUT FE.
     * If FALSE, the front facing of the machine will OUTPUT FE, other sides INPUT EU.
     */
    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    private boolean feToEu;
    @Getter
    private final int amps;
    @Getter
    private final long voltage;
    @Getter
    private final FEContainer feContainer;

    public ConverterTrait(ConverterMachine machine, int amps) {
        super(machine, GTValues.V[machine.getTier()] * 16 * amps, GTValues.V[machine.getTier()], amps,
                GTValues.V[machine.getTier()], amps);
        this.amps = amps;
        this.voltage = GTValues.V[machine.getTier()];
        setSideInputCondition(side -> !this.feToEu && side != this.getMachine().getFrontFacing());
        setSideOutputCondition(side -> this.feToEu && side == this.getMachine().getFrontFacing());
        this.feContainer = new FEContainer(machine);
    }

    ////////////////////////////////
    // ***** Initialization ******//
    ////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public void setFeToEu(boolean feToEu) {
        this.feToEu = feToEu;
        setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_FE_TO_EU, feToEu));
        machine.notifyBlockUpdate();
    }

    //////////////////////////////
    // ********* logic *********//
    //////////////////////////////
    public void checkOutputSubscription() {
        outputSubs = getMachine().subscribeServerTick(outputSubs, this::serverTick);
    }

    @Override
    public void serverTick() {
        if (feToEu) { // output eu
            super.serverTick();
        } else { // output fe
            var fontFacing = machine.getFrontFacing();
            var energyContainer = GTCapabilityHelper.getForgeEnergy(machine.getLevel(),
                    machine.getPos().relative(fontFacing), fontFacing.getOpposite());
            if (energyContainer != null && energyContainer.canReceive()) {
                var energyUsed = FeCompat.insertEu(energyContainer,
                        Math.min(getEnergyStored(), voltage * amps), false);
                if (energyUsed > 0) {
                    setEnergyStored(getEnergyStored() - energyUsed);
                }
            }
        }
    }

    //////////////////////////////
    // ***** Forge Energy ******//
    //////////////////////////////

    private class FEContainer extends MachineTrait implements IEnergyStorage {

        protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FEContainer.class);

        public FEContainer(MetaMachine machine) {
            super(machine);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!feToEu || maxReceive <= 0) return 0;
            int received = Math.min(this.getMaxEnergyStored() - this.getEnergyStored(), maxReceive);
            received -= received % FeCompat.ratio(true); // avoid rounding issues
            if (!simulate) {
                addEnergy(FeCompat.toEu(received, FeCompat.ratio(true)));
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return FeCompat.toFeBounded(ConverterTrait.this.getEnergyStored(), FeCompat.ratio(feToEu),
                    Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEnergyStored() {
            return FeCompat.toFeBounded(ConverterTrait.this.getEnergyCapacity(), FeCompat.ratio(feToEu),
                    Integer.MAX_VALUE);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return feToEu;
        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }
    }
}
