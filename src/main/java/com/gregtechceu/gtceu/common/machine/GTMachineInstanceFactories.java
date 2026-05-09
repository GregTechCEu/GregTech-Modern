package com.gregtechceu.gtceu.common.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.hpca.HPCAComponentPartMachine;
import com.gregtechceu.gtceu.common.machine.trait.hpca.HPCAComponentTrait;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface GTMachineInstanceFactories {

    MachineInstanceFactory.Tiered<SimpleTieredMachine> ROCK_CRUSHER = (info, tier) -> {
        var machine = new SimpleTieredMachine(info, tier);
        machine.getEnvironmentalExplosionTrait().setEnableEnvironmentalExplosions(false);
        return machine;
    };

    ///// HPCA stuff

    MachineInstanceFactory<HPCAComponentPartMachine> HPCA_EMPTY = (info) -> {
        var hpcaTrait = new HPCAComponentTrait(0, 0, false, false);
        return new HPCAComponentPartMachine(info, false, GTGuiTextures.HPCA_EMPTY_COMPONENT, GTGuiTextures.HPCA_EMPTY_COMPONENT, hpcaTrait);
    };

    MachineInstanceFactory<HPCAComponentPartMachine> HPCA_BRIDGE = (info) -> {
        var hpcaTrait = new HPCAComponentTrait(GTValues.VA[GTValues.IV], GTValues.VA[GTValues.IV], false, true);
        return new HPCAComponentPartMachine(info, true, GTGuiTextures.HPCA_BRIDGE_COMPONENT, GTGuiTextures.HPCA_BRIDGE_COMPONENT, hpcaTrait);
    };

    MachineInstanceFactory<HPCAComponentPartMachine> HPCA_COMPUTATION = (info) -> {
        var hpcaTrait = HPCAComponentPartMachine.createHPCAComputationTrait(false);
        return new HPCAComponentPartMachine(info, false, GTGuiTextures.HPCA_COMPUTATION_COMPONENT, GTGuiTextures.HPCA_DAMAGED_COMPUTATION_COMPONENT, hpcaTrait);
    };

    MachineInstanceFactory<HPCAComponentPartMachine> HPCA_COMPUTATION_ADVANCED = (info) -> {
        var hpcaTrait = HPCAComponentPartMachine.createHPCAComputationTrait(true);
        return new HPCAComponentPartMachine(info, true, GTGuiTextures.HPCA_ADVANCED_COMPUTATION_COMPONENT, GTGuiTextures.HPCA_DAMAGED_ADVANCED_COMPUTATION_COMPONENT, hpcaTrait);
    };

    MachineInstanceFactory<HPCAComponentPartMachine> HPCA_COOLER = (info) -> {
        var hpcaTrait = HPCAComponentPartMachine.createHPCACoolerTrait(false);
        return new HPCAComponentPartMachine(info, false, GTGuiTextures.HPCA_HEAT_SINK_COMPONENT, GTGuiTextures.HPCA_HEAT_SINK_COMPONENT, hpcaTrait);
    };

    MachineInstanceFactory<HPCAComponentPartMachine> HPCA_COOLER_ADVANCED = (info) -> {
        var hpcaTrait = HPCAComponentPartMachine.createHPCACoolerTrait(true);
        return new HPCAComponentPartMachine(info, true, GTGuiTextures.HPCA_ACTIVE_COOLER_COMPONENT, GTGuiTextures.HPCA_ACTIVE_COOLER_COMPONENT, hpcaTrait);
    };
}
