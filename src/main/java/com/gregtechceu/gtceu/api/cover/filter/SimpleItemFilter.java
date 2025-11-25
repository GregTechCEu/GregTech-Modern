package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.Dialog;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.slot.PhantomItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

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

    protected ModularPanel panel;
    protected ModularPanel popupPanel;

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
    public WidgetGroup openConfigurator(int x, int y) {
        return null;
    }

    @Override
    public void createPanel(PanelSyncManager syncManager) {
        this.panel = makePanel(syncManager).child(SlotGroupWidget.playerInventory(true));
        if (this.panel instanceof Dialog<?> dialog) {
            dialog.setDraggable(false);
        }
    }

    @Override
    public void createPopupPanel(PanelSyncManager syncManager) {
        this.popupPanel = makePanel(syncManager);
        if (this.popupPanel instanceof Dialog<?> dialog) {
            dialog.setDraggable(true)
                    .setDisablePanelsBelow(false)
                    .closeOnOutOfBoundsClick();
        }
    }

    @Override
    public ModularPanel getPanel(PanelSyncManager syncManager) {
        if (this.panel == null) {
            createPanel(syncManager);
        }
        return this.panel;
    }

    @Override
    public ModularPanel getPopupPanel(PanelSyncManager syncManager) {
        if (this.popupPanel == null) {
            createPopupPanel(syncManager);
        }
        return this.popupPanel;
    }

    private ModularPanel makePanel(PanelSyncManager syncManager) {
        var handler = new CustomItemStackHandler(matches.length) {

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
                return matches[slot] = stack;
            }
        };

        SlotGroup filterInv = new SlotGroup("filter_inv", 3, 1000, true);

        syncManager.registerSlotGroup(filterInv);

        return new Dialog<>("item_filter_dialog", null)
                .resizeableOnDrag(true)
                .padding(7)
                .child(IKey.lang("cover.item_filter.title").asWidget().left(5).top(5))
                .child(new Column()
                        .coverChildren()
                        .name("base")
                        .child(new Row()
                                .height(18 * 3)
                                .left(54).top(10)
                                .child(new Column()
                                        .coverChildren()
                                        .child(SlotGroupWidget.builder()
                                                .matrix("III", "III", "III")
                                                .key('I', index -> new PhantomItemSlot()
                                                        // TODO: Figure out how to get the phantom slots to actually add
                                                        // stuff to the item "handler"
                                                        .slot(SyncHandlers.phantomItemSlot(handler, index)
                                                                .ignoreMaxStackSize(true)
                                                                .slotGroup(filterInv)))
                                                .build()))
                                .child(new Column()
                                        .coverChildren()
                                        .child(new ToggleButton()
                                                .margin(0, 4, 4, 4)
                                                .size(20)
                                                .value(new BoolValue.Dynamic(this::isBlackList, this::setBlackList))
                                                .tooltip(ttip -> ttip.add(
                                                        IKey.dynamic(
                                                                () -> Component.translatable("cover.filter.blacklist." +
                                                                        (isBlackList ? "enabled" : "disabled")))))
                                                .stateOverlay(GTGuiTextures.BUTTON_BLACKLIST))
                                        .child(new ToggleButton()
                                                .margin(0, 4, 4, 4)
                                                .size(20)
                                                .value(new BoolValue.Dynamic(this::isIgnoreNbt, this::setIgnoreNbt))
                                                .tooltip(ttip -> ttip.add(
                                                        IKey.dynamic(() -> Component
                                                                .translatable("cover.item_filter.ignore_nbt." +
                                                                        (ignoreNbt ? "enabled" : "disabled")))))
                                                .stateOverlay(GTGuiTextures.BUTTON_IGNORE_NBT)))));
    }

    // public WidgetGroup openConfigurator(int x, int y) {
    // WidgetGroup group = new WidgetGroup(x, y, 18 * 3 + 25, 18 * 3); // 80 55
    // for (int i = 0; i < 3; i++) {
    // for (int j = 0; j < 3; j++) {
    // final int index = i * 3 + j;
    //
    // var handler = new CustomItemStackHandler(matches[index]);
    //
    // var slot = new PhantomSlotWidget(handler, 0, i * 18, j * 18) {
    //
    // @Override
    // public void updateScreen() {
    // super.updateScreen();
    // setMaxStackSize(maxStackSize);
    // }
    //
    // @Override
    // public void detectAndSendChanges() {
    // super.detectAndSendChanges();
    // setMaxStackSize(maxStackSize);
    // }
    // };
    //
    // slot.setChangeListener(() -> {
    // matches[index] = handler.getStackInSlot(0);
    // onUpdated.accept(this);
    // }).setBackground(GuiTextures.SLOT);
    //
    // group.addWidget(slot);
    // }
    // }
    // group.addWidget(new ToggleButtonWidget(18 * 3 + 5, 0, 20, 20,
    // GuiTextures.BUTTON_BLACKLIST, this::isBlackList, this::setBlackList));
    // group.addWidget(new ToggleButtonWidget(18 * 3 + 5, 20, 20, 20,
    // GuiTextures.BUTTON_FILTER_NBT, this::isIgnoreNbt, this::setIgnoreNbt));
    // return group;
    // }

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
            if (!ignoreNbt && ItemStack.isSameItemSameTags(candidate, itemStack)) {
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
