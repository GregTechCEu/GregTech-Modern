package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NotifiableLaserContainer extends NotifiableEnergyContainer implements ILaserContainer {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableEnergyContainer.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

    public NotifiableLaserContainer(MetaMachine machine, long maxCapacity, long maxInputVoltage, long maxInputAmperage,
                                    long maxOutputVoltage, long maxOutputAmperage) {
        super(machine, maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
    }

    public static NotifiableLaserContainer emitterContainer(MetaMachine machine, long maxCapacity,
                                                            long maxOutputVoltage, long maxOutputAmperage) {
        return new NotifiableLaserContainer(machine, maxCapacity, 0L, 0L, maxOutputVoltage, maxOutputAmperage);
    }

    public static NotifiableLaserContainer receiverContainer(MetaMachine machine, long maxCapacity,
                                                             long maxInputVoltage, long maxInputAmperage) {
        return new NotifiableLaserContainer(machine, maxCapacity, maxInputVoltage, maxInputAmperage, 0L, 0L);
    }

    @Override
    public void serverTick() {
        amps = 0;
        if (getMachine().getLevel().isClientSide)
            return;
        if (getEnergyStored() < getOutputVoltage() || getOutputVoltage() <= 0 || getOutputAmperage() <= 0)
            return;
        long outputVoltage = getOutputVoltage();
        long outputAmperes = Math.min(getEnergyStored() / outputVoltage, getOutputAmperage());
        if (outputAmperes == 0) return;
        long amperesUsed = 0;
        for (Direction side : GTUtil.DIRECTIONS) {
            if (!outputsEnergy(side)) continue;
            BlockEntity tileEntity = getMachine().getLevel().getBlockEntity(getMachine().getPos().relative(side));
            Direction oppositeSide = side.getOpposite();
            ILaserContainer laserContainer = GTCapabilityHelper.getLaser(getMachine().getLevel(),
                    getMachine().getPos().relative(side), oppositeSide);
            if (tileEntity != null && laserContainer != null) {
                if (laserContainer == null || !laserContainer.inputsEnergy(oppositeSide)) continue;
                amperesUsed += laserContainer.acceptEnergyFromNetwork(oppositeSide, outputVoltage,
                        outputAmperes - amperesUsed);
                if (amperesUsed == outputAmperes) break;
            }
        }
        if (amperesUsed > 0) {
            setEnergyStored(getEnergyStored() - amperesUsed * outputVoltage);
        }
    }
}
