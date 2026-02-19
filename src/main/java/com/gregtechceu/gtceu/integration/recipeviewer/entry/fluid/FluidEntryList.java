package com.gregtechceu.gtceu.integration.recipeviewer.entry.fluid;

import com.gregtechceu.gtceu.integration.recipeviewer.entry.EntryList;

import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public sealed interface FluidEntryList extends EntryList<FluidStack>
                                       permits FluidStackList, FluidTagList, FluidHolderSetList {

    List<FluidStack> getStacks();

    boolean isEmpty();
}
