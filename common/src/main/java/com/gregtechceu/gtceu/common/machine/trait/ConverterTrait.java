package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IPlatformEnergyStorage;
import com.gregtechceu.gtceu.api.capability.PlatformEnergyCompat;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class ConverterTrait extends MachineTrait {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ConverterTrait.class);


    @Persisted @DescSynced
    private final int amps;
    @Persisted @DescSynced
    private final long voltage;

    /**
     * If TRUE, the front facing of the machine will OUTPUT EU, other sides INPUT FE.
     * If FALSE, the front facing of the machine will OUTPUT FE, other sides INPUT EU.
     */
    @Persisted @DescSynced
    private boolean feToEu;

    private final IPlatformEnergyStorage energyNative = new PlatformEnergyContainer();
    private final NotifiableEnergyContainer energyEU;
    @Persisted @DescSynced
    protected long storedEU;
    private final long baseCapacity;
    private long usedAmps;
    private BlockPos frontPos;
    private Direction frontDir;

    private TickableSubscription outputSubs;

    public ConverterTrait(ConverterMachine machine, int amps, boolean feToEu) {
        super(machine);
        this.amps = amps;
        this.feToEu = feToEu;
        this.voltage = GTValues.V[machine.getTier()];
        this.baseCapacity = this.voltage * 16 * amps;
        this.energyEU = new EUContainer(machine, baseCapacity, voltage, feToEu ? 0 : amps, voltage, feToEu ? amps : 0);
    }

    public NotifiableEnergyContainer getEnergyEUContainer() {
        return energyEU;
    }

    public IPlatformEnergyStorage getEnergyNativeContainer() {
        return energyNative;
    }

    public boolean isFeToEu() {
        return feToEu;
    }

    public void setFeToEu(boolean feToEu) {
        this.feToEu = feToEu;
    }

    public int getBaseAmps() {
        return amps;
    }

    public long getVoltage() {
        return voltage;
    }

    private long extractInternal(long amount) {
        if (amount <= 0) return 0;
        long change = Math.min(storedEU, amount);
        storedEU -= change;
        return change;
    }

    public void update() {
        if (machine.getFrontFacing() != this.frontDir) {
            this.frontPos = null;
            this.frontDir = null;
        }

        this.usedAmps = 0;
        if (!machine.getLevel().isClientSide) {
            pushEnergy();
        }
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        outputSubs = getMachine().subscribeServerTick(outputSubs, this::update);
    }

    @Override
    public void onMachineUnLoad() {
        super.onMachineUnLoad();
        if (this.outputSubs != null) {
            outputSubs.unsubscribe();
            outputSubs = null;
        }
    }

    protected void pushEnergy() {
        long energyInserted;
        if (feToEu) { // push out EU
            // Get the EU capability in front of us
            IEnergyContainer container = getEUAtFront();
            if (container == null) return;

            // make sure we can output at least 1 amp
            long ampsToInsert = Math.min(amps, storedEU / voltage);
            if (ampsToInsert == 0) return;

            // send out energy
            energyInserted = container.acceptEnergyFromNetwork(machine.getFrontFacing().getOpposite(), voltage, ampsToInsert) * voltage;
        } else { // push out FE
            // Get the FE capability in front of us
            IPlatformEnergyStorage storage = getPlatformEnergyAtFront();
            if (storage == null) return;

            // send out energy
            energyInserted = PlatformEnergyCompat.insertEu(storage, storedEU);
        }
        extractInternal(energyInserted);
    }

    protected IPlatformEnergyStorage getPlatformEnergyAtFront() {
        if (frontPos == null) {
            frontPos = machine.getPos().relative(machine.getFrontFacing());
            frontDir = machine.getFrontFacing();
        }
        Direction opposite = machine.getFrontFacing().getOpposite();
        return GTCapabilityHelper.getPlatformEnergy(machine.getLevel(), frontPos, opposite);
    }

    protected IEnergyContainer getEUAtFront() {
        if (frontPos == null) {
            frontPos = machine.getPos().relative(machine.getFrontFacing());
            frontDir = machine.getFrontFacing();
        }
        Direction opposite = machine.getFrontFacing().getOpposite();
        return GTCapabilityHelper.getEnergyContainer(machine.getLevel(), frontPos, opposite);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // -- GTCEu Energy--------------------------------------------

    public class EUContainer extends NotifiableEnergyContainer {

        public EUContainer(MetaMachine machine, long maxCapacity, long maxInputVoltage, long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage) {
            super(machine, maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
        }

        @Override
        public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
            if (amperage <= 0 || voltage <= 0 || feToEu || side == machine.getFrontFacing())
                return 0;
            if (usedAmps >= ConverterTrait.this.amps) return 0;
            if (voltage > getInputVoltage() && machine instanceof IExplosionMachine explosionMachine) {
                explosionMachine.doExplosion(GTUtil.getExplosionPower(voltage));
                return Math.min(amperage, ConverterTrait.this.amps - usedAmps);
            }

            long space = baseCapacity - storedEU;
            if (space < voltage) return 0;
            long maxAmps = Math.min(Math.min(amperage, ConverterTrait.this.amps - usedAmps), space / voltage);
            storedEU += voltage * maxAmps;
            usedAmps += maxAmps;
            return maxAmps;
        }

        @Override
        public boolean inputsEnergy(Direction side) {
            return !feToEu && side != machine.getFrontFacing();
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
            return feToEu ? 0 : ConverterTrait.this.amps;
        }

        @Override
        public long getInputVoltage() {
            return voltage;
        }

        @Override
        public long getOutputAmperage() {
            return feToEu ? ConverterTrait.this.amps : 0;
        }

        @Override
        public long getOutputVoltage() {
            return voltage;
        }
    }

    // -- Forge Energy--------------------------------------------

    public class PlatformEnergyContainer implements IPlatformEnergyStorage {

        @Override
        public boolean supportsInsertion() {
            return feToEu;
        }

        @Override
        public long insert(long maxReceive, boolean simulate) {
            if (!feToEu || maxReceive <= 0) return 0;
            long received = Math.min(getCapacity() - getAmount(), maxReceive);
            received -= received % PlatformEnergyCompat.ratio(true); // avoid rounding issues
            if (!simulate) storedEU += PlatformEnergyCompat.toEu(received, PlatformEnergyCompat.ratio(true));
            return received;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public long extract(long maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public long getAmount() {
            return PlatformEnergyCompat.toNativeLong(storedEU, PlatformEnergyCompat.ratio(feToEu));
        }

        @Override
        public long getCapacity() {
            return PlatformEnergyCompat.toNativeLong(baseCapacity, PlatformEnergyCompat.ratio(feToEu));
        }
    }
}
