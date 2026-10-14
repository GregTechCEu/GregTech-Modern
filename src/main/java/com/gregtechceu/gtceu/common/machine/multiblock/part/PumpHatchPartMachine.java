package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PumpHatchPartMachine extends FluidHatchPartMachine {

    public PumpHatchPartMachine(BlockEntityCreationInfo info) {
        super(info, 0, IO.OUT, new NotifiableFluidTank(1, FluidType.BUCKET_VOLUME, IO.OUT));
        tank.setFilter(fluidStack -> fluidStack.getFluid().is(GTMaterials.Water.getFluidTag()));
    }

    // By returning false here, we don't allow shift-clicking
    // with a screwdriver to swap the IO.
    @Override
    public boolean swapIO() {
        return false;
    }
}
