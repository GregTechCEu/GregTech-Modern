package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/3/16
 * @implNote ItemHandlerProxyTrait
 */
@Accessors(chain = true)
public class ItemHandlerProxyTrait extends MachineTrait implements IItemTransfer, ICapabilityTrait {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ItemHandlerProxyTrait.class);
    @Getter
    public final IO capabilityIO;
    @Setter @Getter @Nullable
    public IItemTransfer proxy;

    public ItemHandlerProxyTrait(MetaMachine machine, IO capabilityIO) {
        super(machine);
        this.capabilityIO = capabilityIO;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    //*******     Capability    ********//
    //////////////////////////////////////

    @Override
    public void onContentsChanged() {
        if (proxy != null) proxy.onContentsChanged();
    }

    @Override
    public int getSlots() {
        return proxy == null ? 0 : proxy.getSlots();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return proxy == null ? ItemStack.EMPTY : proxy.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int index, ItemStack stack) {
        if (proxy != null) {
            proxy.setStackInSlot(index, stack);
        }
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
        if (proxy != null && canCapInput()) {
            return proxy.insertItem(slot, stack, simulate, notifyChanges);
        }
        return stack;
    }

    public ItemStack insertItemInternal(int slot, @NotNull ItemStack stack, boolean simulate) {
        return proxy == null ? stack : proxy.insertItem(slot, stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
        if (proxy != null && canCapOutput()) {
            return proxy.extractItem(slot, amount, simulate, notifyChanges);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
        return proxy == null ? ItemStack.EMPTY : proxy.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return proxy == null ? 0 : proxy.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return proxy != null && proxy.isItemValid(slot, stack);
    }

    @NotNull
    @Override
    public Object createSnapshot() {
        return proxy != null ? proxy.createSnapshot() : new Object();
    }

    @Override
    public void restoreFromSnapshot(Object snapshot) {
        if (proxy != null) {
            proxy.restoreFromSnapshot(snapshot);
        }
    }

    public boolean isEmpty() {
        if (proxy instanceof NotifiableItemStackHandler itemStackHandler) return itemStackHandler.isEmpty();
        boolean isEmpty = true;
        if (proxy != null) {
            for (int i = 0; i < proxy.getSlots(); i++) {
                if (!proxy.getStackInSlot(i).isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    public void exportToNearby(Direction... facings) {
        if (isEmpty()) return;
        var level = getMachine().getLevel();
        var pos = getMachine().getPos();
        for (Direction facing : facings) {
            ItemTransferHelper.exportToTarget(this, Integer.MAX_VALUE, f -> true, level, pos.relative(facing), facing.getOpposite());
        }
    }

}
