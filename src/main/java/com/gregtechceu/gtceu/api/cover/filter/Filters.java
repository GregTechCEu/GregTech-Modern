package com.gregtechceu.gtceu.api.cover.filter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
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
        if (entry.filterableType != filterableType) return null;
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
        return FILTERS.get(item).filterableType == filterableType;
    }

    public static <T> Filter<T> getEmptyFilter() {
        return new Filter<>(ItemStack.EMPTY) {

            @Override
            public Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
                throw new NotImplementedException("Cannot open UI for empty filter");
            }

            @Override
            protected @Nullable CompoundTag writeFilterNBT() {
                throw new NotImplementedException("Cannot save empty filter");
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
    private record FilterEntry<T>(Class<T> filterableType, Function<ItemStack, Filter<T>> filterFactory) {}
}
