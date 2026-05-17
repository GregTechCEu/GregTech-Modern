package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;

import net.minecraftforge.energy.IEnergyStorage;

import lombok.Getter;

import java.util.List;

public class ConverterTrait extends NotifiableEnergyContainer {

    /**
     * If TRUE, the front facing of the machine will OUTPUT EU, other sides INPUT FE.
     * If FALSE, the front facing of the machine will OUTPUT FE, other sides INPUT EU.
     */
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private boolean feToEu;
    @Getter
    private final int amps;
    @Getter
    private final long voltage;
    @Getter
    private final FEContainer feContainer;

    public ConverterTrait(ConverterMachine machine, int tier, int amps) {
        super(GTValues.V[tier] * 16 * amps, GTValues.V[tier], amps,
                GTValues.V[tier], amps);
        this.amps = amps;
        this.voltage = GTValues.V[tier];
        setSideInputCondition(side -> !feToEu && side != getMachine().getFrontFacing());
        setSideOutputCondition(side -> feToEu && side == getMachine().getFrontFacing());
        this.feContainer = machine.attachTrait(new FEContainer());
    }

    @Override
    public ConverterMachine getMachine() {
        return (ConverterMachine) super.getMachine();
    }

    @Override
    protected List<Class<?>> validMachineClasses() {
        return List.of(ConverterMachine.class);
    }

    ////////////////////////////////
    // ***** Initialization ******//
    ////////////////////////////////

    public void setFeToEu(boolean feToEu) {
        this.feToEu = feToEu;
        setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_FE_TO_EU, feToEu));
        syncDataHolder.markClientSyncFieldDirty("feToEu");
        getMachine().notifyBlockUpdate();
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
            var fontFacing = getMachine().getFrontFacing();
            var energyContainer = GTCapabilityHelper.getForgeEnergy(getLevel(),
                    getBlockPos().relative(fontFacing), fontFacing.getOpposite());
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

    public class FEContainer extends MachineTrait implements IEnergyStorage {

        public static final MachineTraitType<FEContainer> TYPE = new MachineTraitType<>(FEContainer.class);

        @Override
        public MachineTraitType<FEContainer> getTraitType() {
            return TYPE;
        }

        public FEContainer() {
            super();
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!feToEu || maxReceive <= 0) return 0;
            int received = (int) (Math.min(this.getMaxLongEnergyStored() - this.getLongEnergyStored(), maxReceive));
            received -= received % FeCompat.ratio(true); // avoid rounding issues
            if (!simulate) {
                addEnergy(FeCompat.toEu(received, FeCompat.ratio(true)));
            }
            return received;
        }

        public long getMaxLongEnergyStored() {
            return FeCompat.toFeLong(ConverterTrait.this.getEnergyCapacity(), FeCompat.ratio(feToEu));
        }

        public long getLongEnergyStored() {
            return FeCompat.toFeLong(ConverterTrait.this.getEnergyStored(), FeCompat.ratio(feToEu));
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
    }
}
