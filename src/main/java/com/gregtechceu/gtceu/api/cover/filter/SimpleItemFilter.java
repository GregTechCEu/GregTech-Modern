package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.PhantomItemSlotSyncHandler;
import brachy.modularui.widgets.Dialog;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.PhantomItemSlot;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleItemFilter implements ItemFilter {

    @Getter
    protected boolean isBlackList;
    @Getter
    protected boolean ignoreNbt;
    @Getter
    protected ItemStack[] matches = new ItemStack[9];

    protected Consumer<ItemFilter> itemWriter = filter -> {};
    protected Consumer<ItemFilter> onUpdated = filter -> itemWriter.accept(filter);

    @Getter
    protected int maxStackSize;

    protected SimpleItemFilter() {
        Arrays.fill(matches, ItemStack.EMPTY);
        maxStackSize = 1;
    }

    public static SimpleItemFilter loadFilter(ItemStack itemStack) {
        return loadFilter(itemStack.getOrCreateTag(), filter -> itemStack.setTag(filter.saveFilter()));
    }

    private static SimpleItemFilter loadFilter(CompoundTag tag, Consumer<ItemFilter> itemWriter) {
        var handler = new SimpleItemFilter();
        handler.itemWriter = itemWriter;
        handler.isBlackList = tag.getBoolean("isBlackList");
        handler.ignoreNbt = tag.getBoolean("matchNbt");
        var list = tag.getList("matches", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            handler.matches[i] = ItemStack.of((CompoundTag) list.get(i));
        }
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

    public CompoundTag saveFilter() {
        if (isBlank()) {
            return null;
        }
        var tag = new CompoundTag();
        tag.putBoolean("isBlackList", isBlackList);
        tag.putBoolean("matchNbt", ignoreNbt);
        var list = new ListTag();
        for (var match : matches) {
            list.add(match.save(new CompoundTag()));
        }
        tag.put("matches", list);
        return tag;
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
    public ModularPanel<?> getPanel(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        FilterItemStackHandler handler = new FilterItemStackHandler(matches, this);

        Grid filterGrid = new Grid()
                .coverChildren()
                .gridOfSizeWidth(9, 3, (x, y, i) -> new PhantomItemSlot()
                        .size(16)
                        .syncHandler(new PhantomItemSlotSyncHandler(new ModularSlot(handler, i)
                                .changeListener((stack, amount, client, init) -> {
                                    handler.setStackInSlot(i, stack);
                                }).ignoreMaxStackSize(true).accessibility(true, false))));

        BooleanSyncValue blacklist = new BooleanSyncValue(this::isBlackList, this::setBlackList);
        syncManager.syncValue("blacklist", blacklist);

        BooleanSyncValue ignoreNBT = new BooleanSyncValue(this::isIgnoreNbt, this::setIgnoreNbt);
        syncManager.syncValue("ignoreNBT", ignoreNBT);

        Flow filterConfigButtons = Flow.col()
                .coverChildren()
                .child(new ToggleButton().stateBackground(GTGuiTextures.BUTTON_BLACKLIST).syncHandler("blacklist"))
                .child(new ToggleButton().stateBackground(GTGuiTextures.BUTTON_IGNORE_NBT).syncHandler("ignoreNBT"));

        return new Dialog<>("simple_item_filter")
                .disablePanelsBelow(false)
                .draggable(true)
                .closeOnOutOfBoundsClick(true)
                .child(GTMuiWidgets.createTitleBar(GTItems.ITEM_FILTER.asStack(), 176, GTGuiTextures.BACKGROUND))
                .child(Flow.row()
                        .top(10)
                        .coverChildrenHeight()
                        .child(filterGrid.horizontalCenter())
                        .child(filterConfigButtons.marginLeft(118)))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
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
            if (ignoreNbt && ItemStack.isSameItem(candidate, itemStack)) {
                totalCount += candidate.getCount();
            }
            if (!ignoreNbt && GTUtil.isSameItemSameTags(candidate, itemStack)) {
                totalCount += candidate.getCount();
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
}
