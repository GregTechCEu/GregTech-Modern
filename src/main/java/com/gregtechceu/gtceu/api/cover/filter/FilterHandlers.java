package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.sync_system.ISyncManaged;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface FilterHandlers {

    static FilterHandler<ItemStack, ItemFilter> item(ISyncManaged container) {
        return new FilterHandler<>(container) {

            @Override
            public ItemFilter loadFilter(ItemStack filterItem) {
                return ItemFilter.loadFilter(filterItem);
            }

            @Override
            protected ItemFilter getEmptyFilter() {
                return ItemFilter.EMPTY;
            }

            @Override
            public boolean canInsertFilterItem(ItemStack itemStack) {
                return ItemFilter.FILTERS.containsKey(itemStack.getItem());
            }
        };
    }

    static FilterHandler<FluidStack, FluidFilter> fluid(ISyncManaged container) {
        return new FilterHandler<>(container) {

            @Override
            public FluidFilter loadFilter(ItemStack filterItem) {
                return FluidFilter.loadFilter(filterItem);
            }

            @Override
            protected FluidFilter getEmptyFilter() {
                return FluidFilter.EMPTY;
            }

            @Override
            public boolean canInsertFilterItem(ItemStack itemStack) {
                return FluidFilter.FILTERS.containsKey(itemStack.getItem());
            }
        };
    }
}
