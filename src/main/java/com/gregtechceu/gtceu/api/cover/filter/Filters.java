package com.gregtechceu.gtceu.api.cover.filter;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;
import java.util.function.Supplier;

public class Filters {

    private static final Map<Item, FilterEntry<?, ?>> FILTERS = new Object2ObjectOpenHashMap<>();

    public static <T, S extends Filter<T>> void registerFilter(Class<T> filterable, Item item,
                                                               Supplier<S> filterFactory,
                                                               Holder<DataComponentType<?>> dataComponentType) {
        FILTERS.put(item, new FilterEntry<>(filterable, filterFactory, dataComponentType));
    }

    @SuppressWarnings({ "unchecked", "DataFlowIssue" })
    public static <T> Filter<T> loadFilter(Class<T> filterableType, ItemStack stack) {
        var entry = FILTERS.get(stack.getItem());
        if (entry.filterableType != filterableType) return null;
        Filter<T> filter = (Filter<T>) stack.getOrDefault(entry.dataComponentType.value(), entry.filterFactory.get());
        filter.setFilterItemStack(stack);
        filter.setItemWriter(w -> stack.set((DataComponentType<? super Filter<T>>) entry.dataComponentType, w));
        return filter;
    }

    public static Filter<ItemStack> loadItemFilter(ItemStack stack) {
        return loadFilter(ItemStack.class, stack);
    }

    public static Filter<FluidStack> loadFluidFilter(ItemStack stack) {
        return loadFilter(FluidStack.class, stack);
    }

    public static boolean isValidFilter(Class<?> filterableType, Item item) {
        if (!FILTERS.containsKey(item)) return false;
        return FILTERS.get(item).filterableType == filterableType;
    }

    public static <T> Filter<T> getEmptyFilter() {
        return new Filter<>() {

            @Override
            public Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
                throw new NotImplementedException("Cannot open UI for empty filter");
            }

            @Override
            public boolean supportsAmounts() {
                return true;
            }

            @Override
            public boolean test(T t) {
                return true;
            }

            @Override
            public int testAmount(T stack) {
                return Integer.MAX_VALUE;
            }
        };
    }

    /**
     * @param filterableType The object/stack type that this filter supports.
     */
    private record FilterEntry<T, S extends Filter<T>>(Class<T> filterableType, Supplier<S> filterFactory,
                                                       Holder<DataComponentType<?>> dataComponentType) {}
}
