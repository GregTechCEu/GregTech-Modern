package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.PlayerSlotType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Wraps a slot and handles interactions for phantom slots.
 * Use {@link ModularSlot} directly.
 */
public class ItemSlotSyncHandler extends SyncHandler {

    public static final int SYNC_ITEM = 0;
    public static final int SYNC_ENABLED = 1;

    @Getter
    private final ModularSlot slot;
    @Nullable
    @Getter
    private final PlayerSlotType playerSlotType;
    private ItemStack lastStoredItem;
    private boolean registered = false;

    public ItemSlotSyncHandler(ModularSlot slot) {
        this.slot = slot;
        this.playerSlotType = PlayerSlotType.getPlayerSlotType(slot);
    }

    @Override
    public void init(String key, PanelSyncManager syncHandler) {
        super.init(key, syncHandler);
        if (!registered) {
            this.slot.initialize(this, isPhantom());
            getSyncManager().getContainer().registerSlot(getSyncManager().getPanelName(), this.slot);
            this.registered = true;
        }
        this.lastStoredItem = getSlot().getItem().copy();
    }

    @Override
    public void dispose() {
        super.dispose();
        this.slot.dispose();
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        ItemStack itemStack = getSlot().getItem();
        if (itemStack.isEmpty() && this.lastStoredItem.isEmpty()) return;
        boolean onlyAmountChanged = false;
        if (init ||
                !ItemHandlerHelper.canItemStacksStack(this.lastStoredItem, itemStack) ||
                (onlyAmountChanged = itemStack.getCount() != this.lastStoredItem.getCount())) {
            onSlotUpdate(itemStack, onlyAmountChanged, false, init);
            if (onlyAmountChanged) {
                this.lastStoredItem.setCount(itemStack.getCount());
            } else {
                this.lastStoredItem = itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copy();
            }
            final boolean finalOnlyAmountChanged = onlyAmountChanged;
            final boolean forceSync = false;
            syncToClient(SYNC_ITEM, buffer -> {
                buffer.writeBoolean(finalOnlyAmountChanged);
                buffer.writeItem(itemStack);
                buffer.writeBoolean(init);
                buffer.writeBoolean(forceSync);
            });
        }
    }

    @Override
    public void readOnClient(int id, FriendlyByteBuf buf) {
        if (id == SYNC_ITEM) {
            boolean onlyAmountChanged = buf.readBoolean();
            this.lastStoredItem = buf.readItem();
            onSlotUpdate(this.lastStoredItem, onlyAmountChanged, true, buf.readBoolean());
            if (buf.readBoolean()) {
                // force sync
                this.slot.set(this.lastStoredItem);
            }
        } else if (id == SYNC_ENABLED) {
            setEnabled(buf.readBoolean(), false);
        }
    }

    @Override
    public void readOnServer(int id, FriendlyByteBuf buf) {
        if (id == SYNC_ENABLED) {
            setEnabled(buf.readBoolean(), false);
        }
    }

    protected void onSlotUpdate(ItemStack stack, boolean onlyAmountChanged, boolean client, boolean init) {
        getSlot().onSlotChangedReal(stack, onlyAmountChanged, client, init);
    }

    public void setEnabled(boolean enabled, boolean sync) {
        this.slot.setEnabled(enabled);
        if (sync) {
            sync(SYNC_ENABLED, buffer -> buffer.writeBoolean(enabled));
        }
    }

    public void forceSyncItem() {
        boolean onlyAmountChanged = false;
        ItemStack stack = slot.getItem();
        boolean init = false;
        boolean forceSync = true;
        onSlotUpdate(stack, onlyAmountChanged, getSyncManager().isClient(), init);
        this.lastStoredItem = stack.isEmpty() ? ItemStack.EMPTY : stack;
        syncToClient(SYNC_ITEM, buffer -> {
            buffer.writeBoolean(onlyAmountChanged);
            buffer.writeItem(stack);
            buffer.writeBoolean(init);
            buffer.writeBoolean(forceSync);
        });
    }

    public boolean isItemValid(ItemStack itemStack) {
        return getSlot().mayPlace(itemStack);
    }

    public boolean isPhantom() {
        return false;
    }

    public boolean isPlayerSlot() {
        return playerSlotType != null;
    }

    @Nullable
    public String getSlotGroup() {
        return this.slot.getSlotGroupName();
    }
}
