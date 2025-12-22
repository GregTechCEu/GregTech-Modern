package com.gregtechceu.gtceu.integration.xei.entry.fluid;

import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public sealed interface FluidEntryList permits FluidStackList, FluidTagList, FluidHolderSetList {

    List<FluidStack> getStacks();

    boolean isEmpty();
}
