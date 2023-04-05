package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PumpHatchPartMachine extends FluidHatchPartMachine {

    public PumpHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, 0, IO.OUT, args);
    }

    @Override
    protected NotifiableFluidTank createTank(Object... args) {
        return super.createTank(args).setFilter(fluidStack -> fluidStack.getFluid() == GTMaterials.Water.getFluid());
    }

    @Override
    protected long getTankCapacity() {
        return FluidHelper.getBucket();
    }
}
