package com.gregtechceu.gtceu.integration.ae2.slot;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

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
    private CustomFluidTank[] fluidStorages;

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
    public CustomFluidTank[] getStorages() {
        if (this.fluidStorages == null) {
            this.fluidStorages = Arrays.stream(this.inventory)
                    .map(FluidStorageDelegate::new).toArray(CustomFluidTank[]::new);
        }
        return this.fluidStorages;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public boolean supportsFill(int tank) {
        return false;
    }

    @Override
    public FluidStack drainInternal(int maxDrain, FluidAction action) {
        if (maxDrain == 0) {
            return FluidStack.EMPTY;
        }
        FluidStack totalDrained = null;
        for (var tank : inventory) {
            if (totalDrained == null || totalDrained.isEmpty()) {
                totalDrained = tank.drain(maxDrain, action);
                if (totalDrained.isEmpty()) {
                    totalDrained = null;
                } else {
                    maxDrain -= totalDrained.getAmount();
                }
            } else {
                FluidStack copy = totalDrained.copy();
                copy.setAmount(maxDrain);
                FluidStack drain = tank.drain(copy, action);
                totalDrained.grow(drain.getAmount());
                maxDrain -= drain.getAmount();
            }
            if (maxDrain <= 0) break;
        }
        return totalDrained == null ? FluidStack.EMPTY : totalDrained;
    }

    @Override
    public List<SizedFluidIngredient> handleRecipeInner(IO io, RecipeHolder<GTRecipe> recipe,
                                                        List<SizedFluidIngredient> left,
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

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private static class FluidStorageDelegate extends CustomFluidTank {

        private final ExportOnlyAEFluidSlot fluid;

        public FluidStorageDelegate(ExportOnlyAEFluidSlot fluid) {
            super(0);
            this.fluid = fluid;
        }

        @Override
        public FluidStack getFluid() {
            return this.fluid.getFluid();
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack maxDrain, FluidAction action) {
            return fluid.drain(maxDrain, action);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public boolean supportsFill(int tank) {
            return false;
        }

        @Override
        public CustomFluidTank copy() {
            // because recipe testing uses copy storage instead of simulated operations
            return new FluidStorageDelegate(fluid) {

                @NotNull
                @Override
                public FluidStack drain(FluidStack maxDrain, FluidAction action) {
                    return super.drain(maxDrain, FluidAction.SIMULATE);
                }
            };
        }
    }
}
