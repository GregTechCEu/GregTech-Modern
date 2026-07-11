package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.PhantomItemSlotSyncHandler;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.PhantomItemSlot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SimpleItemFilter implements ItemFilter {

    public static final Codec<SimpleItemFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("is_blacklist").forGetter(val -> val.isBlackList),
            Codec.BOOL.fieldOf("ignore_components").forGetter(val -> val.ignoreNbt),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("matches").forGetter(val -> Arrays.stream(val.matches).toList()))
            .apply(instance, SimpleItemFilter::new));

    @Getter
    protected boolean isBlackList;
    @Getter
    protected boolean ignoreNbt;
    @Getter
    protected ItemStack[] matches = new ItemStack[9];

    protected Consumer<SimpleItemFilter> itemWriter = filter -> {};
    protected Consumer<SimpleItemFilter> onUpdated = filter -> itemWriter.accept(filter);

    @Getter
    protected int maxStackSize;

    protected SimpleItemFilter() {
        Arrays.fill(matches, ItemStack.EMPTY);
        maxStackSize = 1;
    }

    public SimpleItemFilter(boolean isBlackList, boolean ignoreNbt, List<ItemStack> matches) {
        this.isBlackList = isBlackList;
        this.ignoreNbt = ignoreNbt;
        this.matches = matches.toArray(ItemStack[]::new);
    }

    public static SimpleItemFilter loadFilter(ItemStack itemStack) {
        SimpleItemFilter handler = itemStack.getOrDefault(GTDataComponents.SIMPLE_ITEM_FILTER, new SimpleItemFilter());
        handler.itemWriter = filter -> itemStack.set(GTDataComponents.SIMPLE_ITEM_FILTER, filter);
        return handler;
    }

    @Override
    public void setOnUpdated(Consumer<ItemFilter> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }

    @Override
    public boolean isBlank() {
        return !isBlackList && !ignoreNbt && Arrays.stream(matches).allMatch(ItemStack::isEmpty);
    }

    public void setBlackList(boolean blackList) {
        isBlackList = blackList;
        onUpdated.accept(this);
    }

    public void setIgnoreNbt(boolean ingoreNbt) {
        this.ignoreNbt = ingoreNbt;
        onUpdated.accept(this);
    }

    @Override
    public ItemStack getFilterItem() {
        return GTItems.ITEM_FILTER.asStack();
    }

    @Override
    public Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        FilterItemStackHandler handler = new FilterItemStackHandler(matches, this);

        Grid filterGrid = new Grid()
                .coverChildren()
                .gridOfSizeWidth(9, 3, (x, y, i) -> new PhantomItemSlot()
                        .size(16)
                        .syncHandler(new PhantomItemSlotSyncHandler(new ModularSlot(handler, i)
                                .changeListener((stack, amount, client, init) -> {
                                    handler.setStackInSlot(i, stack);
                                }).ignoreMaxStackSize(true).accessibility(true, false))));

        BooleanSyncValue blacklist = new BooleanSyncValue(this::isBlackList, this::setBlackList).allowC2S();
        syncManager.syncValue("blacklist", blacklist);

        BooleanSyncValue ignoreNBT = new BooleanSyncValue(this::isIgnoreNbt, this::setIgnoreNbt).allowC2S();
        syncManager.syncValue("ignoreNBT", ignoreNBT);

        Flow filterConfigButtons = Flow.col()
                .coverChildren()
                .child(new ToggleButton().stateBackground(GTGuiTextures.BUTTON_BLACKLIST).syncHandler("blacklist"))
                .child(new ToggleButton().stateBackground(GTGuiTextures.BUTTON_IGNORE_NBT).syncHandler("ignoreNBT"));
        return Flow.row()
                .coverChildrenHeight()
                .child(filterGrid.horizontalCenter())
                .child(filterConfigButtons.marginLeft(118));
    }

    public static class FilterItemStackHandler extends CustomItemStackHandler {

        private final ItemStack[] matches;
        private final SimpleItemFilter filter;

        public FilterItemStackHandler(SimpleItemFilter filter) {
            this(filter.matches, filter);
        }

        public FilterItemStackHandler(ItemStack[] matches, SimpleItemFilter simpleItemFilter) {
            super(matches.length);
            this.matches = matches;
            this.filter = simpleItemFilter;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return matches[slot];
        }

        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount >= matches[slot].getCount()) {
                matches[slot] = ItemStack.EMPTY;
            }
            return matches[slot];
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            super.setStackInSlot(slot, stack);
            matches[slot] = stack.copyWithCount(1);
            filter.onUpdated.accept(filter);
        }
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return testItemCount(itemStack) > 0;
    }

    @Override
    public int testItemCount(ItemStack itemStack) {
        int totalItemCount = getTotalConfiguredItemCount(itemStack);

        if (isBlackList) {
            return (totalItemCount > 0) ? 0 : Integer.MAX_VALUE;
        }

        return totalItemCount;
    }

    public int getTotalConfiguredItemCount(ItemStack itemStack) {
        int totalCount = 0;

        for (var candidate : matches) {
            if (ignoreNbt) {
                if (ItemStack.isSameItem(candidate, itemStack)) totalCount += candidate.getCount();
            } else {
                if (ItemStack.isSameItemSameComponents(candidate, itemStack)) totalCount += candidate.getCount();
            }
        }

        return totalCount;
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;

        for (ItemStack match : matches) {
            match.setCount(Math.min(match.getCount(), maxStackSize));
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleItemFilter that)) return false;

        return isBlackList == that.isBlackList && ignoreNbt == that.ignoreNbt && maxStackSize == that.maxStackSize &&
                Arrays.equals(matches, that.matches);
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(isBlackList);
        result = 31 * result + Boolean.hashCode(ignoreNbt);
        result = 31 * result + Arrays.hashCode(matches);
        result = 31 * result + maxStackSize;
        return result;
    }
}
