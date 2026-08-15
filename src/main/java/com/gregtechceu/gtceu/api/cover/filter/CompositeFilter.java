package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.SlotGroup;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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

    public CompositeFilter(Class<T> filterableType) {
        this.filterableType = filterableType;
    }

    public CompositeFilter(List<ItemStack> items, Class<T> filterableType) {
        this.filterableType = filterableType;
        for (int i = 0; i < 9; i++) {
            itemStacks.setStackInSlot(i, items.get(i));
        }
    }

    private void onFilterItemChanged(int slot) {
        ItemStack newItem = itemStacks.getStackInSlot(slot);
        filters[slot] = null;
        if (!newItem.isEmpty()) {
            filters[slot] = Filters.loadFilter(filterableType, newItem);
            Objects.requireNonNull(filters[slot]).setOnUpdated($ -> updateAndSaveFilter());
        }

        updateAndSaveFilter();
    }

    @Override
    public boolean test(T t) {
        for (int i = 0; i < 9; i++) {
            Filter<T> filter = filters[i];
            if (filter != null && filter.test(t)) return true;
        }
        return false;
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

    public static <T> Codec<CompositeFilter<T>> codec(Class<T> filterableObjectType) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.list(ItemStack.CODEC).fieldOf("itemStacks").forGetter(v -> v.itemStacks.toList()))
                .apply(instance, s -> new CompositeFilter<>(s, filterableObjectType)));
    }
}
