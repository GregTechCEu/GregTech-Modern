package com.gregtechceu.gtceu.common.machine.kinetic;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.KineticMachineDefinition;
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
