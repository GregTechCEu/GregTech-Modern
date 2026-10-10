package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.recipe.IFilteredHandler;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;

public class FilteredFluidTank extends NotifiableFluidTank {

    @SaveField
    @SyncToClient
    @Getter
    protected final FilterHandler<FluidStack> filterHandler;

    public FilteredFluidTank(int slots, int capacity, IO io) {
        super(slots, capacity, io);

        filterHandler = new FilterHandler<>(this, FluidStack.class);
        filterHandler.onFilterLoaded(f -> notifyListeners());
        filterHandler.onFilterRemoved(this::notifyListeners);
        setFilter(this.filterHandler::test);
    }

    @Override
    public int getPriority() {
        return !filterHandler.isFilterPresent() ? super.getPriority() :
                IFilteredHandler.HIGH - getTanks();
    }

    @Override
    public void onMachineDestroyed() {
        if (filterHandler.isFilterPresent())
            Block.popResource(getLevel(), getBlockPos(), filterHandler.getFilterItem());
        super.onMachineDestroyed();
    }
}
