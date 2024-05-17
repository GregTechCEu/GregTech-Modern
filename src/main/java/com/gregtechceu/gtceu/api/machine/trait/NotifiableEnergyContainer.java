package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class NotifiableEnergyContainer extends NotifiableRecipeHandlerTrait<Long> implements IEnergyContainer {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableEnergyContainer.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);
    @Getter
    protected IO handlerIO;
    @Getter
    @Persisted
    @DescSynced
    protected long energyStored;
    @Getter
    private long energyCapacity, inputVoltage, inputAmperage, outputVoltage, outputAmperage;
    @Setter
    private Predicate<Direction> sideInputCondition, sideOutputCondition;

    protected long amps, lastTimeStamp;
    @Nullable
    protected TickableSubscription outputSubs;
    @Nullable
    protected TickableSubscription updateSubs;

    protected long lastEnergyInputPerSec = 0;
    protected long lastEnergyOutputPerSec = 0;
    protected long energyInputPerSec = 0;
    protected long energyOutputPerSec = 0;

    public NotifiableEnergyContainer(MetaMachine machine, long maxCapacity, long maxInputVoltage, long maxInputAmperage,
                                     long maxOutputVoltage, long maxOutputAmperage) {
        super(machine);
        this.lastTimeStamp = Long.MIN_VALUE;
        this.energyCapacity = maxCapacity;
        this.inputVoltage = maxInputVoltage;
        this.inputAmperage = maxInputAmperage;
        this.outputVoltage = maxOutputVoltage;
        this.outputAmperage = maxOutputAmperage;
        var isIn = (inputVoltage != 0 && inputAmperage != 0);
        var isOut = (outputVoltage != 0 && outputAmperage != 0);
        this.handlerIO = (isIn && isOut) ? IO.BOTH : isIn ? IO.IN : isOut ? IO.OUT : IO.NONE;
    }

    public static NotifiableEnergyContainer emitterContainer(MetaMachine machine, long maxCapacity,
                                                             long maxOutputVoltage, long maxOutputAmperage) {
        return new NotifiableEnergyContainer(machine, maxCapacity, 0L, 0L, maxOutputVoltage, maxOutputAmperage);
    }

    public static NotifiableEnergyContainer receiverContainer(MetaMachine machine, long maxCapacity,
                                                              long maxInputVoltage, long maxInputAmperage) {
        return new NotifiableEnergyContainer(machine, maxCapacity, maxInputVoltage, maxInputAmperage, 0L, 0L);
    }

    public void resetBasicInfo(long maxCapacity, long maxInputVoltage, long maxInputAmperage, long maxOutputVoltage,
                               long maxOutputAmperage) {
        this.energyCapacity = maxCapacity;
        this.inputVoltage = maxInputVoltage;
        this.inputAmperage = maxInputAmperage;
        this.outputVoltage = maxOutputVoltage;
        this.outputAmperage = maxOutputAmperage;
        var isIN = (inputVoltage != 0 && inputAmperage != 0);
        var isOUT = (outputVoltage != 0 && outputAmperage != 0);
        this.handlerIO = (isIN && isOUT) ? IO.BOTH : isIN ? IO.IN : isOUT ? IO.OUT : IO.NONE;
        checkOutputSubscription();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        checkOutputSubscription();
        updateSubs = getMachine().subscribeServerTick(updateSubs, this::updateTick);
    }

    @Override
    public void onMachineUnLoad() {
        super.onMachineUnLoad();
        if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    public void checkOutputSubscription() {
        if (getOutputVoltage() > 0 && getOutputAmperage() > 0) {
            if (getEnergyStored() >= getOutputVoltage()) {
                outputSubs = getMachine().subscribeServerTick(outputSubs, this::serverTick);
            } else if (outputSubs != null) {
                outputSubs.unsubscribe();
                outputSubs = null;
            }
        }
    }

    @Override
    public long getInputPerSec() {
        return lastEnergyInputPerSec;
    }

    @Override
    public long getOutputPerSec() {
        return lastEnergyOutputPerSec;
    }

    public void setEnergyStored(long energyStored) {
        if (this.energyStored == energyStored) return;
        if (energyStored > this.energyStored) {
            energyInputPerSec += energyStored - this.energyStored;
        } else {
            energyOutputPerSec += this.energyStored - energyStored;
        }
        this.energyStored = energyStored;
        checkOutputSubscription();
        notifyListeners();
    }

    public void updateTick() {
        if (getMachine().getOffsetTimer() % 20 == 0) {
            lastEnergyOutputPerSec = energyOutputPerSec;
            lastEnergyInputPerSec = energyInputPerSec;
            energyOutputPerSec = 0;
            energyInputPerSec = 0;
        }
    }

    public void serverTick() {
        if (getMachine().getLevel().isClientSide)
            return;
        if (getEnergyStored() >= getOutputVoltage() && getOutputVoltage() > 0 && getOutputAmperage() > 0) {
            long outputVoltage = getOutputVoltage();
            long outputAmperes = Math.min(getEnergyStored() / outputVoltage, getOutputAmperage());
            if (outputAmperes == 0) return;
            long amperesUsed = 0;
            for (Direction side : GTUtil.DIRECTIONS) {
                if (!outputsEnergy(side)) continue;
                var oppositeSide = side.getOpposite();
                var energyContainer = GTCapabilityHelper.getEnergyContainer(machine.getLevel(),
                        machine.getPos().relative(side), oppositeSide);
                if (energyContainer != null && energyContainer.inputsEnergy(oppositeSide)) {
                    amperesUsed += energyContainer.acceptEnergyFromNetwork(oppositeSide, outputVoltage,
                            outputAmperes - amperesUsed);
                    if (amperesUsed == outputAmperes) break;
                }
            }
            if (amperesUsed > 0) {
                setEnergyStored(getEnergyStored() - amperesUsed * outputVoltage);
            }
        }
    }

    public boolean dischargeOrRechargeEnergyContainers(IItemHandlerModifiable itemHandler, int slotIndex,
                                                       boolean simulate) {
        var stackInSlot = itemHandler.getStackInSlot(slotIndex).copy();
        if (stackInSlot.isEmpty()) { // no stack to charge/discharge
            return false;
        }

        var electricItem = GTCapabilityHelper.getElectricItem(stackInSlot);
        if (electricItem != null) {
            if (handleElectricItem(stackInSlot, electricItem, simulate)) {
                if (!simulate) {
                    itemHandler.setStackInSlot(slotIndex, stackInSlot);
                }
                return true;
            }
        }
        return false;
    }

    private boolean handleElectricItem(ItemStack stack, IElectricItem electricItem, boolean simulate) {
        var machineTier = GTUtil.getTierByVoltage(Math.max(getInputVoltage(), getOutputVoltage()));
        var chargeTier = Math.min(machineTier, electricItem.getTier());
        var chargePercent = getEnergyStored() / (getEnergyCapacity() * 1.0);

        // Check if the item is a battery (or similar), and if we can receive some amount of energy
        if (electricItem.canProvideChargeExternally() && getEnergyCanBeInserted() > 0) {

            // Drain from the battery if we are below half energy capacity, and if the tier matches
            if (chargePercent <= 0.5 && chargeTier == machineTier) {
                long dischargedBy = electricItem.discharge(getEnergyCanBeInserted(), machineTier, false, true,
                        simulate);
                if (!simulate) {
                    addEnergy(dischargedBy);
                }
                return dischargedBy > 0L;
            }
        }

        // Else, check if we have above 65% power
        if (chargePercent > 0.65) {
            long chargedBy = electricItem.charge(getEnergyStored(), chargeTier, false, simulate);
            if (!simulate) {
                removeEnergy(chargedBy);
            }
            return chargedBy > 0;
        }
        return false;
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        var latestTimeStamp = getMachine().getOffsetTimer();
        if (lastTimeStamp < latestTimeStamp) {
            amps = 0;
            lastTimeStamp = latestTimeStamp;
        }
        if (amps >= getInputAmperage()) return 0;
        long canAccept = getEnergyCapacity() - getEnergyStored();
        if (voltage > 0L && (side == null || inputsEnergy(side))) {
            if (voltage > getInputVoltage() && machine instanceof IExplosionMachine explosionMachine) {
                explosionMachine.doExplosion(GTUtil.getExplosionPower(voltage));
                return Math.min(amperage, getInputAmperage() - amps);
            }
            if (canAccept >= voltage) {
                long amperesAccepted = Math.min(canAccept / voltage, Math.min(amperage, getInputAmperage() - amps));
                if (amperesAccepted > 0) {
                    setEnergyStored(getEnergyStored() + voltage * amperesAccepted);
                    amps += amperesAccepted;
                    return amperesAccepted;
                }
            }
        }
        return 0;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return !outputsEnergy(side) && getInputVoltage() > 0 &&
                (sideInputCondition == null || sideInputCondition.test(side));
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return getOutputVoltage() > 0 && (sideOutputCondition == null || sideOutputCondition.test(side));
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        long oldEnergyStored = getEnergyStored();
        long newEnergyStored = (energyCapacity - oldEnergyStored < energyToAdd) ? energyCapacity :
                (oldEnergyStored + energyToAdd);
        if (newEnergyStored < 0)
            newEnergyStored = 0;
        setEnergyStored(newEnergyStored);
        return newEnergyStored - oldEnergyStored;
    }

    @Override
    public List<Long> handleRecipeInner(IO io, GTRecipe recipe, List<Long> left, @Nullable String slotName,
                                        boolean simulate) {
        IEnergyContainer capability = this;
        long sum = left.stream().reduce(0L, Long::sum);
        if (io == IO.IN) {
            var canOutput = capability.getEnergyStored();
            if (!simulate) {
                capability.addEnergy(-Math.min(canOutput, sum));
            }
            sum = sum - canOutput;
        } else if (io == IO.OUT) {
            long canInput = capability.getEnergyCapacity() - capability.getEnergyStored();
            if (!simulate) {
                capability.addEnergy(Math.min(canInput, sum));
            }
            sum = sum - canInput;
        }
        return sum <= 0 ? null : Collections.singletonList(sum);
    }

    @Override
    public List<Object> getContents() {
        return List.of(energyStored);
    }

    @Override
    public double getTotalContentAmount() {
        return energyStored;
    }

    @Override
    public RecipeCapability<Long> getCapability() {
        return EURecipeCapability.CAP;
    }
}
