package com.gregtechceu.gtceu.common.machines.electric;

import com.gregtechceu.gtceu.api.machines.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machines.SimpleTieredMachine;
import com.gregtechceu.gtceu.data.GTMachines;

public class RockCrusherMachine extends SimpleTieredMachine {
    public RockCrusherMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, GTMachines.defaultTankSizeFunction, args);
    }

    @Override
    public boolean shouldWeatherOrTerrainExplosion() {
        return false;
    }
}
