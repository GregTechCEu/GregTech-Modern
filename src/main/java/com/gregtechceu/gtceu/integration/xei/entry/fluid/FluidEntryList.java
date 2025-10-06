package com.gregtechceu.gtceu.integration.xei.entry.fluid;

import com.gregtechceu.gtceu.integration.xei.entry.EntryList;

import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public sealed interface FluidEntryList extends EntryList<FluidStack> permits FluidStackList, FluidTagList {

    List<FluidStack> getStacks();

    boolean isEmpty();
}
