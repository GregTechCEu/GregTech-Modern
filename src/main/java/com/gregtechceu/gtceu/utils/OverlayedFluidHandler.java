package com.gregtechceu.gtceu.utils;

import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Simulates consecutive fills to {@link IFluidTransfer} instance.
 */
public class OverlayedFluidHandler {

    private final List<OverlayedTank> overlayedTanks;

    public OverlayedFluidHandler(@NotNull FluidTransferList tank) {
        this.overlayedTanks = new ArrayList<>();
        FluidStack[] entries = IntStream.range(0, tank.getTanks()).mapToObj(tank::getFluidInTank)
                .toArray(FluidStack[]::new);
        for (int i = 0; i < tank.getTanks(); ++i) {
            FluidStorage storage = new FluidStorage(tank.getTankCapacity(i));
            storage.setFluid(entries[i]);
            this.overlayedTanks.add(new OverlayedTank(storage, tank.isFluidValid(i, entries[i])));
        }
    }

    /**
     * Resets the internal state back to the state when the handler was
     * first mirrored.
     */
    public void reset() {
        for (OverlayedTank overlayedTank : this.overlayedTanks) {
            overlayedTank.reset();
        }
    }

    /**
     * Simulate fluid insertion to the fluid tanks.
     *
     * @param fluid          Fluid
     * @param amountToInsert Amount of the fluid to insert
     * @return Amount of fluid inserted into tanks
     */
    public long insertFluid(@NotNull FluidStack fluid, long amountToInsert) {
        if (amountToInsert <= 0) {
            return 0;
        }
        long totalInserted = 0;
        // flag value indicating whether the fluid was stored in 'distinct' slot at least once
        boolean distinctFillPerformed = false;

        // search for tanks with same fluid type first
        for (OverlayedTank overlayedTank : this.overlayedTanks) {
            // if the fluid to insert matches the tank, insert the fluid
            if (!overlayedTank.isEmpty() && overlayedTank.fluid != null && fluid.isFluidEqual(overlayedTank.fluid)) {
                long inserted = overlayedTank.tryInsert(fluid, amountToInsert);
                if (inserted > 0) {
                    totalInserted += inserted;
                    amountToInsert -= inserted;
                    if (amountToInsert <= 0) {
                        return totalInserted;
                    }
                }
                // regardless of whether the insertion succeeded, presence of identical fluid in
                // a slot prevents distinct fill to other slots
                if (!overlayedTank.allowSameFluidFill) {
                    distinctFillPerformed = true;
                }
            }
        }
        // if we still have fluid to insert, loop through empty tanks until we find one that can accept the fluid
        for (OverlayedTank overlayedTank : this.overlayedTanks) {
            // if the tank uses distinct fluid fill (allowSameFluidFill disabled) and another distinct tank had
            // received the fluid, skip this tank
            if ((!distinctFillPerformed || overlayedTank.allowSameFluidFill) &&
                    overlayedTank.isEmpty() &&
                    overlayedTank.property.isFluidValid(fluid)) {
                long inserted = overlayedTank.tryInsert(fluid, amountToInsert);
                if (inserted > 0) {
                    totalInserted += inserted;
                    amountToInsert -= inserted;
                    if (amountToInsert <= 0) {
                        return totalInserted;
                    }
                    if (!overlayedTank.allowSameFluidFill) {
                        distinctFillPerformed = true;
                    }
                }
            }
        }
        // return the amount of fluid that was inserted
        return totalInserted;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean lineBreak) {
        StringBuilder stb = new StringBuilder("OverlayedFluidHandler[").append(this.overlayedTanks.size()).append(";");
        if (lineBreak) stb.append("\n  ");
        for (int i = 0; i < this.overlayedTanks.size(); i++) {
            if (i != 0) stb.append(',');
            if (lineBreak) stb.append("\n  ");

            OverlayedTank overlayedTank = this.overlayedTanks.get(i);
            FluidStack fluid = overlayedTank.fluid;
            if (fluid.isEmpty()) {
                stb.append("None 0 / ").append(overlayedTank.property.getCapacity());
            } else {
                stb.append(FluidHelper.getDisplayName(fluid)).append(' ').append(fluid.getAmount())
                        .append(" / ").append(overlayedTank.property.getCapacity());
            }
        }
        if (lineBreak) stb.append('\n');
        return stb.append(']').toString();
    }

    private static class OverlayedTank {

        private final IFluidStorage property;
        private final boolean allowSameFluidFill;

        private FluidStack fluid;

        OverlayedTank(@NotNull IFluidStorage property, boolean allowSameFluidFill) {
            this.property = property;
            this.allowSameFluidFill = allowSameFluidFill;
            reset();
        }

        public boolean isEmpty() {
            return fluid == FluidStack.empty() || fluid.getAmount() <= 0;
        }

        /**
         * Tries to insert set amount of fluid into this tank. If operation succeeds,
         * the content of this tank will be updated.
         * <b>
         * Note that this method does not check preexisting fluids for insertion.
         *
         * @param fluid  Fluid
         * @param amount Amount of the fluid to insert
         * @return Amount of fluid inserted into this tank
         */
        public long tryInsert(@NotNull FluidStack fluid, long amount) {
            if (this.fluid.isEmpty()) {
                this.fluid = fluid.copy();
                this.fluid.setAmount(Math.min(this.property.getCapacity(), amount));
                return this.fluid.getAmount();
            } else {
                long maxInsert = Math.min(this.property.getCapacity() - this.fluid.getAmount(), amount);
                if (maxInsert > 0) {
                    this.fluid.setAmount(this.fluid.getAmount() + maxInsert);
                    return maxInsert;
                } else return 0;
            }
        }

        public void reset() {
            FluidStack fluid = this.property.getFluid();
            this.fluid = fluid != FluidStack.empty() ? fluid.copy() : FluidStack.empty();
        }
    }
}
