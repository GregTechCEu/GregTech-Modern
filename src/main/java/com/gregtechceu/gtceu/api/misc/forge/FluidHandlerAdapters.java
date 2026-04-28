package com.gregtechceu.gtceu.api.misc.forge;

import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.FluidResourceHandlerItemAdapter;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class FluidHandlerAdapters {

    private FluidHandlerAdapters() {}

    public static ResourceHandler<FluidResource> toResourceHandler(IFluidHandler handler) {
        return new LegacyToResource(handler);
    }

    public static IFluidHandler toFluidHandler(ResourceHandler<FluidResource> handler) {
        return handler == null ? null : new ResourceToLegacy(handler);
    }

    public static IFluidHandlerItem toFluidHandlerItem(ItemStack stack) {
        ItemAccess access = ItemAccess.forStack(stack);
        ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
        return handler == null ? null : new FluidResourceHandlerItemAdapter(handler, access);
    }

    private static final class LegacyToResource implements ResourceHandler<FluidResource> {

        private final IFluidHandler handler;
        private final HandlerJournal journal;

        private LegacyToResource(IFluidHandler handler) {
            this.handler = handler;
            this.journal = handler instanceof IFluidHandlerModifiable modifiable ? new HandlerJournal(modifiable) :
                    null;
        }

        @Override
        public int size() {
            return handler.getTanks();
        }

        @Override
        public FluidResource getResource(int slot) {
            return FluidResource.of(handler.getFluidInTank(slot));
        }

        @Override
        public long getAmountAsLong(int slot) {
            return handler.getFluidInTank(slot).getAmount();
        }

        @Override
        public long getCapacityAsLong(int slot, FluidResource resource) {
            return handler.getTankCapacity(slot);
        }

        @Override
        public boolean isValid(int slot, FluidResource resource) {
            return !resource.isEmpty() && handler.isFluidValid(slot, resource.toStack(1));
        }

        @Override
        public int insert(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0 || !isValid(slot, resource)) {
                return 0;
            }
            FluidStack stack = resource.toStack(amount);
            int inserted = handler.fill(stack, FluidAction.SIMULATE);
            if (inserted <= 0) {
                return 0;
            }
            if (journal != null) {
                journal.updateSnapshots(transaction);
            }
            return handler.fill(stack, FluidAction.EXECUTE);
        }

        @Override
        public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) {
                return 0;
            }
            FluidStack stack = resource.toStack(amount);
            FluidStack extracted = handler.drain(stack, FluidAction.SIMULATE);
            if (extracted.isEmpty()) {
                return 0;
            }
            if (journal != null) {
                journal.updateSnapshots(transaction);
            }
            return handler.drain(stack, FluidAction.EXECUTE).getAmount();
        }

        private static final class HandlerJournal extends SnapshotJournal<FluidStack[]> {

            private final IFluidHandlerModifiable handler;

            private HandlerJournal(IFluidHandlerModifiable handler) {
                this.handler = handler;
            }

            @Override
            protected FluidStack[] createSnapshot() {
                FluidStack[] snapshot = new FluidStack[handler.getTanks()];
                for (int i = 0; i < snapshot.length; i++) {
                    snapshot[i] = handler.getFluidInTank(i).copy();
                }
                return snapshot;
            }

            @Override
            protected void revertToSnapshot(FluidStack[] snapshot) {
                int tanks = Math.min(snapshot.length, handler.getTanks());
                for (int i = 0; i < tanks; i++) {
                    handler.setFluidInTank(i, snapshot[i]);
                }
            }
        }
    }

    private record ResourceToLegacy(ResourceHandler<FluidResource> handler) implements IFluidHandler {

        @Override
        public int getTanks() {
            return handler.size();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            FluidResource resource = handler.getResource(tank);
            return resource.isEmpty() ? FluidStack.EMPTY : resource.toStack(handler.getAmountAsInt(tank));
        }

        @Override
        public int getTankCapacity(int tank) {
            return handler.getCapacityAsInt(tank, handler.getResource(tank));
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return handler.isValid(tank, FluidResource.of(stack));
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return 0;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int inserted = handler.insert(FluidResource.of(resource), resource.getAmount(), transaction);
                if (action.execute()) {
                    transaction.commit();
                }
                return inserted;
            }
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = handler.extract(FluidResource.of(resource), resource.getAmount(), transaction);
                if (action.execute()) {
                    transaction.commit();
                }
                return resource.copyWithAmount(extracted);
            }
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0) {
                return FluidStack.EMPTY;
            }
            for (int slot = 0; slot < handler.size(); slot++) {
                FluidResource resource = handler.getResource(slot);
                if (!resource.isEmpty()) {
                    try (Transaction transaction = Transaction.openRoot()) {
                        int extracted = handler.extract(slot, resource, maxDrain, transaction);
                        if (action.execute()) {
                            transaction.commit();
                        }
                        return resource.toStack(extracted);
                    }
                }
            }
            return FluidStack.EMPTY;
        }
    }
}
