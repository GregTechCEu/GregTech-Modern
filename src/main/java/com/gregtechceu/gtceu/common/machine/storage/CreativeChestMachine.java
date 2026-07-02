package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.PhantomItemSlotSyncHandler;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.PhantomItemSlot;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import lombok.Getter;

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
        return new InfiniteCache();
    }

    private InteractionResult updateStored(ItemStack item) {
        stored = item.copyWithCount(1);
        onItemChanged();
        return InteractionResult.SUCCESS;
    }

    private void setTicksPerCycle(int value) {
        ticksPerCycle = value;
        autoOutput.setTicksPerCycle(ticksPerCycle);
        onItemChanged();
    }

    private void setItemsPerCycle(int value) {
        itemsPerCycle = value;
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
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        var heldItem = context.getItemInHand();
        var player = context.getPlayer();

        if (context.getClickedFace() == getFrontFacing() && !isRemote()) {
            // Clear item if empty hand + shift-rclick
            if (heldItem.isEmpty() && player.isCrouching() && !stored.isEmpty()) {
                return updateStored(ItemStack.EMPTY);
            }

            // If held item can stack with stored item, delete held item
            if (!heldItem.isEmpty() && ItemHandlerHelper.canItemStacksStack(stored, heldItem)) {
                player.setItemInHand(context.getHand(), ItemStack.EMPTY);
                return InteractionResult.SUCCESS;
            } else if (!heldItem.isEmpty()) { // If held item is different than stored item, update stored item
                return updateStored(heldItem);
            }
        }
        return super.onUseWithItem(context);
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return MachineUIPanelBuilder.panelBuilder(this).addDefaultConfigurators(false)
                .addTraitConfigurators(false).rightConfigurators(f -> f.child(GTMuiWidgets.createPowerButton(this)));
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        PhantomItemSlotSyncHandler storedSlot = new PhantomItemSlotSyncHandler(new ModularSlot(cache, 0).filter(
                stack -> stored.isEmpty() || ItemStack.isSameItemSameTags(stack, stored)));

        syncManager.syncValue("stored", storedSlot);

        IntSyncValue itemsPerCycle = new IntSyncValue(this::getItemsPerCycle, this::setItemsPerCycle);
        syncManager.syncValue("itemsPerCycle", itemsPerCycle);
        IntSyncValue ticksPerCycle = new IntSyncValue(this::getTicksPerCycle, this::setTicksPerCycle);
        syncManager.syncValue("ticksPerCycle", ticksPerCycle);

        mainWidget
                .child(Flow.col()
                        .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, 86)
                        .name("main")
                        .padding(7)
                        .mainAxisAlignment(Alignment.MainAxis.START)
                        .child(Flow.row().coverChildrenHeight()
                                .child(Text.lang("gtceu.creative.chest.item").asWidget()
                                        .marginRight(4)
                                        .verticalCenter())
                                .child(new PhantomItemSlot().syncHandler("stored")))
                        .child(new Rectangle().color(0xFF555555).asWidget()
                                .height(1).widthRel(0.95f).marginBottom(4).marginTop(4))
                        .child(Flow.row()
                                .height(18)
                                .child(Text.lang("gtceu.creative.chest.ipc").asWidget()
                                        .marginRight(4)
                                        .width(80)
                                        .verticalCenter())
                                .child(new TextFieldWidget()
                                        .setTextAlignment(Alignment.CENTER)
                                        .setNumbers(1, Integer.MAX_VALUE)
                                        .value(itemsPerCycle)))
                        .child(new Rectangle().color(0xFF555555).asWidget()
                                .height(1).widthRel(0.95f).marginBottom(4).marginTop(4))
                        .child(Flow.row()
                                .height(18)
                                .child(Text.lang("gtceu.creative.chest.tpc").asWidget()
                                        .marginRight(4)
                                        .width(80)
                                        .verticalCenter())
                                .child(new TextFieldWidget()
                                        .setTextAlignment(Alignment.CENTER)
                                        .setNumbers(1, Integer.MAX_VALUE)
                                        .value(ticksPerCycle))));
    }

    private class InfiniteCache extends ItemCache {

        public InfiniteCache() {
            super();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return stored;
        }

        @Override
        public void setStackInSlot(int index, ItemStack stack) {
            updateStored(stack);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!stored.isEmpty() && GTUtil.isSameItemSameTags(stored, stack)) return ItemStack.EMPTY;
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!stored.isEmpty()) return stored.copyWithCount(itemsPerCycle);
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    }
}
