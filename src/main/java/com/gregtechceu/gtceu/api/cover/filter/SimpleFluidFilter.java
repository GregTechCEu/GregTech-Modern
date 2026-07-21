package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.FluidSlot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SimpleFluidFilter implements FluidFilter {

    public static final Codec<SimpleFluidFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("is_blacklist").forGetter(val -> val.isBlackList),
            Codec.BOOL.fieldOf("ignore_components").forGetter(val -> val.ignoreNbt),
            FluidStack.OPTIONAL_CODEC.listOf().fieldOf("matches").forGetter(val -> Arrays.stream(val.matches).toList()))
            .apply(instance, SimpleFluidFilter::new));
    @Getter
    protected boolean isBlackList;
    @Getter
    protected boolean ignoreNbt;
    @Getter
    protected FluidStack[] matches = new FluidStack[9];

    protected Consumer<SimpleFluidFilter> itemWriter = filter -> {};
    protected Consumer<SimpleFluidFilter> onUpdated = filter -> itemWriter.accept(filter);

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

    protected SimpleFluidFilter(boolean isBlackList, boolean ignoreNbt, List<FluidStack> matches) {
        this.isBlackList = isBlackList;
        this.ignoreNbt = ignoreNbt;
        this.matches = matches.toArray(FluidStack[]::new);
    }

    public static SimpleFluidFilter loadFilter(ItemStack itemStack) {
        var handler = itemStack.getOrDefault(GTDataComponents.SIMPLE_FLUID_FILTER, new SimpleFluidFilter());
        handler.itemWriter = filter -> itemStack.set(GTDataComponents.SIMPLE_FLUID_FILTER, filter);
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
        return GTItems.FLUID_FILTER.asStack();
    }

    public Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        for (int i = 0; i < 9; i++) {
            syncManager.syncValue("filter_slot_" + i,
                    new FluidSlotSyncHandler(fluidStorageSlots[i]).controlsAmount(true).phantom(true));
        }

        Grid filterGrid = new Grid()
                .coverChildren()
                .gridOfSizeWidth(9, 3, (x, y, i) -> new FluidSlot().syncHandler("filter_slot_" + i));

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
            if (ignoreNbt) {
                if (FluidStack.isSameFluid(candidate, fluidStack)) totalAmount += candidate.getAmount();
            } else {
                if (FluidStack.isSameFluidSameComponents(candidate, fluidStack)) totalAmount += candidate.getAmount();
            }
        }

        return totalAmount;
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;

        for (CustomFluidTank slot : fluidStorageSlots) {
            slot.setCapacity(maxStackSize);
        }

        for (FluidStack match : matches) {
            if (!match.isEmpty())
                match.setAmount(Math.min(match.getAmount(), maxStackSize));
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleFluidFilter that)) return false;

        return isBlackList == that.isBlackList && ignoreNbt == that.ignoreNbt && Arrays.equals(matches, that.matches);
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(isBlackList);
        result = 31 * result + Boolean.hashCode(ignoreNbt);
        result = 31 * result + Arrays.hashCode(matches);
        return result;
    }
}
