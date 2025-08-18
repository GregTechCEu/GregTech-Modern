package com.gregtechceu.gtceu.integration.xei.entry.fluid;

import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class FluidHolderSetList implements FluidEntryList {

    @Getter
    private final List<FluidHolderSetEntry> entries = new ArrayList<>();

    public static FluidHolderSetList of(@NotNull HolderSet<Fluid> set, int amount, @Nullable CompoundTag nbt) {
        var list = new FluidHolderSetList();
        list.add(set, amount, nbt);
        return list;
    }

    public void add(FluidHolderSetEntry entry) {
        entries.add(entry);
    }

    public void add(@NotNull HolderSet<Fluid> set, int amount, @Nullable CompoundTag nbt) {
        add(new FluidHolderSetEntry(set, amount, nbt));
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public List<FluidStack> getStacks() {
        return entries.stream()
                .flatMap(FluidHolderSetEntry::stacks)
                .toList();
    }

    public record FluidHolderSetEntry(@NotNull HolderSet<Fluid> set, int amount, @Nullable CompoundTag nbt) {

        public Stream<FluidStack> stacks() {
            return set.stream().map(holder -> new FluidStack(holder.get(), amount, nbt));
        }
    }
}
