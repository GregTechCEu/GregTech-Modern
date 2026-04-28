package com.gregtechceu.gtceu.api.misc.forge;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class ItemHandlerAdapters {

    private ItemHandlerAdapters() {}

    public static ResourceHandler<ItemResource> toResourceHandler(IItemHandler handler) {
        return new LegacyToResource(handler);
    }

    public static IItemHandler toItemHandler(ResourceHandler<ItemResource> handler) {
        return handler == null ? null : new ResourceToLegacy(handler);
    }

    private static final class LegacyToResource implements ResourceHandler<ItemResource> {

        private final IItemHandler handler;
        private final HandlerJournal journal;

        private LegacyToResource(IItemHandler handler) {
            this.handler = handler;
            this.journal = handler instanceof IItemHandlerModifiable modifiable ? new HandlerJournal(modifiable) :
                    null;
        }

        @Override
        public int size() {
            return handler.getSlots();
        }

        @Override
        public ItemResource getResource(int slot) {
            return ItemResource.of(handler.getStackInSlot(slot));
        }

        @Override
        public long getAmountAsLong(int slot) {
            return handler.getStackInSlot(slot).getCount();
        }

        @Override
        public long getCapacityAsLong(int slot, ItemResource resource) {
            if (resource.isEmpty()) {
                return handler.getSlotLimit(slot);
            }
            return Math.min(handler.getSlotLimit(slot), resource.getMaxStackSize());
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return !resource.isEmpty() && handler.isItemValid(slot, resource.toStack(1));
        }

        @Override
        public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0 || !isValid(slot, resource)) {
                return 0;
            }
            ItemStack stack = resource.toStack(amount);
            int inserted = amount - handler.insertItem(slot, stack, true).getCount();
            if (inserted <= 0) {
                return 0;
            }
            if (journal != null) {
                journal.updateSnapshots(transaction);
            }
            return amount - handler.insertItem(slot, stack, false).getCount();
        }

        @Override
        public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0 || !resource.matches(handler.getStackInSlot(slot))) {
                return 0;
            }
            ItemStack extracted = handler.extractItem(slot, amount, true);
            if (extracted.isEmpty()) {
                return 0;
            }
            if (journal != null) {
                journal.updateSnapshots(transaction);
            }
            return handler.extractItem(slot, amount, false).getCount();
        }

        private static final class HandlerJournal extends SnapshotJournal<ItemStack[]> {

            private final IItemHandlerModifiable handler;

            private HandlerJournal(IItemHandlerModifiable handler) {
                this.handler = handler;
            }

            @Override
            protected ItemStack[] createSnapshot() {
                ItemStack[] snapshot = new ItemStack[handler.getSlots()];
                for (int i = 0; i < snapshot.length; i++) {
                    snapshot[i] = handler.getStackInSlot(i).copy();
                }
                return snapshot;
            }

            @Override
            protected void revertToSnapshot(ItemStack[] snapshot) {
                int slots = Math.min(snapshot.length, handler.getSlots());
                for (int i = 0; i < slots; i++) {
                    handler.setStackInSlot(i, snapshot[i]);
                }
            }
        }
    }

    private record ResourceToLegacy(ResourceHandler<ItemResource> handler) implements IItemHandler {

        @Override
        public int getSlots() {
            return handler.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemUtil.getStack(handler, slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return ItemUtil.insertItemReturnRemaining(handler, slot, stack, simulate, null);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemResource resource = handler.getResource(slot);
            if (resource.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int maxExtract = Math.min(amount, resource.getMaxStackSize());
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = handler.extract(slot, resource, maxExtract, transaction);
                if (!simulate) {
                    transaction.commit();
                }
                return resource.toStack(extracted);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return handler.getCapacityAsInt(slot, ItemResource.EMPTY);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return handler.isValid(slot, ItemResource.of(stack));
        }
    }
}
