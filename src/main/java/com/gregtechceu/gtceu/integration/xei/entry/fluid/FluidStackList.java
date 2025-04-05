package com.gregtechceu.gtceu.integration.xei.entry.fluid;

import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public final class FluidStackList implements FluidEntryList {

    private final List<FluidStack> stacks;

    public FluidStackList() {
        this.stacks = new ArrayList<>();
    }

    public static FluidStackList of(FluidStack stack) {
        var list = new FluidStackList();
        list.add(stack);
        return list;
    }

    public static FluidStackList of(Collection<FluidStack> coll) {
        var list = new FluidStackList();
        list.addAll(coll);
        return list;
    }

    public void add(FluidStack stack) {
        stacks.add(stack);
    }

    public void addAll(Collection<FluidStack> list) {
        stacks.addAll(list);
    }

    @Override
    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    @Override
    public List<FluidStack> getStacks() {
        return stacks;
    }

    public Stream<FluidStack> stream() {
        return stacks.stream();
    }
}
