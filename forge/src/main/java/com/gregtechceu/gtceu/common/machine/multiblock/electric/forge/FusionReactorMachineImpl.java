package com.gregtechceu.gtceu.common.machine.multiblock.electric.forge;

import com.lowdragmc.lowdraglib.LDLib;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class FusionReactorMachineImpl {

    public static int getFluidColor(FluidState fluid) {
        if (LDLib.isClient()) {
            return IClientFluidTypeExtensions.of(fluid).getTintColor();
        }
        return -1;
    }
}
