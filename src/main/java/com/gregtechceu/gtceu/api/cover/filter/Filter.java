package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.Dialog;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class Filter<T> implements Predicate<T> {

    @Setter
    private Consumer<Filter<T>> onUpdated = $ -> {};

    @Getter
    protected final ItemStack filterItemStack;

    public Filter(ItemStack stack) {
        this.filterItemStack = stack;
    }

    /**
     * @return Filter panel when opened by itself (including the player inventory)
     */
    @SuppressWarnings("deprecation")
    public ModularPanel<?> getPanel(GuiData data, PanelSyncManager syncManager, UISettings settings,
                                    boolean displayPlayerInventory) {
        return new Dialog<>(BuiltInRegistries.ITEM.getKey(filterItemStack.getItem()).toString())
                .disablePanelsBelow(false)
                .draggable(true)
                .closeOnOutOfBoundsClick(false)
                .child(GTMuiWidgets.createTitleBar(() -> filterItemStack, 176, GTGuiTextures.BACKGROUND))
                .child(getFilterUI(data, syncManager, settings).top(10))
                .childIf(displayPlayerInventory, () -> SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    /**
     * Writes this filter to the filter item's NBT and calls the {@link #onUpdated} listener.
     */
    public void updateAndSaveFilter() {
        filterItemStack.setTag(writeFilterNBT());
        onUpdated.accept(this);
    }

    /**
     * @return The filter UI.
     */
    public abstract Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings);

    /**
     * Writes this filter's data to a tag
     * 
     * @return The data tag.
     */
    @Nullable
    protected abstract CompoundTag writeFilterNBT();

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
     * Retrieves the given amount that matches the filter for the stack. {@link #supportsAmounts()} should be checked
     * before calling this.
     *
     * @return The exact amount that matches the filter.<br>
     *         If the stack is not matched by this filter, 0 is returned instead.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int testAmount(T stack) {
        throw new NotImplementedException("This filter does not support testing amounts.");
    }
}
