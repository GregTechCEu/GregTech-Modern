package com.gregtechceu.gtceu.common.machine.multiblock.electric.fabric;

import com.lowdragmc.lowdraglib.LDLib;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.world.level.material.FluidState;

public class FusionReactorMachineImpl {

    public static int getFluidColor(FluidState fluid) {
        if (LDLib.isClient()) {
            return FluidRenderHandlerRegistry.INSTANCE.get(fluid.getType()).getFluidColor(null, null, fluid);
        }
        return -1;
    }
}
