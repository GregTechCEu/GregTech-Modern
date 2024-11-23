package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;

import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class OverlayedTankHandler {

    private final List<OverlayedTank> tanks;

    public OverlayedTankHandler(List<NotifiableFluidTank> NFTs) {
        var copy = new ArrayList<>(NFTs);
        copy.sort(IRecipeHandler.ENTRY_COMPARATOR);
        tanks = new ArrayList<>(copy.size());
        for (var nft : copy) tanks.add(new OverlayedTank(nft));
    }

    public void reset() {
        for (var tank : tanks) tank.reset();
    }

    public int insertFluid(FluidStack fluid, int amount) {
        if (amount <= 0) return 0;
        int total = 0;
        for (var tank : tanks) {
            int filled = tank.tryFill(fluid, amount);
            if (filled > 0) {
                total += filled;
                amount -= filled;
                if (amount <= 0) return total;
            }
        }

        return total;
    }

    private static class OverlayedTank {

        private final int size;
        private final int capacity;
        private final boolean sameFluidFill;
        private final Predicate<FluidStack> filter;
        private final List<FluidStack> originalStacks;

        private List<FluidStack> stacks;

        OverlayedTank(NotifiableFluidTank tank) {
            sameFluidFill = tank.isAllowSameFluids();
            size = tank.getTanks();
            capacity = tank.getTankCapacity(0);
            filter = tank.getFilter();
            originalStacks = new ArrayList<>(tank.getStorages().length);
            for (var storage : tank.getStorages()) {
                if (!storage.getFluid().isEmpty())
                    originalStacks.add(storage.getFluid());
            }
            reset();
        }

        public int tryFill(FluidStack fluid, int amount) {
            if (!filter.test(fluid) || capacity <= 0) return 0;
            int filled = fill(fluid, amount);
            if (!sameFluidFill || filled >= amount) return filled;

            int total = filled;
            amount -= filled;
            for (int i = 1; i < size; ++i) { // Attempt to 'fill' tanks a total of (size) times
                filled = fill(fluid, amount);
                total += filled;
                amount -= filled;
                if (amount <= 0) return total;
            }
            return total;
        }

        private int fill(FluidStack fluid, int amount) {
            var existing = get(fluid);
            if (existing.isEmpty() || existing.getAmount() >= capacity) { // Need to add new stack
                if (!existing.isEmpty() && !sameFluidFill) return 0;  // Can't add new stack
                if (stacks.size() >= size) return 0;  // No space to add new stack
                int canInsert = Math.min(capacity, amount);
                stacks.add(new FluidStack(fluid, amount));
                return canInsert;
            } else { // Stack (that can grow) exists
                int canInsert = Math.min(capacity - existing.getAmount(), amount);
                existing.grow(canInsert);
                return canInsert;
            }
        }

        private FluidStack get(FluidStack fluid) {
            FluidStack found = FluidStack.EMPTY;
            for (var stack : stacks) {
                if (stack.isFluidEqual(fluid)) {
                    if (!sameFluidFill || stack.getAmount() < capacity) return stack;
                    else found = stack;
                }
            }
            return found;
        }

        public void reset() {
            stacks = new ArrayList<>(size);
            for (var stack : originalStacks) stacks.add(stack.copy());
        }
    }
}
