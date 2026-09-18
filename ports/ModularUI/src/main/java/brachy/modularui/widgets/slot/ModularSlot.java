package brachy.modularui.widgets.slot;

import brachy.modularui.core.mixins.common.SlotAccessor;
import brachy.modularui.core.mixins.common.CombinedInvWrapperAccessor;
import brachy.modularui.value.sync.ItemSlotSyncHandler;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.mojang.datafixers.util.Pair;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.PlayerArmorInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerOffhandInvWrapper;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * The base class for slots in a modular ui.
 * It represents an interface between a player (via gui) and a slot in a {@link IItemHandler} that exists
 * on server and client.
 */
public class ModularSlot extends SlotItemHandler {

    public static final Comparator<ModularSlot> SHIFT_CLICK_PRIORITY = Comparator.comparingInt(
            slot -> Objects.requireNonNull(slot.getSlotGroup()).getShiftClickPriority());

    @Getter
    @Setter(onMethod_ = {@ApiStatus.Internal})
    private boolean enabled = true;
    @Getter private boolean canTake = true, canPut = true, canDragInto = true;
    @Getter private Predicate<ItemStack> filter = stack -> true;
    private IOnSlotChanged changeListener = IOnSlotChanged.DEFAULT;
    @Getter
    private boolean ignoreMaxStackSize = false;
    @Getter
    private @Nullable String slotGroupName = null;
    @Getter
    private @Nullable SlotGroup slotGroup = null;
    @Getter
    private boolean phantom = false;

    private ItemSlotSyncHandler syncHandler = null;

    public static ModularSlot playerSlot(PlayerInvWrapper inv, int index, Player player) {
        return new ModularSlot(inv, index, player);
    }

    public static ModularSlot playerSlot(PlayerMainInvWrapper inv, int index, Player player) {
        return new ModularSlot(inv, index, player);
    }

    public static ModularSlot playerSlot(PlayerArmorInvWrapper inv, int index, Player player) {
        return new ModularSlot(inv, index, player);
    }

    public static ModularSlot playerOffhandSlot(Player player) {
        return new ModularSlot(new PlayerOffhandInvWrapper(player.getInventory()), 0, player);
    }

    /**
     * Creates a ModularSlot
     *
     * @param itemHandler item handler of the slot
     * @param index       slot index in the item handler
     */
    public ModularSlot(IItemHandler itemHandler, int index) {
        super(itemHandler, index, Integer.MIN_VALUE, Integer.MIN_VALUE);
        validateIndex(itemHandler, index);
        if (isPlayerInvWrapper(itemHandler)) {
            throw new IllegalArgumentException("This constructor must NOT use forge player inventory wrapper.");
        }
    }

    protected ModularSlot(IItemHandler itemHandler, int index, Player player) {
        super(itemHandler, index, Integer.MIN_VALUE, Integer.MIN_VALUE);
        validateIndex(itemHandler, index);
        if (!isPlayerInvWrapper(itemHandler)) {
            throw new IllegalArgumentException("This constructor must use forge player inventory wrapper.");
        }
        ((SlotAccessor) this).setContainer(player.getInventory());
    }

    @ApiStatus.Internal
    public void initialize(ItemSlotSyncHandler syncManager, boolean phantom) {
        this.syncHandler = syncManager;
        this.phantom = phantom;
    }

    @ApiStatus.Internal
    public void dispose() {
        this.syncHandler = null;
        this.phantom = false;
    }

    public boolean isInitialized() {
        return this.syncHandler != null;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return this.canPut && !stack.isEmpty() && this.filter.test(stack) && super.mayPlace(stack);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return this.canTake && super.mayPickup(playerIn);
    }

    public boolean canDragIntoSlot() {
        return this.canDragInto;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return this.ignoreMaxStackSize ? getMaxStackSize() : super.getMaxStackSize(stack);
    }

    @Override
    public void setChanged() {
        if (this.syncHandler != null) {
            this.syncHandler.checkUpdate();
        }
    }

    public void onSlotChangedReal(ItemStack oldStack, ItemStack newStack, boolean client, boolean init) {
        if (this.slotGroup != null) {
            this.slotGroup.slotChanged(this);
        }
        this.changeListener.onChange(oldStack, newStack, client, init);
        if (!init && isInitialized()) {
            getSyncHandler().getSyncManager().getContainer().onSlotChanged(this, oldStack, newStack);
        }
    }

    public void onCraftShiftClick(Player playerIn, ItemStack itemStack) {}

    @Override
    public @Nullable Pair<Identifier, Identifier> getNoItemIcon() {
        return null;
    }

    @Override
    public boolean isActive() {
        return this.isEnabled();
    }

    public @NotNull ItemSlotSyncHandler getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("ModularSlot is not yet initialized");
        }
        return this.syncHandler;
    }

    protected Player getPlayer() {
        return getSyncHandler().getSyncManager().getPlayer();
    }

    @Override
    public boolean isSameInventory(@NotNull Slot other) {
        return other instanceof SlotItemHandler slotItemHandler && slotItemHandler.getItemHandler() == this.getItemHandler();
    }

    /**
     * Sets a filter. The predicate is called every time someone tries to insert something via the gui.
     *
     * @param filter the predicate to test on every item
     */
    public ModularSlot filter(Predicate<ItemStack> filter) {
        this.filter = filter != null ? filter : stack -> true;
        return this;
    }

    /**
     * Sets a change listener that is called every time the item in this slot changes, with the new item as argument.
     * ! It is not guaranteed that the new item is different from the old one.
     *
     * @param changeListener change listener that should be called on a change
     */
    public ModularSlot changeListener(IOnSlotChanged changeListener) {
        this.changeListener = changeListener != null ? changeListener : IOnSlotChanged.DEFAULT;
        return this;
    }

    /**
     * Sets if items can be taken or put into this slot via the gui.
     * ! It does NOT affect transfers via pipes and the likes!
     *
     * @param canPut  if items can be put into the slot via the gui
     * @param canTake if items can be taken from the slot via the gui
     */
    public ModularSlot accessibility(boolean canPut, boolean canTake) {
        this.canPut = canPut;
        this.canTake = canTake;
        return this;
    }

    public ModularSlot canPut(boolean canPut) {
        this.canPut = canPut;
        return this;
    }

    public ModularSlot canTake(boolean canTake) {
        this.canTake = canTake;
        return this;
    }

    /**
     * Sets if this slots accepts items which are dragged across the screen.
     * This is useful to disable when the filter depends on the items in the other slots.
     * When dragging, the item in the slot is not real and its only updated once the dragging is completed.
     * This method is by default called from {@link brachy.modularui.screen.ModularContainerMenu#canDragTo(Slot)},
     * which can be overridden for other custom behavior.
     *
     * @param canDragInto if items can be dragged into this slot
     * @return this
     */
    public ModularSlot canDragInto(boolean canDragInto) {
        this.canDragInto = canDragInto;
        return this;
    }

    /**
     * Sets if the max stack size of items should be ignored. Only item handler slot limit matters if true.
     *
     * @param ignoreMaxStackSize if max stack size should be ignored
     */
    @ApiStatus.Experimental
    public ModularSlot ignoreMaxStackSize(boolean ignoreMaxStackSize) {
        this.ignoreMaxStackSize = ignoreMaxStackSize;
        return this;
    }

    /**
     * Sets a slot group for this slot by a name. The slot group must be registered.
     * The real slot group is later automatically set.
     *
     * @param slotGroup slot group id
     */
    public ModularSlot slotGroup(String slotGroup) {
        this.slotGroupName = slotGroup;
        return this;
    }

    /**
     * Sets a slot group for this slot. The slot group must be registered if it's not a singleton.
     *
     * @param slotGroup slot group
     */
    public ModularSlot slotGroup(SlotGroup slotGroup) {
        if (this.slotGroup == slotGroup) return this;
        if (this.slotGroup != null) {
            this.slotGroup.removeSlot(this);
        }
        this.slotGroup = slotGroup;
        if (this.slotGroup != null) {
            this.slotGroup.addSlot(this);
        }
        return this;
    }

    /**
     * Creates and sets a singleton slot group simply for the purpose of shift clicking into slots that don't belong to
     * a group.
     *
     * @param shiftClickPriority determines in which group a shift clicked item should be inserted first
     */
    public ModularSlot singletonSlotGroup(int shiftClickPriority) {
        this.slotGroupName = null;
        return slotGroup(SlotGroup.singleton(toString(), shiftClickPriority));
    }

    /**
     * Creates and sets a singleton slot group simply for the purpose of shift clicking into slots that don't belong to
     * a group.
     */
    public ModularSlot singletonSlotGroup() {
        return singletonSlotGroup(SlotGroup.STORAGE_SLOT_PRIO);
    }

    public static boolean isPlayerSlot(Slot slot) {
        return slot.container instanceof Inventory;
    }

    public static boolean isPlayerSlot(SlotItemHandler slot) {
        return slot.getItemHandler() instanceof PlayerInvWrapper ||
                slot.getItemHandler() instanceof PlayerMainInvWrapper ||
                slot.getItemHandler() instanceof PlayerArmorInvWrapper ||
                slot.getItemHandler() instanceof PlayerOffhandInvWrapper;
    }

    public static Player getPlayerFromInventorySlot(Slot slot) {
        return slot.container instanceof Inventory inv ? inv.player : null;
    }

    public static Player getPlayerFromInventorySlot(SlotItemHandler slot) {
        switch (slot.getItemHandler()) {
            case PlayerInvWrapper inv -> {
                for (IItemHandlerModifiable ih : ((CombinedInvWrapperAccessor) inv).getItemHandler()) {
                    if (ih instanceof PlayerMainInvWrapper mainInv) {
                        return mainInv.getInventoryPlayer().player;
                    }
                }
                return null;
            }
            case PlayerMainInvWrapper wrapper -> {
                return wrapper.getInventoryPlayer().player;
            }
            case PlayerArmorInvWrapper wrapper -> {
                return wrapper.getInventoryPlayer().player;
            }
            default -> {
            }
        }
        return null;
    }

    public static boolean isPlayerInvWrapper(IItemHandler itemHandler) {
        return itemHandler instanceof PlayerInvWrapper || itemHandler instanceof PlayerMainInvWrapper ||
                itemHandler instanceof PlayerArmorInvWrapper || itemHandler instanceof PlayerOffhandInvWrapper;
    }

    private static void validateIndex(IItemHandler itemHandler, int index) {
        if (index < 0 || index >= itemHandler.getSlots()) {
            throw new IllegalArgumentException("Tried to create a slot with invalid index " + index +
                    ". Valid index range is [0," + itemHandler.getSlots() + ")");
        }
    }

    public static boolean onlyAmountChanged(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b) && a.getCount() != b.getCount();
    }
}
