package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.PhantomItemSlot;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.common.mui.GTGuis;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
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

    private IItemHandlerModifiable inventory() {
        return new CustomItemStackHandler(matches.length) {
            {
                for (int i = 0; i < matches.length; i++) {
                    setStackInSlot(i, matches[i]);
                }
            }

            @Override
            public void setStackInSlot(int index, @NotNull ItemStack stack) {
                matches[index] = stack.copyWithCount(1);
                onUpdated.accept(SimpleItemFilter.this);
            }

            @Override
            public @NotNull ItemStack getStackInSlot(int slot) {
                return matches[slot];
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (!simulate) {
                    setStackInSlot(slot, stack.copyWithCount(1));
                }
                return stack;
            }

        };
    }

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
    public ModularPanel createPanel() {
         return GTGuis.createPanel("test")
                .padding(7)
                .child(new Column()
                        .coverChildren()
                        .name("base")
                        .child(new Row()
                                .name("main ui panel")
                                .coverChildren()
                                .child(SlotGroupWidget
                                        .builder()
                                        .matrix("III","III","iii")
                                        .matrix("III", "III", "III")
                                        .key('I', index -> new PhantomItemSlot()
                                                .slot(SyncHandlers.phantomItemSlot(inventory(), index)
                                                        .ignoreMaxStackSize(true))).build())
                                .padding(7)
                                .mainAxisAlignment(Alignment.MainAxis.CENTER))
                        .child(SlotGroupWidget.playerInventory(false)
                        )
                );
    }

    //    public WidgetGroup openConfigurator(int x, int y) {
//        WidgetGroup group = new WidgetGroup(x, y, 18 * 3 + 25, 18 * 3); // 80 55
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                final int index = i * 3 + j;
//
//                var handler = new CustomItemStackHandler(matches[index]);
//
//                var slot = new PhantomSlotWidget(handler, 0, i * 18, j * 18) {
//
//                    @Override
//                    public void updateScreen() {
//                        super.updateScreen();
//                        setMaxStackSize(maxStackSize);
//                    }
//
//                    @Override
//                    public void detectAndSendChanges() {
//                        super.detectAndSendChanges();
//                        setMaxStackSize(maxStackSize);
//                    }
//                };
//
//                slot.setChangeListener(() -> {
//                    matches[index] = handler.getStackInSlot(0);
//                    onUpdated.accept(this);
//                }).setBackground(GuiTextures.SLOT);
//
//                group.addWidget(slot);
//            }
//        }
//        group.addWidget(new ToggleButtonWidget(18 * 3 + 5, 0, 20, 20,
//                GuiTextures.BUTTON_BLACKLIST, this::isBlackList, this::setBlackList));
//        group.addWidget(new ToggleButtonWidget(18 * 3 + 5, 20, 20, 20,
//                GuiTextures.BUTTON_FILTER_NBT, this::isIgnoreNbt, this::setIgnoreNbt));
//        return group;
//    }

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
