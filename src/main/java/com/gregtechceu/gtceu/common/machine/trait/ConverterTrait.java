package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.FeCompat;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConverterTrait extends MachineTrait {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ConverterTrait.class);

    @Getter
    private final int amps;
    @Getter
    private final long voltage;

    /**
     * If TRUE, the front facing of the machine will OUTPUT EU, other sides INPUT FE.
     * <p>
     * If FALSE, the front facing of the machine will OUTPUT FE, other sides INPUT EU.
     */
    @Getter
    @Setter
    private boolean feToEu;

    @Getter
    private final IEnergyStorage energyFEContainer = new FEContainer();
    @Getter
    private final IEnergyContainer energyEUContainer = new EUContainer();
    protected long storedEU;

    private final long baseCapacity;

    private long usedAmps;

    @Nullable
    protected TickableSubscription outputSubs;

    public ConverterTrait(ConverterMachine mte, int amps, boolean feToEu) {
        super(mte);
        this.amps = amps;
        this.feToEu = feToEu;
        this.voltage = GTValues.V[mte.getTier()];
        this.baseCapacity = this.voltage * 16 * amps;
    }

    private long extractInternal(long amount) {
        if (amount <= 0) return 0;
        long change = Math.min(storedEU, amount);
        storedEU -= change;
        return change;
    }

    @NotNull
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("StoredEU", storedEU);
        nbt.putBoolean("feToEu", feToEu);
        return nbt;
    }

    public void deserializeNBT(@NotNull CompoundTag nbt) {
        this.storedEU = nbt.getLong("StoredEU");
        this.feToEu = nbt.getBoolean("feToEu");
    }

    public void checkOutputSubscription() {
        outputSubs = getMachine().subscribeServerTick(outputSubs, this::serverTick);
    }

    @Override
    public void onMachineLoad() {
        checkOutputSubscription();
    }

    public void serverTick() {
        this.usedAmps = 0;
        if (!getMachine().getLevel().isClientSide) {
            pushEnergy();
        }
    }

    protected void pushEnergy() {
        long energyInserted;
        if (feToEu) { // push out EU
            // Get the EU capability in front of us
            IEnergyContainer container = getCapabilityAtFront(GTCapability.CAPABILITY_ENERGY_CONTAINER);
            if (container == null) return;

            // make sure we can output at least 1 amp
            long ampsToInsert = Math.min(amps, storedEU / voltage);
            if (ampsToInsert == 0) return;

            // send out energy
            energyInserted = container.acceptEnergyFromNetwork(getMachine().getFrontFacing().getOpposite(), voltage,
                    ampsToInsert) * voltage;
        } else { // push out FE
            // Get the FE capability in front of us
            IEnergyStorage storage = getCapabilityAtFront(Capabilities.EnergyStorage.BLOCK);
            if (storage == null) return;

            // send out energy
            energyInserted = FeCompat.insertEu(storage, storedEU);
        }
        extractInternal(energyInserted);
    }

    protected <T> T getCapabilityAtFront(BlockCapability<T, Direction> capability) {
        return getMachine().getLevel().getCapability(capability,
                getMachine().getPos().relative(getMachine().getFrontFacing()),
                getMachine().getFrontFacing().getOpposite());
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // -- GTCEu Energy--------------------------------------------

    public class EUContainer implements IEnergyContainer {

        @Override
        public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
            if (amperage <= 0 || voltage <= 0 || feToEu || side == getMachine().getFrontFacing())
                return 0;
            if (usedAmps >= amps) return 0;
            if (voltage > getInputVoltage()) {
                if (getMachine() instanceof IExplosionMachine explosionMachine) {
                    explosionMachine.doExplosion(GTUtil.getExplosionPower(voltage));
                }
                return Math.min(amperage, amps - usedAmps);
            }

            long space = baseCapacity - storedEU;
            if (space < voltage) return 0;
            long maxAmps = Math.min(Math.min(amperage, amps - usedAmps), space / voltage);
            storedEU += voltage * maxAmps;
            usedAmps += maxAmps;
            return maxAmps;
        }

        @Override
        public boolean inputsEnergy(Direction side) {
            return !feToEu && side != getMachine().getFrontFacing();
        }

        @Override
        public long changeEnergy(long amount) {
            if (amount == 0) return 0;
            return amount > 0 ? addEnergy(amount) : removeEnergy(-amount);
        }

        @Override
        public long addEnergy(long energyToAdd) {
            if (energyToAdd <= 0) return 0;
            long original = energyToAdd;

            // add energy to internal buffer
            long change = Math.min(baseCapacity - storedEU, energyToAdd);
            storedEU += change;
            energyToAdd -= change;

            return original - energyToAdd;
        }

        @Override
        public long removeEnergy(long energyToRemove) {
            return extractInternal(energyToRemove);
        }

        @Override
        public long getEnergyStored() {
            return storedEU;
        }

        @Override
        public long getEnergyCapacity() {
            return baseCapacity;
        }

        @Override
        public long getInputAmperage() {
            return feToEu ? 0 : amps;
        }

        @Override
        public long getInputVoltage() {
            return voltage;
        }

        @Override
        public long getOutputAmperage() {
            return feToEu ? amps : 0;
        }

        @Override
        public long getOutputVoltage() {
            return voltage;
        }
    }

    // -- Forge Energy--------------------------------------------

    public class FEContainer implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!feToEu || maxReceive <= 0) return 0;
            int received = Math.min(getMaxEnergyStored() - getEnergyStored(), maxReceive);
            received -= received % FeCompat.ratio(true); // avoid rounding issues
            if (!simulate) storedEU += FeCompat.toEu(received, FeCompat.ratio(true));
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return (int) FeCompat.toFeBounded(storedEU, FeCompat.ratio(feToEu), Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) FeCompat.toFeBounded(baseCapacity, FeCompat.ratio(feToEu), Integer.MAX_VALUE);
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
