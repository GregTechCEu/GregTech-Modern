package com.gregtechceu.gtceu.client.mui.screen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.factory.GuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.ModularSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.common.data.GTMenuTypes;
import com.gregtechceu.gtceu.core.mixins.client.AbstractContainerMenuAccessor;
import com.gregtechceu.gtceu.utils.NetworkUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

import lombok.Getter;
import org.jetbrains.annotations.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ModularContainerMenu extends AbstractContainerMenu {

    public static ModularContainerMenu getCurrent(Player player) {
        if (player.containerMenu instanceof ModularContainerMenu container) {
            return container;
        }
        return null;
    }

    private static final int DROP_TO_WORLD = -999;
    private static final int LEFT_MOUSE = 0;
    private static final int RIGHT_MOUSE = 1;

    @Getter
    private Player player;
    private ModularSyncManager syncManager;
    private boolean init = true;
    // all phantom slots (inventory doesn't contain phantom slots)
    private final List<ModularSlot> phantomSlots = new ArrayList<>();
    private final List<ModularSlot> shiftClickSlots = new ArrayList<>();
    @Getter
    private GuiData guiData;
    private UISettings settings;

    @OnlyIn(Dist.CLIENT)
    private ModularScreen optionalScreen;

    public ModularContainerMenu(int containerId) {
        super(GTMenuTypes.MODULAR_CONTAINER.get(), containerId);
    }

    public <T extends GuiData> ModularContainerMenu(MenuType<ModularContainerMenu> type, int containerId,
                                                    Inventory playerInv, @Nullable FriendlyByteBuf data) {
        super(type, containerId);
        // TODO: Better integration with menu types for custom containers and screens.
        throw new IllegalArgumentException("Do not open the modular container the forge way. Use an UIFactory!");
    }

    @ApiStatus.Internal
    public void construct(Player player, ModularSyncManager msm, UISettings settings, String mainPanelName,
                          GuiData guiData) {
        this.player = player;
        this.syncManager = msm;
        this.syncManager.construct(this, mainPanelName);
        this.settings = settings;
        this.guiData = guiData;
        sortShiftClickSlots();
    }

    @OnlyIn(Dist.CLIENT)
    void initializeClient(ModularScreen screen) {
        this.optionalScreen = screen;
    }

    @ApiStatus.Internal
    @OnlyIn(Dist.CLIENT)
    public void constructClientOnly() {
        this.player = Minecraft.getInstance().player;
        this.syncManager = null;
    }

    public boolean isInitialized() {
        return this.player != null;
    }

    @OnlyIn(Dist.CLIENT)
    public ModularScreen getScreen() {
        if (this.optionalScreen == null) throw new NullPointerException("ModularScreen is not yet initialised!");
        return this.optionalScreen;
    }

    public boolean isScreenInitialized() {
        return this.optionalScreen != null;
    }

    public AbstractContainerMenuAccessor acc() {
        return (AbstractContainerMenuAccessor) this;
    }

    public void opened() {}

    /**
     * Called when this container closes. This is different to {@link AbstractContainerMenu#removed(Player)}, since that
     * one is also
     * called from {@link AbstractContainerScreen#removed()}, which means it is called even when the container may still
     * exist.
     * This happens when a temporary client screen takes over (like JEI,NEI,etc.). This is only called when the
     * container actually closes.
     */

    public void closed() {}

    public void disposed() {}

    @MustBeInvokedByOverriders
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.syncManager != null) {
            this.syncManager.detectAndSendChanges(this.init);
        }
        this.init = false;
    }

    @MustBeInvokedByOverriders
    public void onUpdate() {
        // detectAndSendChanges is potentially called multiple times per tick, while this method is called exactly once
        // per tick
        if (this.syncManager != null) {
            this.syncManager.onUpdate();
        }
    }

    private void sortShiftClickSlots() {
        this.shiftClickSlots.sort(ModularSlot.SHIFT_CLICK_PRIORITY);
    }

    @Override
    public void initializeContents(int stateId, List<ItemStack> items, @NotNull ItemStack carried) {
        if (this.slots.size() != items.size()) {
            GTCEu.LOGGER.error("Here are {} slots, but expected {}", this.slots.size(), items.size());
        }
        super.initializeContents(stateId, items, carried);
    }

    @ApiStatus.Internal
    public void registerSlot(String panelName, ModularSlot slot) {
        if (slot.isPhantom()) {
            if (this.phantomSlots.contains(slot)) {
                throw new IllegalArgumentException("Tried to register slot which already exists!");
            }
            this.phantomSlots.add(slot);
        } else {
            if (this.slots.contains(slot)) {
                throw new IllegalArgumentException("Tried to register slot which already exists!");
            }
            addSlot(slot);
        }
        if (slot.getSlotGroupName() != null) {
            SlotGroup slotGroup = getSyncManager().getSlotGroup(panelName, slot.getSlotGroupName());
            if (slotGroup == null) {
                GTCEu.LOGGER.throwing(
                        new IllegalArgumentException("SlotGroup '" + slot.getSlotGroupName() + "' is not registered!"));
                return;
            }
            slot.slotGroup(slotGroup);
        }
        if (slot.getSlotGroup() != null) {
            SlotGroup slotGroup = slot.getSlotGroup();
            if (slotGroup.isAllowShiftTransfer()) {
                this.shiftClickSlots.add(slot);
                if (!this.init) {
                    sortShiftClickSlots();
                }
            }
        }
    }

    @Contract("_, null, null -> fail")
    @NotNull
    @ApiStatus.Internal
    public SlotGroup validateSlotGroup(String panelName, @Nullable String slotGroupName,
                                       @Nullable SlotGroup slotGroup) {
        if (slotGroup != null) {
            if (getSyncManager().getSlotGroup(panelName, slotGroup.getName()) == null) {
                throw new IllegalArgumentException("Slot group is not registered in the GUI.");
            }
            return slotGroup;
        }
        if (slotGroupName != null) {
            slotGroup = getSyncManager().getSlotGroup(panelName, slotGroupName);
            if (slotGroup == null) {
                throw new IllegalArgumentException("Can't find slot group for name " + slotGroupName);
            }
            return slotGroup;
        }
        throw new IllegalArgumentException("Either the slot group or the name must not be null!");
    }

    public ModularSyncManager getSyncManager() {
        if (this.syncManager == null) {
            throw new IllegalStateException("GuiSyncManager is not available for client only GUI's.");
        }
        return this.syncManager;
    }

    public boolean isClient() {
        return this.syncManager == null || NetworkUtils.isClient(this.player);
    }

    public boolean isClientOnly() {
        return this.syncManager == null;
    }

    public ModularSlot getModularSlot(int index) {
        Slot slot = this.slots.get(index);
        if (slot instanceof ModularSlot modularSlot) {
            return modularSlot;
        }
        throw new IllegalStateException(
                "A non-ModularSlot was found, but all slots in a ModularContainer must extend ModularSlot.");
    }

    @UnmodifiableView
    public List<ModularSlot> getShiftClickSlots() {
        return Collections.unmodifiableList(this.shiftClickSlots);
    }

    public void onSlotChanged(ModularSlot slot, ItemStack stack, boolean onlyAmountChanged) {}

    @Override
    public boolean stillValid(@NotNull Player playerIn) {
        return this.settings.canPlayerInteractWithUI(playerIn);
    }

    @Override
    public void clicked(int slotId, int mouseButton, @NotNull ClickType clickTypeIn, @NotNull Player player) {
        ItemStack returnable = ItemStack.EMPTY;
        Inventory inventory = player.getInventory();

        if (clickTypeIn == ClickType.QUICK_CRAFT || acc().getQuickcraftType() != -1) {
            superClicked(slotId, mouseButton, clickTypeIn, player);
            return;
        }

        if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) &&
                (mouseButton == LEFT_MOUSE || mouseButton == RIGHT_MOUSE)) {
            if (slotId == DROP_TO_WORLD) {
                superClicked(slotId, mouseButton, clickTypeIn, player);
                return;
            }

            // early return
            if (slotId < 0) return;

            if (clickTypeIn == ClickType.QUICK_MOVE) {
                Slot fromSlot = getSlot(slotId);

                if (!fromSlot.mayPickup(player)) {
                    return;
                }
                // simpler code, but effectively no difference // TODO: NEA
                ItemStack remainder;
                do {
                    remainder = quickMoveStack(player, slotId);
                    returnable = remainder.copy();
                } while (!remainder.isEmpty() && ItemHandlerHelper.canItemStacksStack(fromSlot.getItem(), remainder));
            } else {
                Slot clickedSlot = getSlot(slotId);

                ItemStack slotStack = clickedSlot.getItem();
                ItemStack heldStack = this.getCarried();

                if (slotStack.isEmpty()) {
                    // no dif
                    if (!heldStack.isEmpty() && clickedSlot.mayPlace(heldStack)) {
                        int stackCount = mouseButton == LEFT_MOUSE ? heldStack.getCount() : 1;

                        int lim = clickedSlot.getMaxStackSize(heldStack);
                        if (stackCount > lim) {
                            stackCount = lim;
                        }

                        clickedSlot.setByPlayer(heldStack.split(stackCount));
                    }
                } else if (clickedSlot.mayPickup(player)) {
                    if (heldStack.isEmpty() && !slotStack.isEmpty()) {
                        // checking max stack size here, probably for oversized slots
                        int s = Math.min(slotStack.getCount(), slotStack.getMaxStackSize());
                        int toRemove = mouseButton == LEFT_MOUSE ? s : (s + 1) / 2;
                        this.setCarried(slotStack.split(toRemove));
                        clickedSlot.setByPlayer(slotStack);
                        clickedSlot.onTake(player, this.getCarried());
                    } else if (clickedSlot.mayPlace(heldStack)) {
                        if (ItemStack.isSameItemSameTags(slotStack, heldStack)) {
                            int stackCount = mouseButton == LEFT_MOUSE ? heldStack.getCount() : 1;

                            int lim = clickedSlot.getMaxStackSize(heldStack);
                            if (stackCount > lim - slotStack.getCount()) {
                                stackCount = lim - slotStack.getCount();
                            }

                            heldStack.shrink(stackCount);
                            slotStack.grow(stackCount);
                            clickedSlot.setByPlayer(slotStack);

                        } else if (heldStack.getCount() <= clickedSlot.getMaxStackSize(heldStack)) {
                            clickedSlot.setByPlayer(heldStack);
                            this.setCarried(slotStack);
                        }
                    } else if (heldStack.getMaxStackSize() > 1 &&
                            ItemStack.isSameItemSameTags(slotStack, heldStack) && !slotStack.isEmpty()) {
                                int stackCount = slotStack.getCount();

                                if (stackCount + heldStack.getCount() <= heldStack.getMaxStackSize()) {
                                    heldStack.grow(stackCount);
                                    slotStack = clickedSlot.remove(stackCount);

                                    if (slotStack.isEmpty()) {
                                        clickedSlot.setByPlayer(ItemStack.EMPTY);
                                    }

                                    clickedSlot.onTake(player, this.getCarried());
                                }
                            }
                }
                clickedSlot.setChanged();
            }
            broadcastChanges();
        } else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot slot = slots.get(slotId);
            ItemStack carried = this.getCarried();

            if (!carried.isEmpty() && (!slot.hasItem() || !slot.mayPickup(player))) {
                int i = mouseButton == 0 ? 0 : slots.size() - 1;
                int j = mouseButton == 0 ? 1 : -1;

                for (int k = 0; k < 2; ++k) {
                    for (int l = i; l >= 0 && l < slots.size() &&
                            carried.getCount() < carried.getMaxStackSize(); l += j) {
                        Slot slot1 = slots.get(l);
                        if (slot1 instanceof ModularSlot modularSlot && modularSlot.isPhantom()) continue;

                        if (slot1.hasItem() && canItemQuickReplace(slot1, carried, true) && slot1.mayPickup(player) &&
                                canTakeItemForPickAll(carried, slot1)) {
                            ItemStack slotItem = slot1.getItem();

                            if (k != 0 || slotItem.getCount() != slotItem.getMaxStackSize()) {
                                int toRemove = Math.min(carried.getMaxStackSize() - carried.getCount(),
                                        slotItem.getCount());
                                ItemStack removed = slot1.remove(toRemove);
                                carried.grow(toRemove);

                                if (removed.isEmpty()) {
                                    slot1.setByPlayer(ItemStack.EMPTY);
                                }

                                slot1.onTake(player, removed);
                            }
                        }
                    }
                }
            }

            broadcastChanges();
            return;
        } else if (clickTypeIn == ClickType.SWAP && mouseButton >= 0 && mouseButton < 9) {
            ModularSlot phantom = getModularSlot(slotId);
            ItemStack hotbarStack = inventory.getItem(mouseButton);
            if (phantom.isPhantom()) {
                // insert stack from hotbar slot into phantom slot
                phantom.setByPlayer(hotbarStack.isEmpty() ? ItemStack.EMPTY : hotbarStack.copy());
                broadcastChanges();
            }
        } else {
            superClicked(slotId, mouseButton, clickTypeIn, player);
        }
    }

    protected final void superClicked(int slotId, int mouseButton, @NotNull ClickType clickTypeIn,
                                      @NotNull Player player) {
        super.clicked(slotId, mouseButton, clickTypeIn, player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
        ModularSlot slot = getModularSlot(index);
        if (!slot.isPhantom()) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                ItemStack copy = stack.copy();
                stack = stack.copy();
                int base = 0;
                if (stack.getCount() > stack.getMaxStackSize()) {
                    base = stack.getCount() - stack.getMaxStackSize();
                    stack.setCount(stack.getMaxStackSize());
                }
                ItemStack remainder = transferItem(slot, stack.copy());
                if (ItemStack.isSameItemSameTags(remainder, stack)) return ItemStack.EMPTY;
                if (base == 0 && remainder.isEmpty()) stack = ItemStack.EMPTY;
                else stack.setCount(base + remainder.getCount());
                slot.set(stack);
                slot.onQuickCraft(remainder, copy);
                slot.onTake(playerIn, remainder);
                slot.onCraftShiftClick(playerIn, remainder);
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack transferItem(ModularSlot fromSlot, ItemStack fromStack) {
        @Nullable
        SlotGroup fromSlotGroup = fromSlot.getSlotGroup();
        // in first iteration only insert into non-empty, non-phantom slots
        for (ModularSlot toSlot : getShiftClickSlots()) {
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            if (slotGroup != fromSlotGroup && toSlot.isActive() && toSlot.mayPlace(fromStack)) {
                ItemStack toStack = toSlot.getItem().copy();
                if (!fromSlot.isPhantom() && ItemHandlerHelper.canItemStacksStack(fromStack, toStack)) {
                    int j = toStack.getCount() + fromStack.getCount();
                    // Math.min(toSlot.getMaxStackSize(), fromStack.getMaxStackSize());
                    int maxSize = toSlot.getMaxStackSize(fromStack);

                    if (j <= maxSize) {
                        fromStack.setCount(0);
                        toStack.setCount(j);
                        toSlot.set(toStack);
                    } else if (toStack.getCount() < maxSize) {
                        fromStack.shrink(maxSize - toStack.getCount());
                        toStack.setCount(maxSize);
                        toSlot.set(toStack);
                    }

                    if (fromStack.isEmpty()) {
                        return fromStack;
                    }
                }
            }
        }
        boolean hasNonEmptyPhantom = false;
        // now insert into first empty slot (phantom or not) and check if we have any non-empty phantom slots
        for (ModularSlot toSlot : getShiftClickSlots()) {
            ItemStack itemstack = toSlot.getItem();
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            if (slotGroup != fromSlotGroup && toSlot.isActive() && toSlot.mayPlace(fromStack)) {
                if (toSlot.isPhantom()) {
                    if (!itemstack.isEmpty()) {
                        // skip non-empty phantom for now
                        hasNonEmptyPhantom = true;
                    } else {
                        toSlot.set(fromStack.copy());
                        return fromStack;
                    }
                } else if (itemstack.isEmpty()) {
                    if (fromStack.getCount() > toSlot.getMaxStackSize(fromStack)) {
                        toSlot.set(fromStack.split(toSlot.getMaxStackSize(fromStack)));
                    } else {
                        toSlot.set(fromStack.split(fromStack.getCount()));
                    }
                    if (fromStack.getCount() < 1) {
                        break;
                    }
                }
            }
        }
        if (!hasNonEmptyPhantom) return fromStack;

        // now insert into the first phantom slot we can find (will be non-empty)
        // unfortunately, when all phantom slots are used it will always overwrite the first one
        for (ModularSlot toSlot : getShiftClickSlots()) {
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            if (slotGroup != fromSlotGroup && toSlot.isPhantom() && toSlot.isActive() && toSlot.mayPlace(fromStack)) {
                // don't check for stackable, just overwrite
                toSlot.set(fromStack.copy());
                return fromStack;
            }
        }
        return fromStack;
    }
}
