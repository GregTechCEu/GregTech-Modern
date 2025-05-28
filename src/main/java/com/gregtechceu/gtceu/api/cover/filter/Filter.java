package com.gregtechceu.gtceu.api.cover.filter;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Filter<T, S extends Filter<T, S>> extends Predicate<T> {

    WidgetGroup openConfigurator(int x, int y);

    CompoundTag saveFilter();

    void setOnUpdated(Consumer<S> onUpdated);

    default boolean isBlackList() {
        return false;
    }

    default boolean isBlank() {
        return false;
    }
}
