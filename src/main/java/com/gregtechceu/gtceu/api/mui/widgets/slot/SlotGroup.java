package com.gregtechceu.gtceu.api.mui.widgets.slot;

import net.minecraft.world.inventory.Slot;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A slot group is a group of slots that can be sorted (via Inventory BogoSorter)
 * and be shift clicked into. The slot group must exist on server and client side.
 * Slot groups must be registered via
 * {@link com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager#registerSlotGroup(String, int, boolean)}
 * or overloads of the method (except it's a singleton).
 */
@Accessors(chain = true)
public class SlotGroup {

    public static final int PLAYER_INVENTORY_PRIO = 0;
    public static final int STORAGE_SLOT_PRIO = 100;

    @Getter
    private final String name;
    private final List<Slot> slots = new ArrayList<>();
    @Getter
    private final int rowSize;
    @Getter
    private final int shiftClickPriority;
    @Getter
    private final boolean allowShiftTransfer;
    @Setter
    private boolean allowSorting = true;
    @Getter
    private final boolean singleton;

    /**
     * Creates a slot group that is only a single slot. Singleton groups don't need to be registered.
     * This exists only exists so that single slots can accept items from shift clicks.
     *
     * @param name               the name of the group
     * @param shiftClickPriority determines in which group a shift clicked item should be inserted first
     * @return a new singleton slot group
     */
    public static SlotGroup singleton(String name, int shiftClickPriority) {
        return new SlotGroup(name, 1, shiftClickPriority, true, true);
    }

    public SlotGroup(String name, int rowSize) {
        this(name, rowSize, true);
    }

    public SlotGroup(String name, int rowSize, boolean allowShiftTransfer) {
        this(name, rowSize, STORAGE_SLOT_PRIO, allowShiftTransfer);
    }

    /**
     * Creates a slot group.
     *
     * @param name               the name of the group
     * @param rowSize            how many slots fit into a row in this group (assumes rectangular shape)
     * @param shiftClickPriority determines in which group a shift clicked item should be inserted first
     * @param allowShiftTransfer true if items can be shift clicked into this group
     */
    public SlotGroup(String name, int rowSize, int shiftClickPriority, boolean allowShiftTransfer) {
        this(name, rowSize, shiftClickPriority, allowShiftTransfer, false);
    }

    private SlotGroup(String name, int rowSize, int shiftClickPriority, boolean allowShiftTransfer, boolean singleton) {
        this.name = name;
        this.rowSize = rowSize;
        this.shiftClickPriority = shiftClickPriority;
        this.allowShiftTransfer = allowShiftTransfer;
        this.singleton = singleton;
    }

    @ApiStatus.Internal
    void addSlot(Slot slot) {
        this.slots.add(slot);
        if (isSingleton() && this.slots.size() > 1) {
            throw new IllegalStateException("Singleton slot group has more than one slot!");
        }
    }

    @ApiStatus.Internal
    void removeSlot(ModularSlot slot) {
        this.slots.remove(slot);
    }

    @UnmodifiableView
    public List<Slot> getSlots() {
        return Collections.unmodifiableList(this.slots);
    }

    public boolean isAllowSorting() {
        return this.slots.size() > 1 && this.allowSorting;
    }
}
