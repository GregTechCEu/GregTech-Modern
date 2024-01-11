package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IPlatformEnergyStorage;
import com.gregtechceu.gtceu.api.capability.PlatformEnergyCompat;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;

public class ConverterTrait extends NotifiableEnergyContainer implements IPlatformEnergyStorage {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ConverterTrait.class, NotifiableEnergyContainer.MANAGED_FIELD_HOLDER);

    /**
     * If TRUE, the front facing of the machine will OUTPUT EU, other sides INPUT FE.
     * If FALSE, the front facing of the machine will OUTPUT FE, other sides INPUT EU.
     */
    @Getter @Persisted @DescSynced @RequireRerender
    private boolean feToEu;
    @Getter
    private final int amps;
    @Getter
    private final long voltage;

    public ConverterTrait(ConverterMachine machine, int amps) {
        super(machine, GTValues.V[machine.getTier()] * 16 * amps, GTValues.V[machine.getTier()], amps, GTValues.V[machine.getTier()], amps);
        this.amps = amps;
        this.voltage = GTValues.V[machine.getTier()];
        setSideInputCondition(side -> !this.feToEu && side != this.getMachine().getFrontFacing());
        setSideOutputCondition(side -> this.feToEu && side == this.getMachine().getFrontFacing());
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public void setFeToEu(boolean feToEu) {
        this.feToEu = feToEu;
        machine.notifyBlockUpdate();
    }

    //////////////////////////////////////
    //*********      logic     *********//
    //////////////////////////////////////
    public void checkOutputSubscription() {
        outputSubs = getMachine().subscribeServerTick(outputSubs, this::serverTick);
    }

    @Override
    public void serverTick() {
        if (feToEu) { // output eu
            super.serverTick();
        } else { // output fe
            var fontFacing = machine.getFrontFacing();
            var energyContainer = GTCapabilityHelper.getPlatformEnergy(machine.getLevel(), machine.getPos().relative(fontFacing), fontFacing.getOpposite());
            if (energyContainer != null && energyContainer.supportsInsertion()) {
                var energyUsed = PlatformEnergyCompat.insertEu(energyContainer, Math.min(getEnergyStored(), voltage * amps));
                if (energyUsed > 0) {
                    setEnergyStored(getEnergyStored() - energyUsed);
                }
            }
        }
    }

    //////////////////////////////////////
    //****      PlatformEnergy     *****//
    //////////////////////////////////////
    @Override
    public boolean supportsInsertion() {
        return feToEu;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long insert(long maxReceive, boolean simulate) {
        if (!feToEu || maxReceive <= 0) return 0;
        long received = Math.min(getCapacity() - getAmount(), maxReceive);
        received -= received % PlatformEnergyCompat.ratio(true); // avoid rounding issues
        if (!simulate) {
            addEnergy(PlatformEnergyCompat.toEu(received, PlatformEnergyCompat.ratio(true)));
        }
        return received;
    }

    @Override
    public long extract(long maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public long getAmount() {
        return PlatformEnergyCompat.toNativeLong(getEnergyStored(), PlatformEnergyCompat.ratio(feToEu));
    }

    @Override
    public long getCapacity() {
        return PlatformEnergyCompat.toNativeLong(getEnergyCapacity(), PlatformEnergyCompat.ratio(feToEu));
    }
}
