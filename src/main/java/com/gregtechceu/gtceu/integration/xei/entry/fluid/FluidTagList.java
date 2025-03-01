package com.gregtechceu.gtceu.integration.xei.entry.fluid;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class FluidTagList implements FluidEntryList {

    @Getter
    private final List<FluidTagEntry> entries = new ArrayList<>();

    public static FluidTagList of(@NotNull TagKey<Fluid> tag, int amount, @Nullable CompoundTag nbt) {
        var list = new FluidTagList();
        list.add(tag, amount, nbt);
        return list;
    }

    public void add(FluidTagEntry entry) {
        entries.add(entry);
    }

    public void add(@NotNull TagKey<Fluid> tag, int amount, @Nullable CompoundTag nbt) {
        add(new FluidTagEntry(tag, amount, nbt));
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public List<FluidStack> getStacks() {
        return entries.stream()
                .flatMap(FluidTagEntry::stacks)
                .toList();
    }

    public record FluidTagEntry(@NotNull TagKey<Fluid> tag, int amount, @Nullable CompoundTag nbt) {

        public Stream<FluidStack> stacks() {
            return BuiltInRegistries.FLUID.getTag(tag).map(HolderSet.ListBacked::stream).orElseGet(Stream::empty)
                    .map(holder -> new FluidStack(holder.get(), amount, nbt));
        }
    }
}
