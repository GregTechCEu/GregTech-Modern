package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

public class RockCrusherMachine extends SimpleTieredMachine {

    public RockCrusherMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, GTMachineUtils.defaultTankSizeFunction);
    }

    @Override
    public boolean shouldWeatherOrTerrainExplosion() {
        return false;
    }
}
