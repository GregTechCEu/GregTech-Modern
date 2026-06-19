package com.gregtechceu.gtceu.api.cover.filter;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CompositeFilter<T> extends Filter<T> {

    private Class<T> filterableType;

    @Getter
    protected ItemStack[] itemStacks = new ItemStack[9];

    @Getter
    @SuppressWarnings("unchecked")
    protected Filter<T>[] filters = (Filter<T>[])new Filter[9];

    public CompositeFilter(ItemStack stack, Class<T> filterableType) {
        super(stack);
        this.filterableType = filterableType;
    }

    @Override
    public Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }

    @Override
    protected @Nullable CompoundTag saveFilter() {
        return null;
    }

    @Override
    public boolean supportsAmounts() {
        return false;
    }

    @Override
    public boolean test(T t) {
        return false;
    }

    @Override
    public int testAmount(T stack) {
        return 0;
    }
}
