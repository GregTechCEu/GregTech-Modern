package com.gregtechceu.gtceu.api.cover.filter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Filters {

    private static final Map<Item, FilterEntry<?>> FILTERS = new Object2ObjectOpenHashMap<>();

    public static <T> void registerFilter(Class<T> filterable, Item item,
                                          Function<ItemStack, Filter<T>> filterFactory) {
        FILTERS.put(item, new FilterEntry<>(filterable, filterFactory));
    }

    @SuppressWarnings({ "unchecked", "DataFlowIssue" })
    public static <T> Filter<T> loadFilter(Class<T> filterableType, ItemStack stack) {
        var entry = FILTERS.get(stack.getItem());
        if (entry.filterable != filterableType) return null;
        return (Filter<T>) entry.filterFactory.apply(stack);
    }

    public static Filter<ItemStack> loadItemFilter(ItemStack stack) {
        return loadFilter(ItemStack.class, stack);
    }

    public static Filter<FluidStack> loadFluidFilter(ItemStack stack) {
        return loadFilter(FluidStack.class, stack);
    }

    public static boolean isValidFilter(Class<?> filterableType, Item item) {
        if (!FILTERS.containsKey(item)) return false;
        return FILTERS.get(item).filterable == filterableType;
    }

    public static final Filter<ItemStack> EMPTY_ITEM = new Filter<>(ItemStack.EMPTY) {

        @Override
        public int testAmount(ItemStack itemStack) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean test(ItemStack itemStack) {
            return true;
        }

        @Override
        public ModularPanel<?> getPanel(GuiData data, PanelSyncManager syncManager, UISettings settings) {
            return null;
        }

        @Override
        public Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
            return null;
        }

        @Override
        public CompoundTag saveFilter() {
            throw new NotImplementedException("Not available for empty item filter");
        }

        @Override
        public void setOnUpdated(Consumer<Filter<ItemStack>> onUpdated) {
            throw new NotImplementedException("Not available for empty item filter");
        }
    };

    public static final Filter<FluidStack> EMPTY_FLUID = new Filter<>(ItemStack.EMPTY) {

        @Override
        public boolean test(FluidStack fluidStack) {
            return true;
        }

        @Override
        public int testAmount(FluidStack fluidStack) {
            return Integer.MAX_VALUE;
        }

        @Override
        public ModularPanel<?> getPanel(GuiData data, PanelSyncManager syncManager, UISettings settings) {
            return null;
        }

        @Override
        public Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
            return null;
        }

        @Override
        public CompoundTag saveFilter() {
            throw new NotImplementedException("Not available for empty fluid filter");
        }

        @Override
        public void setOnUpdated(Consumer<Filter<FluidStack>> onUpdated) {
            throw new NotImplementedException("Not available for empty fluid filter");
        }
    };

    /**
     * @param filterable The object/stack type that this filter supports.
     */
    private record FilterEntry<T>(Class<T> filterable, Function<ItemStack, Filter<T>> filterFactory) {}
}
