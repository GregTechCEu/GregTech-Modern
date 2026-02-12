package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeChestMachine extends QuantumChestMachine {

    @Getter
    @SaveField
    private int itemsPerCycle, ticksPerCycle = 1;

    public CreativeChestMachine(BlockEntityCreationInfo info) {
        super(info, GTValues.MAX, -1);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) autoOutput.setTicksPerCycle(ticksPerCycle);
    }

    @Override
    protected ItemCache createCacheItemHandler() {
        return new InfiniteCache(this);
    }

    private InteractionResult updateStored(ItemStack item) {
        stored = item.copyWithCount(1);
        onItemChanged();
        return InteractionResult.SUCCESS;
    }

    private void setTicksPerCycle(String value) {
        if (value.isEmpty()) return;
        ticksPerCycle = Integer.parseInt(value);
        autoOutput.setTicksPerCycle(ticksPerCycle);
        onItemChanged();
    }

    private void setItemsPerCycle(String value) {
        if (value.isEmpty()) return;
        itemsPerCycle = Integer.parseInt(value);
        onItemChanged();
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        tag.putInt("itemsPerCycle", itemsPerCycle);
        tag.putInt("ticksPerCycle", ticksPerCycle);
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        itemsPerCycle = tag.getInt("itemsPerCycle");
        ticksPerCycle = tag.getInt("ticksPerCycle");
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        var heldItem = player.getItemInHand(hand);
        if (hit.getDirection() == getFrontFacing() && !isRemote()) {
            // Clear item if empty hand + shift-rclick
            if (heldItem.isEmpty() && player.isCrouching() && !stored.isEmpty()) {
                return updateStored(ItemStack.EMPTY);
            }

            // If held item can stack with stored item, delete held item
            if (!heldItem.isEmpty() && ItemHandlerHelper.canItemStacksStack(stored, heldItem)) {
                player.setItemInHand(hand, ItemStack.EMPTY);
                return InteractionResult.SUCCESS;
            } else if (!heldItem.isEmpty()) { // If held item is different than stored item, update stored item
                return updateStored(heldItem);
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 176, 131);
        group.addWidget(new PhantomSlotWidget(cache, 0, 36, 6)
                .setClearSlotOnRightClick(true)
                .setMaxStackSize(1)
                .setBackgroundTexture(GuiTextures.SLOT));
        group.addWidget(new LabelWidget(7, 9, "gtceu.creative.chest.item"));
        group.addWidget(new ImageWidget(7, 48, 154, 14, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget(9, 50, 152, 10, () -> String.valueOf(itemsPerCycle), this::setItemsPerCycle)
                .setMaxStringLength(11)
                .setNumbersOnly(1, Integer.MAX_VALUE));
        group.addWidget(new LabelWidget(7, 28, "gtceu.creative.chest.ipc"));
        group.addWidget(new ImageWidget(7, 85, 154, 14, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget(9, 87, 152, 10, () -> String.valueOf(ticksPerCycle), this::setTicksPerCycle)
                .setMaxStringLength(11)
                .setNumbersOnly(1, Integer.MAX_VALUE));
        group.addWidget(new LabelWidget(7, 65, "gtceu.creative.chest.tpc"));
        group.addWidget(new SwitchWidget(7, 101, 162, 20, (clickData, value) -> setWorkingEnabled(value))
                .setTexture(
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                new TextTexture("gtceu.creative.activity.off")),
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                new TextTexture("gtceu.creative.activity.on")))
                .setPressed(isWorkingEnabled()));

        return group;
    }

    private class InfiniteCache extends ItemCache {

        public InfiniteCache(MetaMachine holder) {
            super(holder);
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return stored;
        }

        @Override
        public void setStackInSlot(int index, ItemStack stack) {
            updateStored(stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!stored.isEmpty() && GTUtil.isSameItemSameTags(stored, stack)) return ItemStack.EMPTY;
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!stored.isEmpty()) return stored.copyWithCount(itemsPerCycle);
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    }
}
