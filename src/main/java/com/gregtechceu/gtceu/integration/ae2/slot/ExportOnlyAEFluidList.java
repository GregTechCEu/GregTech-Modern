package com.gregtechceu.gtceu.integration.ae2.slot;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class ExportOnlyAEFluidList extends NotifiableFluidTank {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ExportOnlyAEFluidList.class, NotifiableFluidTank.MANAGED_FIELD_HOLDER);

    @Persisted
    public final ExportOnlyAEFluidSlot[] tanks;
    private FluidStorage[] fluidStorages;

    public ExportOnlyAEFluidList(MetaMachine machine, int slots, long capacity, IO io) {
        super(machine, slots, capacity, io);
        this.tanks = new ExportOnlyAEFluidSlot[slots];
        for (int i = 0; i < slots; i++) {
            this.tanks[i] = new ExportOnlyAEFluidSlot(null, null);
            this.tanks[i].setOnContentsChanged(this::onContentsChanged);
        }
        this.fluidStorages = null;
    }

    @Override
    public FluidStorage[] getStorages() {
        if (this.fluidStorages == null) {
            this.fluidStorages = Arrays.stream(this.tanks)
                    .map(tank -> new FluidStorageDelegate(tank.getCapacity(), tank)).toArray(FluidStorage[]::new);
            return this.fluidStorages;
        } else {
            return this.fluidStorages;
        }
    }

    @Override
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        return 0;
    }

    @Override
    public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                   @Nullable String slotName, boolean simulate) {
        return handleIngredient(io, recipe, left, simulate, this.handlerIO, getStorages());
    }

    public FluidStack drainInternal(long maxDrain, boolean simulate) {
        if (maxDrain == 0) {
            return FluidStack.empty();
        }
        FluidStack totalDrained = null;
        for (var tank : tanks) {
            if (totalDrained == null || totalDrained.isEmpty()) {
                totalDrained = tank.drain(maxDrain, simulate);
                if (totalDrained.isEmpty()) {
                    totalDrained = null;
                } else {
                    maxDrain -= totalDrained.getAmount();
                }
            } else {
                FluidStack copy = totalDrained.copy();
                copy.setAmount(maxDrain);
                FluidStack drain = tank.drain(copy, simulate);
                totalDrained.grow(drain.getAmount());
                maxDrain -= drain.getAmount();
            }
            if (maxDrain <= 0) break;
        }
        return totalDrained == null ? FluidStack.empty() : totalDrained;
    }

    @NotNull
    @Override
    public Object createSnapshot() {
        return Arrays.stream(tanks).map(IFluidTransfer::createSnapshot).toArray(Object[]::new);
    }

    @Override
    public void restoreFromSnapshot(Object snapshot) {
        if (snapshot instanceof Object[] array && array.length == tanks.length) {
            for (int i = 0; i < array.length; i++) {
                tanks[i].restoreFromSnapshot(array[i]);
            }
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private static class FluidStorageDelegate extends FluidStorage {

        private final ExportOnlyAEFluidSlot fluid;

        public FluidStorageDelegate(long capacity, ExportOnlyAEFluidSlot fluid) {
            super(capacity);
            this.fluid = fluid;
        }

        public FluidStorageDelegate(long capacity, Predicate<FluidStack> validator, ExportOnlyAEFluidSlot fluid) {
            super(capacity, validator);
            this.fluid = fluid;
        }

        @Override
        @NotNull
        public FluidStack getFluid() {
            return this.fluid.getFluid();
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack maxDrain, boolean simulate, boolean notifyChanges) {
            return fluid.drain(maxDrain, simulate, notifyChanges);
        }

        @Override
        public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChange) {
            return fluid.fill(tank, resource, simulate, notifyChange);
        }

        @Override
        public FluidStorage copy() {
            var storage = new FluidStorageDelegate(capacity, validator, this.fluid);
            storage.setFluid(super.fluid.copy());
            return storage;
        }
    }

}
