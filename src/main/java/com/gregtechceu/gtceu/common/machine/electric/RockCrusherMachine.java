package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public class RockCrusherMachine extends SimpleTieredMachine {

    public RockCrusherMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, GTMachineUtils.defaultTankSizeFunction, args);
    }

    @Override
    public boolean shouldWeatherOrTerrainExplosion() {
        return false;
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        getRecipeLogic().updateTickSubscription();
    }
}
