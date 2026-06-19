package com.gregtechceu.gtceu.api.cover.filter;

import brachy.modularui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ItemSlot;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class CompositeFilter<T> extends Filter<T> {

    private final Class<T> filterableType;

    private final CustomItemStackHandler itemStacks = new CustomItemStackHandler(9) {

        @Override
        public void onContentsChanged(int slot) {
            onFilterItemChanged(slot);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    @Getter
    @SuppressWarnings("unchecked")
    protected @Nullable Filter<T>[] filters = (Filter<T>[]) new Filter[9];

    public CompositeFilter(ItemStack stack, Class<T> filterableType) {
        super(stack);

        var tag = stack.getOrCreateTag();
        this.filterableType = filterableType;
        this.itemStacks.setFilter(s -> Filters.isValidFilter(filterableType, s.getItem()));

        if (tag.isEmpty()) return;
        itemStacks.deserializeNBT(tag.getCompound("filters"));

        for (int i=0; i<9; i++) {
            var item = itemStacks.getStackInSlot(i);
            if (item.isEmpty()) continue;
            filters[i] = Filters.loadFilter(filterableType, item);
            Objects.requireNonNull(filters[i]).setOnUpdated($ -> updateAndSaveFilter());
        }

    }

    @Override
    protected @Nullable CompoundTag writeFilterNBT() {
        if (itemStacks.toList().stream().allMatch(i -> i == ItemStack.EMPTY)) return null;

        var tag = new CompoundTag();
        tag.put("filters", itemStacks.serializeNBT());
        return tag;
    }

    private void onFilterItemChanged(int slot) {
        var newItem = itemStacks.getStackInSlot(slot);
        filters[slot] = null;
        if (!newItem.isEmpty()) {
            filters[slot] = Filters.loadFilter(filterableType, newItem);
            Objects.requireNonNull(filters[slot]).setOnUpdated($ -> updateAndSaveFilter());
        };

        updateAndSaveFilter();
    }

    @Override
    public boolean supportsAmounts() {
        return false;
    }

    @Override
    public boolean test(T t) {
        return Arrays.stream(filters).anyMatch(f -> f != null && f.test(t));
    }

    @Override
    public Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {

        SlotGroup slotGroup = new SlotGroup("filters", 9);

        Grid filterGrid = new Grid()
                .coverChildren()
                .gridOfSizeWidth(9, 3, (x, y, i) -> new ItemSlot()
                        .slot(SyncHandlers.itemSlot(itemStacks, i).slotGroup(slotGroup)));

        return Flow.row()
                .coverChildrenHeight()
                .child(filterGrid.horizontalCenter());
    }
}
