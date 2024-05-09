package com.gregtechceu.gtceu.common.machines.kinetic;

import com.gregtechceu.gtceu.api.machines.feature.IMachineFeature;
import com.gregtechceu.gtceu.common.blockentities.KineticMachineBlockEntity;
import com.gregtechceu.gtceu.common.machines.KineticMachineDefinition;
import net.minecraft.core.Direction;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote IKineticMachine
 */
public interface IKineticMachine extends IMachineFeature {
    default KineticMachineBlockEntity getKineticHolder() {
        return (KineticMachineBlockEntity)self().getHolder();
    }

    default KineticMachineDefinition getKineticDefinition() {
        return (KineticMachineDefinition) self().getDefinition();
    }

    default float getRotationSpeedModifier(Direction direction) {
        return 1;
    }

    default Direction getRotationFacing() {
        var frontFacing = self().getFrontFacing();
        return getKineticDefinition().isFrontRotation() ? frontFacing : (frontFacing.getAxis() == Direction.Axis.Y ? Direction.NORTH : frontFacing.getClockWise());
    }

    default boolean hasShaftTowards(Direction face) {
        return face.getAxis() == getRotationFacing().getAxis();
    }
}
