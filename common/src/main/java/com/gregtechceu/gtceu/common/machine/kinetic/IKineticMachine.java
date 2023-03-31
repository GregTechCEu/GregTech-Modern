package com.gregtechceu.gtceu.common.machine.kinetic;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.KineticMachineDefinition;

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
}
