package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NotifiableLaserContainer extends NotifiableEnergyContainer implements ILaserContainer {

    public static final MachineTraitType<NotifiableLaserContainer> TYPE = new MachineTraitType<>(
            NotifiableLaserContainer.class);

    public NotifiableLaserContainer(long maxCapacity, long maxInputVoltage, long maxInputAmperage,
                                    long maxOutputVoltage, long maxOutputAmperage) {
        super(maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
    }

    public static NotifiableLaserContainer emitterContainer(long maxCapacity,
                                                            long maxOutputVoltage, long maxOutputAmperage) {
        return new NotifiableLaserContainer(maxCapacity, 0L, 0L, maxOutputVoltage, maxOutputAmperage);
    }

    public static NotifiableLaserContainer receiverContainer(long maxCapacity,
                                                             long maxInputVoltage, long maxInputAmperage) {
        return new NotifiableLaserContainer(maxCapacity, maxInputVoltage, maxInputAmperage, 0L, 0L);
    }

    @Override
    public MachineTraitType<NotifiableLaserContainer> getTraitType() {
        return TYPE;
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
            BlockEntity tileEntity = getMachine().getLevel().getBlockEntity(getMachine().getBlockPos().relative(side));
            Direction oppositeSide = side.getOpposite();
            ILaserContainer laserContainer = GTCapabilityHelper.getLaser(getMachine().getLevel(),
                    getMachine().getBlockPos().relative(side), oppositeSide);
            if (tileEntity != null && laserContainer != null) {
                if (!laserContainer.inputsEnergy(oppositeSide)) continue;
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
