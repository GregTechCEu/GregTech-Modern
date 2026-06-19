package com.gregtechceu.gtceu.api.cover.filter;

import brachy.modularui.widgets.Dialog;
import brachy.modularui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import net.minecraft.core.registries.BuiltInRegistries;
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

    public void setOnUpdated(Consumer<Filter<T>> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }

    /**
     * @return Filter panel when opened by itself (including the player inventory)
     */
    @SuppressWarnings("deprecation")
    public ModularPanel<?> getPanel(GuiData data, PanelSyncManager syncManager, UISettings settings, boolean displayPlayerInventory) {
        return new Dialog<>(BuiltInRegistries.ITEM.getKey(filterItemStack.getItem()).toString())
                .disablePanelsBelow(false)
                .draggable(true)
                .closeOnOutOfBoundsClick(false)
                .child(GTMuiWidgets.createTitleBar(() -> filterItemStack, 176, GTGuiTextures.BACKGROUND))
                .child(getFilterUI(data, syncManager, settings).top(10))
                .childIf(displayPlayerInventory, () -> SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    /**
     * @return The filter UI.
     */
    public abstract Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings);

    @Nullable
    protected abstract CompoundTag saveFilter();

    /**
     * @return Whether this filter supports querying for exact content amounts.
     */
    public abstract boolean supportsAmounts();

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
