package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget;
import com.gregtechceu.gtceu.api.item.datacomponents.CreativeMachineInfo;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.data.item.GTDataComponents;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class CreativeChestMachine extends QuantumChestMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CreativeChestMachine.class,
            QuantumChestMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Persisted
    private int itemsPerCycle = 1;
    @Getter
    @Persisted
    @DropSaved
    private int ticksPerCycle = 1;

    public CreativeChestMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.MAX, -1);
    }

    @Override
    protected ItemCache createCacheItemHandler(Object... args) {
        return new InfiniteCache(this);
    }

    protected void checkAutoOutput() {
        if (getOffsetTimer() % ticksPerCycle == 0) {
            if (isAutoOutputItems() && getOutputFacingItems() != null) {
                cache.exportToNearby(getOutputFacingItems());
            }
            updateAutoOutputSubscription();
        }
    }

    private void updateStored(ItemStack item) {
        stored = item.copyWithCount(1);
        onItemChanged();
    }

    private void setTicksPerCycle(String value) {
        if (value.isEmpty()) return;
        ticksPerCycle = Integer.parseInt(value);
        onItemChanged();
    }

    private void setItemsPerCycle(String value) {
        if (value.isEmpty()) return;
        itemsPerCycle = Integer.parseInt(value);
        onItemChanged();
    }

    @Override
    public ItemInteractionResult onUseWithItem(ItemStack stack, BlockState state, Level world, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (hit.getDirection() != getFrontFacing() || isRemote()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        // If held item can stack with stored item, delete held item
        if (ItemStack.isSameItemSameComponents(stored, stack)) {
            player.setItemInHand(hand, ItemStack.EMPTY);
            return ItemInteractionResult.SUCCESS;
        } else if (!stack.isEmpty()) { // If held item is different than stored item, update stored item
            updateStored(stack);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        var heldItem = player.getItemInHand(hand);
        if (hit.getDirection() != getFrontFacing() || isRemote()) {
            return InteractionResult.PASS;
        }
        // Clear item if empty hand + shift-rclick
        if (heldItem.isEmpty() && player.isShiftKeyDown() && !stored.isEmpty()) {
            updateStored(ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 176, 131);
        group.addWidget(new PhantomSlotWidget(cache, 0, 36, 6)
                .setClearSlotOnRightClick(true)
                .setMaxStackSize(1)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setChangeListener(this::markDirty));
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

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void applyImplicitComponents(MetaMachineBlockEntity.@NotNull ExDataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        CreativeMachineInfo info = componentInput.get(GTDataComponents.CREATIVE_MACHINE_INFO);
        if (info != null) {
            itemsPerCycle = info.outputPerCycle();
            ticksPerCycle = info.ticksPerCycle();
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.@NotNull Builder components) {
        super.collectImplicitComponents(components);
        components.set(GTDataComponents.CREATIVE_MACHINE_INFO, new CreativeMachineInfo(itemsPerCycle, ticksPerCycle));
    }

    @Override
    public void removeItemComponentsFromTag(@NotNull CompoundTag tag) {
        super.removeItemComponentsFromTag(tag);
        tag.remove("itemsPerCycle");
        tag.remove("ticksPerCycle");
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
            if (!stored.isEmpty() && ItemStack.isSameItemSameComponents(stored, stack)) return ItemStack.EMPTY;
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
