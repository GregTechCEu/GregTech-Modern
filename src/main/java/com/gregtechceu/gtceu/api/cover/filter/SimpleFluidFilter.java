package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.FluidSlot;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleFluidFilter extends Filter<FluidStack> {

    @Getter
    protected boolean isBlackList;
    @Getter
    protected boolean ignoreNbt;
    @Getter
    protected FluidStack[] matches = new FluidStack[9];

    @Getter
    protected int maxStackSize = 1;

    private final CustomFluidTank[] fluidStorageSlots = new CustomFluidTank[9];

    public SimpleFluidFilter(ItemStack stack) {
        super(stack);
        var tag = stack.getOrCreateTag();

        for (int i = 0; i < 9; i++) {
            int finalI = i;
            fluidStorageSlots[i] = new CustomFluidTank(64000);
            fluidStorageSlots[i].setOnContentsChanged(() -> {
                matches[finalI] = fluidStorageSlots[finalI].getFluid();
                onUpdated.accept(this);
            });
        }
        Arrays.fill(matches, FluidStack.EMPTY);

        if (tag.isEmpty()) return;

        isBlackList = tag.getBoolean("isBlackList");
        ignoreNbt = tag.getBoolean("matchNbt");
        var list = tag.getList("matches", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            matches[i] = FluidStack.loadFluidStackFromNBT((CompoundTag) list.get(i));
            fluidStorageSlots[i].setFluid(matches[i]);
        }
    }

    public @Nullable CompoundTag saveFilter() {
        if (!isBlackList && !ignoreNbt && Arrays.stream(matches).allMatch(FluidStack::isEmpty)) {
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

    @Override
    public boolean supportsAmounts() {
        return !isBlackList();
    }

    public void setBlackList(boolean blackList) {
        isBlackList = blackList;
        onUpdated.accept(this);
    }

    public void setIgnoreNbt(boolean ingoreNbt) {
        this.ignoreNbt = ingoreNbt;
        onUpdated.accept(this);
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
        return testAmount(other) > 0L;
    }

    @Override
    public int testAmount(FluidStack fluidStack) {
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
            slot.setCapacity(maxStackSize);
        }

        for (FluidStack match : matches) {
            if (!match.isEmpty())
                match.setAmount(Math.min(match.getAmount(), maxStackSize));
        }
    }
}
