package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.MethodsReturnNonnullByDefault;
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
import org.jetbrains.annotations.Range;

import java.util.Arrays;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleItemFilter extends Filter<ItemStack> {

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

    @Getter
    protected int maxStackSize;

    public SimpleItemFilter() {
        Arrays.fill(matches, ItemStack.EMPTY);
        maxStackSize = 1;
    }

    public SimpleItemFilter(boolean isBlackList, boolean ignoreNbt, List<ItemStack> matches) {
        this.isBlackList = isBlackList;
        this.ignoreNbt = ignoreNbt;
        this.matches = matches.toArray(ItemStack[]::new);
    }

    @Override
    public boolean supportsAmounts() {
        return !isBlackList();
    }

    public void setBlackList(boolean blackList) {
        isBlackList = blackList;
        updateAndSaveFilter();
    }

    public void setIgnoreNbt(boolean ingoreNbt) {
        this.ignoreNbt = ingoreNbt;
        updateAndSaveFilter();
    }

    @Override
    public Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        FilterItemStackHandler handler = new FilterItemStackHandler(matches, this);
        Grid filterGrid = new Grid()
                .coverChildren()
                .gridOfSizeWidth(9, 3, (x, y, i) -> new PhantomItemSlot()
                        .size(16)
                        .syncHandler(new PhantomItemSlotSyncHandler(new ModularSlot(handler, i)
                                .ignoreMaxStackSize(true).accessibility(true, false))));

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
        public ItemStack getStackInSlot(int slot) {
            return matches[slot];
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount >= matches[slot].getCount()) {
                matches[slot] = ItemStack.EMPTY;
            }
            return matches[slot];
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            super.setStackInSlot(slot, stack);
            matches[slot] = stack.copyWithCount(1);
            filter.updateAndSaveFilter();
        }
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return testAmount(itemStack) > 0;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int testAmount(ItemStack itemStack) {
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
