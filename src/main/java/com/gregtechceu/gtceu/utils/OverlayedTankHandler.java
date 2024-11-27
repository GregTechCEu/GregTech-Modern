package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;

import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Simulates consecutive fills to multiple {@link NotifiableFluidTank} instances
 */
public class OverlayedTankHandler {

    private final List<OverlayedTank> overlayedTanks;

    public OverlayedTankHandler(List<NotifiableFluidTank> tanks) {
        overlayedTanks = new ArrayList<>(tanks.size());
        var copy = new ArrayList<>(tanks);
        copy.sort(IRecipeHandler.ENTRY_COMPARATOR);
        for (var tank : copy) {
            overlayedTanks.add(new OverlayedTank(tank));
        }
    }

    /**
     * Resets the internal state of the handler to when it was constructed
     */
    public void reset() {
        for (var tank : overlayedTanks) tank.reset();
    }

    /**
     * Simulate fluid filling to the tanks
     * 
     * @param fluid  {@link FluidStack} with the fluid to attempt to 'fill' - stack amount does not matter
     * @param amount Actual amount of fluid to attempt to 'fill'
     * @return Amount of fluid that could potentially be filled into these tanks
     */
    public int tryFill(FluidStack fluid, int amount) {
        if (amount <= 0) return 0;
        int total = 0;
        for (var tank : overlayedTanks) {
            int filled = tank.fill(fluid, amount);
            total += filled;
            amount -= filled;
            if (amount <= 0) return total;
        }

        return total;
    }

    /**
     * Represents a single {@link NotifiableFluidTank} and its storages
     */
    private static class OverlayedTank {

        private final int size;
        private final int capacity;
        private final boolean sameFluidFill;
        private final Predicate<FluidStack> filter;
        private final List<FluidStack> originalStacks;

        private List<FluidStack> stacks;

        /**
         * Constructs the Overlayed Tank from the given NotifiableFluidTank <br>
         * Stores properties, such as:
         * <ul>
         * <li>The number of tanks via {@link NotifiableFluidTank#getTanks}</li>
         * <li>The tank's capacity per storage via {@link NotifiableFluidTank#getTankCapacity}</li>
         * <li>Whether the tank is allowed be filled with duplicate fluids</li>
         * <li>The tank's filter</li>
         * <li>The tank's currently stored FluidStacks</li>
         * </ul>
         */
        private OverlayedTank(NotifiableFluidTank tank) {
            size = tank.getTanks();
            capacity = tank.getTankCapacity(0);
            sameFluidFill = tank.isAllowSameFluids();
            filter = tank.getFilter();
            originalStacks = new ArrayList<>(tank.getStorages().length);
            for (var storage : tank.getStorages()) {
                if (!storage.getFluid().isEmpty())
                    originalStacks.add(storage.getFluid());
            }
            reset();
        }

        /**
         * Resets the Overlayed Tank back to its original state
         */
        public void reset() {
            stacks = new ArrayList<>(size);
            for (var stack : originalStacks) stacks.add(stack.copy());
        }

        /**
         * 'Fill' this Overlayed Tank as much as is allowed
         * 
         * @param fluid  FluidStack with the fluid to attempt to 'fill'; stack amount does not matter
         * @param amount Actual amount of fluid to attempt to 'fill'
         * @return Amount of fluid that could potentially be filled into this tank
         */
        public int fill(FluidStack fluid, int amount) {
            if (!filter.test(fluid) || capacity <= 0) return 0;
            int filled = tryFill(fluid, amount);
            if (!sameFluidFill || filled >= amount) return filled;

            int total = filled;
            amount -= filled;
            for (int i = 1; i < size; ++i) { // Attempt to 'fill' tanks a total of (size) times
                filled = tryFill(fluid, amount);
                total += filled;
                amount -= filled;
                if (amount <= 0) return total;
            }
            return total;
        }

        private int tryFill(FluidStack fluid, int amount) {
            var existing = search(fluid);
            if (existing.isEmpty() || existing.getAmount() >= capacity) { // Need to add new stack
                if (!existing.isEmpty() && !sameFluidFill) return 0;  // Not allowed to add new stack
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

        /**
         * Searches {@link OverlayedTank#stacks} for a FluidStack equivalent to the passed {@code fluid} <br>
         * 
         * @param fluid A FluidStack with the fluid to search for
         * @return If {@code sameFluidFill} is false, then the first matching stack found. Otherwise, the first non-full
         *         stack. If no matching stack is found, then {@link FluidStack#EMPTY}
         */
        private FluidStack search(FluidStack fluid) {
            FluidStack found = FluidStack.EMPTY;
            for (var stack : stacks) {
                if (stack.isFluidEqual(fluid)) {
                    if (!sameFluidFill || stack.getAmount() < capacity) return stack;
                    else found = stack;
                }
            }
            return found;
        }
    }
}
