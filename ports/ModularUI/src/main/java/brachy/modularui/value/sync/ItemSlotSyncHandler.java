package brachy.modularui.value.sync;

import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.PlayerSlotType;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Wraps a slot and handles interactions for phantom slots.
 * Use {@link ModularSlot} directly.
 */
public class ItemSlotSyncHandler extends SyncHandler<ItemSlotSyncHandler> {

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
        allowC2S();
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
        checkUpdate(init);
    }

    public void checkUpdate() {
        checkUpdate(false);
    }

    private void checkUpdate(boolean init) {
        if (!isValid() || getSyncManager().isClient()) return;
        ItemStack itemStack = getSlot().getItem();
        if (itemStack.isEmpty() && this.lastStoredItem.isEmpty()) return;
        if (init || !ItemStack.matches(itemStack, this.lastStoredItem)) {
            ItemStack oldStack = this.lastStoredItem;
            onSlotUpdate(oldStack, itemStack, false, init);
            this.lastStoredItem = itemStack.copy();

            final boolean forceSync = false;
            syncToClient(SYNC_ITEM, buffer -> {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, oldStack);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, itemStack);
                buffer.writeBoolean(init);
                buffer.writeBoolean(forceSync);
            });
        }
    }

    @Override
    public void readOnClient(int id, RegistryFriendlyByteBuf buf) {
        if (id == SYNC_ITEM) {
            ItemStack oldStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            ItemStack newStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            this.lastStoredItem = newStack;
            onSlotUpdate(oldStack, newStack, true, buf.readBoolean());
            if (buf.readBoolean()) {
                // force sync
                this.slot.set(this.lastStoredItem.copy());
            }
        } else if (id == SYNC_ENABLED) {
            setEnabled(buf.readBoolean(), false);
        }
    }

    @Override
    public void readOnServer(int id, RegistryFriendlyByteBuf buf) {
        if (id == SYNC_ENABLED) {
            setEnabled(buf.readBoolean(), false);
        }
    }

    protected void onSlotUpdate(ItemStack oldStack, ItemStack newStack, boolean client, boolean init) {
        getSlot().onSlotChangedReal(oldStack, newStack, client, init);
    }

    public void setEnabled(boolean enabled, boolean sync) {
        this.slot.setEnabled(enabled);
        if (sync) {
            sync(SYNC_ENABLED, buffer -> buffer.writeBoolean(enabled));
        }
    }

    public void forceSyncItem() {
        ItemStack newStack = this.slot.getItem();
        ItemStack oldStack = this.lastStoredItem;
        boolean init = false;
        boolean forceSync = true;
        onSlotUpdate(oldStack, newStack, getSyncManager().isClient(), init);
        this.lastStoredItem = newStack.copy();
        syncToClient(SYNC_ITEM, buffer -> {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, oldStack);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, newStack);
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
