package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamHatchPartMachine extends FluidHatchPartMachine {

    public static final int INITIAL_TANK_CAPACITY = 64 * FluidType.BUCKET_VOLUME;

    public SteamHatchPartMachine(BlockEntityCreationInfo info) {
        super(info, 0, IO.IN, SteamHatchPartMachine.INITIAL_TANK_CAPACITY, 1);
        circuitSlot.setEnabled(false);
    }

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots) {
        return super.createTank(initialCapacity, slots)
                .setFilter(fluidStack -> fluidStack.getFluid().is(GTMaterials.Steam.getFluidTag()));
    }

    // By returning false here, we don't allow shift-clicking
    // with a screwdriver to swap the IO, since this is a
    // hatch that only allows steam in, not
    // a steam version of an input/output hatch
    @Override
    public boolean swapIO() {
        return false;
    }
}
