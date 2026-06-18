package com.gregtechceu.gtceu.api.cover.filter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class Filter<T> implements Predicate<T> {

    @Setter
    protected Consumer<Filter<T>> itemWriter;
    protected Consumer<Filter<T>> onUpdated = filter -> itemWriter.accept(filter);

    @Getter
    protected final ItemStack filterItemStack;

    public Filter(ItemStack stack) {
        this.filterItemStack = stack;
        itemWriter = filter -> stack.setTag(filter.saveFilter());
    }

    /**
     * @return Filter panel when opened by itself (including the player inventory)
     */
    public abstract ModularPanel<?> getPanel(GuiData data, PanelSyncManager syncManager, UISettings settings);

    public abstract Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings);

    @Nullable
    protected abstract CompoundTag saveFilter();

    public void setOnUpdated(Consumer<Filter<T>> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }

    public boolean isBlackList() {
        return false;
    }

    /**
     * @return Whether this filter supports querying for exact content amounts.
     */
    public boolean supportsAmounts() {
        return !isBlackList();
    }

    /**
     * Tests if the given stack matches this filter.
     * 
     * @return If the given stack matches this filter.
     */
    @Override
    public abstract boolean test(T t);

    /**
     * Retrieves the configured count for the stack.
     *
     * @return The amount configured for the stack.<br>
     *         If the stack is not matched by this filter, 0 is returned instead.
     */
    public abstract int testAmount(T stack);
}
