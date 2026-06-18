package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.sync_system.managed.ISyncManaged;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface FilterHandlers {

    static FilterHandler<ItemStack, Filter<ItemStack>> item(ISyncManaged container) {
        return new FilterHandler<>(container) {

            @Override
            public Filter<ItemStack> loadFilter(ItemStack filterItem) {
                return Filters.loadItemFilter(filterItem);
            }

            @Override
            protected Filter<ItemStack> getEmptyFilter() {
                return Filters.EMPTY_ITEM;
            }

            @Override
            public boolean canInsertFilterItem(ItemStack itemStack) {
                return Filters.isValidFilter(ItemStack.class, itemStack.getItem());
            }
        };
    }

    static FilterHandler<FluidStack, Filter<FluidStack>> fluid(ISyncManaged container) {
        return new FilterHandler<>(container) {

            @Override
            public Filter<FluidStack> loadFilter(ItemStack filterItem) {
                return Filters.loadFluidFilter(filterItem);
            }

            @Override
            protected Filter<FluidStack> getEmptyFilter() {
                return Filters.EMPTY_FLUID;
            }

            @Override
            public boolean canInsertFilterItem(ItemStack itemStack) {
                return Filters.isValidFilter(FluidStack.class, itemStack.getItem());
            }
        };
    }
}
