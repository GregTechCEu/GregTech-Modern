package brachy.modularui.value.sync;

import brachy.modularui.utils.MouseData;
import brachy.modularui.widgets.slot.ModularSlot;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.api.distmarker.Dist;

import org.jetbrains.annotations.ApiStatus;

/**
 * Wraps a slot and handles interactions for phantom slots.
 * Use {@link ModularSlot} directly.
 */
public class PhantomItemSlotSyncHandler extends ItemSlotSyncHandler {

    public static final int SYNC_CLICK = 100;
    public static final int SYNC_SCROLL = 101;
    public static final int SYNC_ITEM_SIMPLE = 102;

    private ItemStack lastStoredPhantomItem = ItemStack.EMPTY;

    @ApiStatus.Internal
    public PhantomItemSlotSyncHandler(ModularSlot slot) {
        super(slot);
        ((Slot) slot).index = -1;
    }

    @Override
    public void init(String key, PanelSyncManager syncHandler) {
        super.init(key, syncHandler);
        if (isPhantom() && !getSlot().getItem().isEmpty()) {
            this.lastStoredPhantomItem = getSlot().getItem().copy();
            this.lastStoredPhantomItem.setCount(1);
        }
    }

    @Override
    protected void onSlotUpdate(ItemStack oldStack, ItemStack newStack, boolean client, boolean init) {
        if (!ItemStack.isSameItemSameComponents(oldStack, newStack) && !newStack.isEmpty()) {
            // store last non-empty stack for later
            this.lastStoredPhantomItem = newStack.copy();
            this.lastStoredPhantomItem.setCount(1);
        }
        super.onSlotUpdate(oldStack, newStack, client, init);
    }

    @Override
    public void readOnClient(int id, RegistryFriendlyByteBuf buf) {
        if (id == SYNC_ITEM) {
            // for normal slots minecraft handles the syncing
            // for phantom slots we manually set the slot and ignore the other packet arguments
            // the set() will then invoke the onSlotChanged
            // Must match the ItemSlotSyncHandler#checkUpdate's syncToClient() call order
            ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            getSlot().set(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            buf.readBoolean();
            buf.readBoolean();
            return;
        }
        super.readOnClient(id, buf);
    }

    @Override
    public void readOnServer(int id, RegistryFriendlyByteBuf buf) {
        super.readOnServer(id, buf);
        if (id == SYNC_CLICK) {
            phantomClick(MouseData.readPacket(buf));
        } else if (id == SYNC_SCROLL) {
            phantomScroll(MouseData.readPacket(buf), buf.readDouble(), buf.readDouble());
        } else if (id == SYNC_ITEM_SIMPLE) {
            if (!isPhantom()) return;
            phantomClick(new MouseData(Dist.DEDICATED_SERVER, 0, false, false, false),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
        }
    }

    public void updateFromClient(ItemStack stack) {
        syncToServer(SYNC_ITEM_SIMPLE, buf -> ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack));
    }

    protected void phantomClick(MouseData mouseData) {
        phantomClick(mouseData, getSyncManager().getCursorItem());
    }

    protected void phantomClick(MouseData mouseData, ItemStack cursorStack) {
        ItemStack slotStack = getSlot().getItem();
        ItemStack stackToPut;
        if (!cursorStack.isEmpty() && !slotStack.isEmpty() &&
                !ItemStack.isSameItemSameComponents(cursorStack, slotStack)) {
            if (!isItemValid(cursorStack)) return;
            stackToPut = cursorStack.copy();
            if (mouseData.mouseButton() == InputConstants.MOUSE_BUTTON_RIGHT) {
                stackToPut.setCount(1);
            }
            stackToPut.setCount(Math.min(stackToPut.getCount(), getSlot().getMaxStackSize(stackToPut)));
            getSlot().set(stackToPut);
            this.lastStoredPhantomItem = stackToPut.copy();
        } else if (slotStack.isEmpty()) {
            if (cursorStack.isEmpty()) {
                if (mouseData.mouseButton() == InputConstants.MOUSE_BUTTON_RIGHT && !this.lastStoredPhantomItem.isEmpty()) {
                    stackToPut = this.lastStoredPhantomItem.copy();
                } else {
                    return;
                }
            } else {
                if (!isItemValid(cursorStack)) return;
                stackToPut = cursorStack.copy();
            }
            if (mouseData.mouseButton() == InputConstants.MOUSE_BUTTON_RIGHT) {
                stackToPut.setCount(1);
            }
            stackToPut.setCount(Math.min(stackToPut.getCount(), getSlot().getMaxStackSize(stackToPut)));
            getSlot().set(stackToPut);
            this.lastStoredPhantomItem = stackToPut.copy();
        } else {
            if (mouseData.mouseButton() == InputConstants.MOUSE_BUTTON_LEFT) {
                if (mouseData.shift()) {
                    getSlot().set(ItemStack.EMPTY);
                } else {
                    incrementStackCount(-1);
                }
            } else if (mouseData.mouseButton() == InputConstants.MOUSE_BUTTON_RIGHT) {
                incrementStackCount(1);
            }
        }
    }

    protected void phantomScroll(MouseData mouseData, double scrollX, double scrollY) {
        ItemStack currentItem = getSlot().getItem();
        int amount = (int) scrollY;
        if (mouseData.shift()) amount *= 4;
        if (mouseData.ctrl()) amount *= 16;
        if (mouseData.alt()) amount *= 64;

        if (amount > 0 && currentItem.isEmpty() && !this.lastStoredPhantomItem.isEmpty()) {
            ItemStack stackToPut = this.lastStoredPhantomItem.copy();
            stackToPut.setCount(amount);
            getSlot().set(stackToPut);
        } else {
            incrementStackCount(amount);
        }
    }

    public void incrementStackCount(int amount) {
        ItemStack stack = getSlot().getItem();
        if (stack.isEmpty()) {
            return;
        }
        int oldAmount = stack.getCount();
        if (amount < 0) {
            amount = Math.max(0, oldAmount + amount);
        } else {
            if (Integer.MAX_VALUE - amount < oldAmount) {
                amount = Integer.MAX_VALUE;
            } else {
                int maxSize = getSlot().getMaxStackSize();
                if (!getSlot().isIgnoreMaxStackSize() && stack.getMaxStackSize() < maxSize) {
                    maxSize = stack.getMaxStackSize();
                }
                amount = Math.min(oldAmount + amount, maxSize);
            }
        }
        if (oldAmount != amount) {
            stack = stack.copy();
            stack.setCount(amount);
            getSlot().set(stack);
        }
    }

    @Override
    public boolean isPhantom() {
        return true;
    }
}
