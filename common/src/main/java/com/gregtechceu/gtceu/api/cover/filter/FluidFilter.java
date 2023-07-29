package com.gregtechceu.gtceu.api.cover.filter;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/3/14
 * @implNote FluidFilter
 */
public interface FluidFilter extends Filter<FluidStack, FluidFilter> {

    Map<Item, Function<ItemStack, FluidFilter>> FILTERS = new HashMap<>();

    static FluidFilter loadFilter(ItemStack itemStack) {
        return FILTERS.get(itemStack.getItem()).apply(itemStack);
    }

    /**
     * An empty fluid filter that allows all fluids.<br>
     * ONLY TO BE USED FOR FLUID MATCHING! All other functionality will throw an exception.
     */
    FluidFilter EMPTY = new FluidFilter() {
        @Override
        public boolean test(FluidStack fluidStack) {
            return true;
        }

        @Override
        public WidgetGroup openConfigurator(int x, int y) {
            throw new NotImplementedException("Not available for empty fluid filter");
        }

        @Override
        public CompoundTag saveFilter() {
            throw new NotImplementedException("Not available for empty fluid filter");
        }

        @Override
        public void setOnUpdated(Consumer<FluidFilter> onUpdated) {
            throw new NotImplementedException("Not available for empty fluid filter");
        }
    };
}
