package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.recipe.IFilteredHandler;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import lombok.Getter;

public class LockableFluidTank extends NotifiableFluidTank {

    @SaveField
    @SyncToClient
    @Getter
    protected final CustomFluidTank lockedFluid = new CustomFluidTank(FluidType.BUCKET_VOLUME);

    public LockableFluidTank(int slots, int capacity, IO io) {
        super(slots, capacity, io);
        this.lockedFluid.setOnContentsChanged(this::onLockedFluidChanged);
    }

    @Override
    public boolean test(FluidIngredient ingredient) {
        return !this.isLocked() || ingredient.test(this.lockedFluid.getFluid());
    }

    @Override
    public int getPriority() {
        return !isLocked() || lockedFluid.getFluid().isEmpty() ? super.getPriority() :
                IFilteredHandler.HIGH - getTanks();
    }

    protected void onLockedFluidChanged() {
        syncDataHolder.markClientSyncFieldDirty("lockedFluid");
        var newFluid = this.lockedFluid.getFluid();
        if (newFluid.isEmpty()) {
            this.setFilter(stack -> true);
            this.onContentsChanged();
            return;
        }
        for (int i = 0; i < this.getTanks(); i++) {
            if (this.getFluidInTank(i).isEmpty()) continue;
            if (!this.getFluidInTank(i).isFluidEqual(newFluid)) {
                // Fluid in a tank that doesn't equal the new locked fluid
                this.lockedFluid.setFluid(FluidStack.EMPTY);
                return;
            }
        }
        this.setFilter(stack -> stack.isFluidEqual(newFluid));
        this.onContentsChanged();
    }

    public boolean isLocked() {
        return !lockedFluid.getFluid().isEmpty();
    }

    public void setLocked(boolean locked) {
        setLocked(locked, storages[0].getFluid());
    }

    public void setLocked(boolean locked, FluidStack fluidStack) {
        if (this.isLocked() == locked) return;
        if (locked && !fluidStack.isEmpty()) {
            this.lockedFluid.setFluid(fluidStack.copy());
            this.lockedFluid.getFluid().setAmount(1);
            setFilter(stack -> stack.isFluidEqual(this.lockedFluid.getFluid()));
        } else {
            this.lockedFluid.setFluid(FluidStack.EMPTY);
            setFilter(stack -> true);
        }
        syncDataHolder.markClientSyncFieldDirty("lockedFluid");
        onContentsChanged();
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        if (this.isLocked()) {
            setFilter(stack -> stack.isFluidEqual(this.lockedFluid.getFluid()));
        }
    }
}
