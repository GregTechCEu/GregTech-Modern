package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.Dialog;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.FluidSlot;
import lombok.Getter;

import java.util.Arrays;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleFluidFilter implements FluidFilter {

    @Getter
    protected boolean isBlackList;
    @Getter
    protected boolean ignoreNbt;
    @Getter
    protected FluidStack[] matches = new FluidStack[9];

    protected Consumer<FluidFilter> itemWriter = filter -> {};
    protected Consumer<FluidFilter> onUpdated = filter -> itemWriter.accept(filter);

    @Getter
    protected int maxStackSize = 1;

    private final CustomFluidTank[] fluidStorageSlots = new CustomFluidTank[9];

    protected SimpleFluidFilter() {
        for (int i = 0; i < 9; i++) {
            int finalI = i;
            fluidStorageSlots[i] = new CustomFluidTank(64000);
            fluidStorageSlots[i].setOnContentsChanged(() -> {
                matches[finalI] = fluidStorageSlots[finalI].getFluid();
                onUpdated.accept(this);
            });
        }
        Arrays.fill(matches, FluidStack.EMPTY);
    }

    public static SimpleFluidFilter loadFilter(ItemStack itemStack) {
        return loadFilter(itemStack.getOrCreateTag(), filter -> itemStack.setTag(filter.saveFilter()));
    }

    private static SimpleFluidFilter loadFilter(CompoundTag tag, Consumer<FluidFilter> itemWriter) {
        var handler = new SimpleFluidFilter();
        handler.itemWriter = itemWriter;
        handler.isBlackList = tag.getBoolean("isBlackList");
        handler.ignoreNbt = tag.getBoolean("matchNbt");
        var list = tag.getList("matches", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            handler.matches[i] = FluidStack.loadFluidStackFromNBT((CompoundTag) list.get(i));
            handler.fluidStorageSlots[i].setFluid(handler.matches[i]);
        }
        return handler;
    }

    @Override
    public void setOnUpdated(Consumer<FluidFilter> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }

    @Override
    public boolean isBlank() {
        return !isBlackList && !ignoreNbt && Arrays.stream(matches).allMatch(FluidStack::isEmpty);
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
            list.add(match.writeToNBT(new CompoundTag()));
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
        for (int i = 0; i < 9; i++) {
            syncManager.syncValue("filter_slot_" + i,
                    new FluidSlotSyncHandler(fluidStorageSlots[i]).controlsAmount(true).phantom(true));
        }

        Grid filterGrid = new Grid()
                .coverChildren()
                .gridOfSizeWidth(9, 3, (x, y, i) -> new FluidSlot().syncHandler("filter_slot_" + i));

        BooleanSyncValue blacklist = new BooleanSyncValue(this::isBlackList, this::setBlackList);
        syncManager.syncValue("blacklist", blacklist);

        BooleanSyncValue ignoreNBT = new BooleanSyncValue(this::isIgnoreNbt, this::setIgnoreNbt);
        syncManager.syncValue("ignoreNBT", ignoreNBT);

        Flow filterConfigButtons = Flow.col()
                .coverChildren()
                .child(new ToggleButton().stateBackground(GTGuiTextures.BUTTON_BLACKLIST).syncHandler("blacklist"))
                .child(new ToggleButton().stateBackground(GTGuiTextures.BUTTON_IGNORE_NBT).syncHandler("ignoreNBT"));

        return new Dialog<>("simple_fluid_filter")
                .disablePanelsBelow(false)
                .draggable(true)
                .closeOnOutOfBoundsClick(true)
                .child(GTMuiWidgets.createTitleBar(GTItems.FLUID_FILTER.asStack(), 176, GTGuiTextures.BACKGROUND))
                .child(Flow.row()
                        .top(10)
                        .coverChildrenHeight()
                        .child(filterGrid.horizontalCenter())
                        .child(filterConfigButtons.marginLeft(118)))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    @Override
    public boolean test(FluidStack other) {
        return testFluidAmount(other) > 0L;
    }

    @Override
    public int testFluidAmount(FluidStack fluidStack) {
        int totalFluidAmount = getTotalConfiguredFluidAmount(fluidStack);

        if (isBlackList) {
            return (totalFluidAmount > 0) ? 0 : Integer.MAX_VALUE;
        }

        return totalFluidAmount;
    }

    public int getTotalConfiguredFluidAmount(FluidStack fluidStack) {
        int totalAmount = 0;

        for (var candidate : matches) {
            if (ignoreNbt && candidate.getFluid() == fluidStack.getFluid()) {
                totalAmount += candidate.getAmount();
            }
            if (!ignoreNbt && candidate.isFluidEqual(fluidStack)) {
                totalAmount += candidate.getAmount();
            }
        }

        return totalAmount;
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;

        for (CustomFluidTank slot : fluidStorageSlots) {
            if (slot != null)
                slot.setCapacity(maxStackSize);
        }

        for (FluidStack match : matches) {
            if (!match.isEmpty())
                match.setAmount(Math.min(match.getAmount(), maxStackSize));
        }
    }
}
