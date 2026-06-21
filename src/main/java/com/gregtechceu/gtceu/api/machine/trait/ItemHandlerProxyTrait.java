package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Accessors(chain = true)
@MethodsReturnNonnullByDefault
public class ItemHandlerProxyTrait extends MachineTrait implements IItemHandlerModifiable, ICapabilityTrait {

    public static final MachineTraitType<ItemHandlerProxyTrait> TYPE = new MachineTraitType<>(
            ItemHandlerProxyTrait.class);

    @Override
    public MachineTraitType<ItemHandlerProxyTrait> getTraitType() {
        return TYPE;
    }

    @Getter
    public final IO capabilityIO;
    @Setter
    @Getter
    @Nullable
    public IItemHandlerModifiable proxy;

    public ItemHandlerProxyTrait(IO capabilityIO) {
        super();
        this.capabilityIO = capabilityIO;
    }

    //////////////////////////////////////
    // ******* Capability ********//
    //////////////////////////////////////

    @Override
    public int getSlots() {
        return proxy == null ? 0 : proxy.getSlots();
    }

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

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (proxy != null && canCapInput()) {
            return proxy.insertItem(slot, stack, simulate);
        }
        return stack;
    }

    public ItemStack insertItemInternal(int slot, ItemStack stack, boolean simulate) {
        return proxy == null ? stack : proxy.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (proxy != null && canCapOutput()) {
            return proxy.extractItem(slot, amount, simulate);
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
    public boolean isItemValid(int slot, ItemStack stack) {
        return proxy != null && proxy.isItemValid(slot, stack);
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
        var pos = getMachine().getBlockPos();
        for (Direction facing : facings) {
            var filter = getMachine().getItemCapFilter(facing, IO.OUT);
            GTTransferUtils.getAdjacentItemHandler(level, pos, facing)
                    .ifPresent(adj -> GTTransferUtils.transferItemsFiltered(this, adj, filter));
        }
    }
}
