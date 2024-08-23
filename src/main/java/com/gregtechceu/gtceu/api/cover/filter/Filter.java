package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.cover.filter.MatchResult;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;

import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/3/14
 * @implNote Filter
 */
public interface Filter<T, S extends Filter<T, S>> extends Predicate<T>, Function<T, MatchResult> {

    WidgetGroup openConfigurator(int x, int y);

    void loadFilter(CompoundTag tag);

    CompoundTag saveFilter();

    void setOnUpdated(Consumer<S> onUpdated);

    default boolean isBlackList() {
        return false;
    }

    default void setBlackList(boolean blackList) {}

    int getMaxTransferSize();

    void setMaxTransferSize(int maxTransferSize);

    default int getTransferLimit(int slot, int transferSize) {
        return transferSize;
    }

    default int getTransferLimit(int slot) {
        return getMaxTransferSize();
    }

    default int getTransferLimit(T stack, int transferSize) {
        return 0;
    }

    default int getTransferLimit(T stack) {
        return 0;
    }

    default boolean testGeneric(Object test) {
        return this.test((T) test);
    }

    default MatchResult match(T test) {
        return this.apply(test);
    }

    default MatchResult matchGeneric(Object test) {
        return this.apply((T) test);
    }

    @RequiredArgsConstructor
    enum FilterType implements StringRepresentable {

        ITEM("item", SimpleItemFilter::new),
        ITEM_TAG("item_tag", TagItemFilter::new),
        FLUID("fluid", SimpleFluidFilter::new),
        FLUID_TAG("fluid_tag", TagFluidFilter::new);

        private final String name;
        private final Supplier<Filter<?, ?>> constructor;

        @Override
        public String getSerializedName() {
            return name;
        }

        public static FilterType getByName(String name) {
            return switch (name) {
                case "item" -> FilterType.ITEM;
                case "item_tag" -> FilterType.ITEM_TAG;
                case "fluid" -> FilterType.FLUID;
                case "fluid_tag" -> FilterType.FLUID_TAG;
                default -> throw new IllegalStateException("Unexpected value: " + name);
            };
        }

        public static Filter<?, ?> makeNew(FilterType filterType) {
            return filterType.constructor.get();
        }

        public static Filter<?, ?> makeNew(String filterType) {
            return getByName(filterType).constructor.get();
        }
    }
}
