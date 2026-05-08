package com.gregtechceu.gtceu.common.machine;

import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface MachineInstanceFactories {

    MachineInstanceFactory.Tiered ROCK_CRUSHER = (info, tier) -> {
        var machine = new SimpleTieredMachine(info, tier, GTMachineUtils.defaultTankSizeFunction);
        machine.getEnvironmentalExplosionTrait().setEnableEnvironmentalExplosions(false);
        return machine;
    };
}
