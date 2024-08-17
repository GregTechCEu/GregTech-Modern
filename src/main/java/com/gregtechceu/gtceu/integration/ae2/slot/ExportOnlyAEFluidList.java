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

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ExportOnlyAEFluidList extends NotifiableFluidTank implements IConfigurableSlotList {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ExportOnlyAEFluidList.class, NotifiableFluidTank.MANAGED_FIELD_HOLDER);

    @Getter
    @Persisted
    protected ExportOnlyAEFluidSlot[] inventory;
    private FluidStorage[] fluidStorages;

    public ExportOnlyAEFluidList(MetaMachine machine, int slots) {
        this(machine, slots, ExportOnlyAEFluidSlot::new);
    }

    public ExportOnlyAEFluidList(MetaMachine machine, int slots, Supplier<ExportOnlyAEFluidSlot> slotFactory) {
        super(machine, slots, 0, IO.IN);
        this.inventory = new ExportOnlyAEFluidSlot[slots];
        for (int i = 0; i < slots; i++) {
            this.inventory[i] = slotFactory.get();
            this.inventory[i].setOnContentsChanged(this::onContentsChanged);
        }
    }

    @Override
    public FluidStorage[] getStorages() {
        if (this.fluidStorages == null) {
            this.fluidStorages = Arrays.stream(this.inventory)
                    .map(FluidStorageDelegate::new).toArray(FluidStorage[]::new);
        }
        return this.fluidStorages;
    }

    @Override
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        return 0;
    }

    @Override
    public boolean supportsFill(int tank) {
        return false;
    }

    @Override
    public FluidStack drainInternal(long maxDrain, boolean simulate) {
        if (maxDrain == 0) {
            return FluidStack.empty();
        }
        FluidStack totalDrained = null;
        for (var tank : inventory) {
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

    @Override
    public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                   @Nullable String slotName, boolean simulate) {
        return handleIngredient(io, recipe, left, simulate, this.handlerIO, getStorages());
    }

    @Override
    public IConfigurableSlot getConfigurableSlot(int index) {
        return inventory[index];
    }

    @Override
    public int getConfigurableSlots() {
        return inventory.length;
    }

    public boolean isAutoPull() {
        return false;
    }

    public boolean isStocking() {
        return false;
    }

    public boolean ownsSlot(ExportOnlyAEFluidSlot testSlot) {
        for (var tank : inventory) {
            if (tank == testSlot) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    @NotNull
    @Override
    public Object createSnapshot() {
        return Arrays.stream(inventory).map(IFluidTransfer::createSnapshot).toArray(Object[]::new);
    }

    @Deprecated
    @Override
    public void restoreFromSnapshot(Object snapshot) {
        if (snapshot instanceof Object[] array && array.length == inventory.length) {
            for (int i = 0; i < array.length; i++) {
                inventory[i].restoreFromSnapshot(array[i]);
            }
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private static class FluidStorageDelegate extends FluidStorage {

        private final ExportOnlyAEFluidSlot fluid;

        public FluidStorageDelegate(ExportOnlyAEFluidSlot fluid) {
            super(0);
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
            return 0;
        }

        @Override
        public boolean supportsFill(int tank) {
            return false;
        }

        @Override
        public FluidStorage copy() {
            // because recipe testing uses copy storage instead of simulated operations
            return new FluidStorageDelegate(fluid) {

                @NotNull
                @Override
                public FluidStack drain(FluidStack maxDrain, boolean simulate, boolean notifyChanges) {
                    return super.drain(maxDrain, true, notifyChanges);
                }
            };
        }
    }
}
