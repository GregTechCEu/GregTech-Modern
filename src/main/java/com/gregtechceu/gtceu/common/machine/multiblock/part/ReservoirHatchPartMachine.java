package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ReservoirHatchPartMachine extends FluidHatchPartMachine {

    protected InfiniteWaterTank waterTank;

    public static final int FLUID_AMOUNT = 2_000_000_000;

    public ReservoirHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, GTValues.EV, IO.IN, FLUID_AMOUNT, 1, args);
    }

    //////////////////////////////////
    // ****** Initialization ****** //
    //////////////////////////////////

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        this.waterTank = new InfiniteWaterTank(initialCapacity);
        // allow both importing and exporting from the tank
        return new NotifiableFluidTank(this, Collections.singletonList(waterTank), io, IO.BOTH);
    }

    //////////////////////////////////
    // ******** Fill Water ******** //
    //////////////////////////////////

    @Override
    protected void updateTankSubscription() {
        if (isWorkingEnabled() && !waterTank.isFull()) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    @Override
    protected void autoIO() {
        // replace with refilling water tank
        if (getOffsetTimer() % 20 == 0) {
            waterTank.refillWater();
            updateTankSubscription();
        }
    }

    // By returning false here, we don't allow shift-clicking
    // with a screwdriver to swap the IO.
    @Override
    public boolean swapIO() {
        return false;
    }

    protected static class InfiniteWaterTank extends CustomFluidTank {

        private static final CompoundTag EMPTY = new CompoundTag();
        private static final FluidStack WATER = new FluidStack(Fluids.WATER, Integer.MAX_VALUE);

        public InfiniteWaterTank(int capacity) {
            super(capacity);
            // start with the full amount
            setFluid(new FluidStack(Fluids.WATER, capacity));
        }

        public void refillWater() {
            // call super since our overrides don't allow any kind of filling
            super.fill(WATER, FluidAction.EXECUTE);
        }

        public boolean isFull() {
            return getFluidAmount() >= capacity;
        }

        @Override
        public boolean supportsFill(int tank) {
            // don't allow external callers to fill this tank
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            // don't allow external filling
            return 0;
        }

        @Override
        public CompoundTag serializeNBT() {
            // serialization is unnecessary here, we can always recreate it completely full since it would refill anyway
            return EMPTY;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {}
    }
}
