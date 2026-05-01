package com.gregtechceu.gtceu.api.cover.filter;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Filter<T, S extends Filter<T, S>> extends Predicate<T> {

    Widget openConfigurator(int x, int y);

    void setOnUpdated(Consumer<S> onUpdated);

    default boolean isBlackList() {
        return false;
    }

    default boolean isBlank() {
        return false;
    }
}
