package com.gregtechceu.gtceu.api.cover.filter;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/3/14
 * @implNote Filter
 */
public interface Filter<T, S extends Filter<T, S>> extends Predicate<T> {

    WidgetGroup openConfigurator(int x, int y);

    void loadFilter(CompoundTag tag);

    CompoundTag saveFilter();

    void setOnUpdated(Consumer<S> onUpdated);

    default boolean isBlackList() {
        return false;
    }

    default void setBlackList(boolean blackList) {}

    default boolean testGeneric(Object test) {
        return this.test((T) test);
    }

    @RequiredArgsConstructor
    enum FilterType implements StringRepresentable {
        ITEM("item", SimpleItemFilter::new),
        ITEM_TAG("item_tag", TagItemFilter::new),
        FLUID("fluid", SimpleFluidFilter::new),
        FLUID_TAG("fluid_tag", TagFluidFilter::new)
        ;

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
