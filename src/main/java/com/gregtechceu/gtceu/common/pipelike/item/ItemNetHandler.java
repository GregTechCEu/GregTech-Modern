package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.filter.Filter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.transfer.item.IVirtualItemHandler;
import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.gregtechceu.gtceu.common.cover.ConveyorCover;
import com.gregtechceu.gtceu.common.cover.RobotArmCover;
import com.gregtechceu.gtceu.utils.FacingPos;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.ToIntFunction;

public class ItemNetHandler implements IItemHandlerModifiable, IVirtualItemHandler {

    @Getter
    @Setter
    private ItemPipeNet network;
    private final ItemPipeBlockEntity pipe;
    @Getter
    private final Direction facing;
    private final Object2IntOpenHashMap<FacingPos> simulatedTransfersGlobalRoundRobin = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<ItemRoutePath> simulatedTransfers = new Object2IntOpenHashMap<>();

    public ItemNetHandler(ItemPipeNet net, ItemPipeBlockEntity pipe, Direction facing) {
        this.network = net;
        this.pipe = pipe;
        this.facing = facing;
    }

    /// Attempt to insert an item stack onto the pipe network.
    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return handleInsert(stack, simulate, InsertionMode.SPLIT);
    }

    @NotNull
    @Override
    public ItemStack insertItemBundle(@NotNull ItemStack stack, boolean simulate) {
        return handleInsert(stack, simulate, InsertionMode.ATOMIC);
    }

    @Override
    public int stockInventoryItems(IItemHandler sourceInventory, int maxTransferAmount,
                                   ToIntFunction<ItemStack> itemKeepAmountProvider) {
        if (network == null || pipe == null || pipe.isRemoved() || pipe.isBlocked(facing) ||
                network.isTransferringItem()) {
            return 0;
        }

        try {
            // Protect against recursive transfers via external item handlers
            network.setTransferringItem(true);

            CoverBehavior sourcePipeCover = pipe.getCoverContainer().getCoverAtSide(facing);
            CoverBehavior sourceBlockCover = getCoverOnNeighbour(pipe.getBlockPos(), facing);

            // abort if there are two conveyors
            if (sourcePipeCover instanceof ConveyorCover && sourceBlockCover instanceof ConveyorCover) return 0;

            List<ItemRoutePath> routePaths = network.getNetData(pipe.getBlockPos(), facing, ItemRoutePathSet.FULL);

            // Distribution mode does not matter for stocking - we just loop all route paths and attempt to stock all of
            // them
            int remainingTransferAmount = maxTransferAmount;
            for (ItemRoutePath routePath : routePaths) {

                // Restrict the maximum transfer amount for the route
                int routeMaxTransferAmount = checkTransferable(routePath.getProperties().getTransferRate(),
                        remainingTransferAmount, false);
                if (routeMaxTransferAmount == 0)
                    continue;

                IItemHandler neighbourHandler = routePath.getHandler(network.getLevel());
                if (neighbourHandler == null)
                    continue;

                CoverBehavior destinationPipeCover = routePath.getTargetPipe().getCoverContainer()
                        .getCoverAtSide(routePath.getTargetFacing());

                // Wrap the original item amount provider with route-specific filter
                int transferredItemAmount = GTTransferUtils.stockInventoryItems(sourceInventory, neighbourHandler,
                        routeMaxTransferAmount, itemStack -> getItemKeepAmountViaRoute(
                                itemStack, destinationPipeCover, routePath, itemKeepAmountProvider));

                if (transferredItemAmount > 0) {
                    // Account for the transfer in the pipe transfer limit and external max transfer amount
                    remainingTransferAmount -= transferredItemAmount;
                    transfer(false, transferredItemAmount);

                    if (remainingTransferAmount <= 0)
                        break;
                }
            }

            return maxTransferAmount - remainingTransferAmount;
        } finally {
            network.setTransferringItem(false);
        }
    }

    private int getItemKeepAmountViaRoute(ItemStack itemStack,
                                          CoverBehavior destinationPipeCover,
                                          ItemRoutePath routePath,
                                          ToIntFunction<ItemStack> sourceItemKeepAmountProvider) {
        // Evaluate route filters
        if (!routePath.matchesFilters(itemStack))
            return 0;

        // Evaluate filter on destination item pipe (route filters do not include source/destination)
        if (destinationPipeCover != null) {
            var defaultHandler = new ItemStackHandler(1);
            defaultHandler.setStackInSlot(0, itemStack.copyWithCount(1));
            IItemHandlerModifiable itemHandler = destinationPipeCover.getItemHandlerCap(defaultHandler);

            // If cover returns null item handler, it disallows item transfer explicitly
            if (itemHandler == null)
                return 0;

            // Cover might prevent insertion of this particular item type, or restrict the amount
            // We ignore amount restrictions when stocking, so the only case that matters is item type being explicitly
            // disallowed
            if (itemHandler != defaultHandler && itemHandler.extractItem(0, 1, true).getCount() <= 0)
                return 0;
        }

        // Evaluate the original item amount provider
        return sourceItemKeepAmountProvider.applyAsInt(itemStack);
    }

    private ItemStack handleInsert(ItemStack stack, boolean simulate, InsertionMode mode) {
        if (stack.isEmpty()) return stack;

        if (network == null || pipe == null || pipe.isRemoved() || pipe.isBlocked(facing) ||
                network.isTransferringItem()) {
            return stack;
        }

        try {
            // Protect against recursive transfers via external item handlers
            network.setTransferringItem(true);

            simulatedTransfers.clear();
            simulatedTransfers.putAll(pipe.getTransferredItems());
            simulatedTransfersGlobalRoundRobin.clear();
            simulatedTransfersGlobalRoundRobin.putAll(pipe.getTransferredGlobalRoundRobin());

            CoverBehavior pipeCover = pipe.getCoverContainer().getCoverAtSide(facing);
            CoverBehavior tileCover = getCoverOnNeighbour(pipe.getBlockPos(), facing);
            ConveyorCover conveyor = null;

            // abort if there are two conveyors
            if (pipeCover instanceof ConveyorCover && tileCover instanceof ConveyorCover) return stack;

            if (pipeCover instanceof ConveyorCover pipeConveyor) conveyor = pipeConveyor;
            if (tileCover instanceof ConveyorCover tileConveyor) conveyor = tileConveyor;

            List<ItemRoutePath> routePaths = network.getNetData(pipe.getBlockPos(), facing, ItemRoutePathSet.FULL);
            if (routePaths.isEmpty()) return stack;
            List<ItemRoutePath> routePathsCopy = new ArrayList<>(routePaths);

            if (conveyor == null) return distributeHighestPriority(routePathsCopy, stack, simulate, mode);

            return switch (conveyor.getDistributionMode()) {
                case INSERT_FIRST -> distributeHighestPriority(routePathsCopy, stack, simulate, mode);
                case ROUND_ROBIN_GLOBAL -> distributeEqually(routePathsCopy, stack, simulate, mode);
                case ROUND_ROBIN_PRIO -> distributeEquallyNoRestrictive(stack, simulate, mode);
            };
        } finally {
            network.setTransferringItem(false);
        }
    }

    /**
     * Distributes items to handlers, attempting to fill handlers with a higher priority first
     */
    private ItemStack distributeHighestPriority(List<ItemRoutePath> copy, ItemStack stack, boolean simulate,
                                                InsertionMode mode) {
        return insertOrdered(copy, stack, simulate, mode, false);
    }

    /// {@code trackFairness} records round-robin bookkeeping for the destination that ends up receiving items,
    /// so later calls keep rotating fairly.
    private ItemStack insertOrdered(List<ItemRoutePath> routePaths, ItemStack stack, boolean simulate,
                                    InsertionMode mode, boolean trackFairness) {
        for (ItemRoutePath inv : routePaths) {
            if (mode == InsertionMode.SPLIT) {
                stack = insertIntoTarget(inv, stack, simulate, false);
                if (stack.isEmpty()) return ItemStack.EMPTY;
            } else if (insertIntoTarget(inv, stack, true, false).isEmpty()) {
                if (!simulate) {
                    insertIntoTarget(inv, stack, false, false);
                }
                if (trackFairness) {
                    transferTo(inv, simulate, stack);
                }
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    /////////////////////////////////////
    // *** DISTRIBUTION MODES ***//
    /////////////////////////////////////

    /**
     * Distributes items evenly to multiple handlers. Attempts to exclude handlers that are behind Restrictive Pipes,
     * unless no other routes are available.
     * Does not take in a list of routes, pulls a copy of the routes if it needs it
     *
     * @param stack    the {@link ItemStack} to insert
     * @param simulate simulate
     * @param mode     see {@link InsertionMode}
     * @return any remaining items not inserted
     */
    private ItemStack distributeEquallyNoRestrictive(ItemStack stack, boolean simulate, InsertionMode mode) {
        // Round-robin distribute to all non-Restrictive destinations
        List<ItemRoutePath> routePathsNonRestrictedCopy = new ArrayList<>(
                network.getNetData(pipe.getBlockPos(), facing, ItemRoutePathSet.NONRESTRICTED));
        ItemStack remainsNonRestricted;
        if (routePathsNonRestrictedCopy.isEmpty()) {
            remainsNonRestricted = stack;
        } else {
            remainsNonRestricted = distributeEqually(routePathsNonRestrictedCopy, stack, simulate, mode);
        }
        // if anything is left, distribute to Restrictive destinations
        if (!remainsNonRestricted.isEmpty()) {
            List<ItemRoutePath> routePathsRestrictiveCopy = new ArrayList<>(
                    network.getNetData(pipe.getBlockPos(), facing, ItemRoutePathSet.RESTRICTED));
            return distributeEqually(routePathsRestrictiveCopy, remainsNonRestricted, simulate, mode);
        } else {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Equally distributes items to all handlers.
     *
     * @param routePaths to insert to
     * @param stack      to insert
     * @param simulate   simulate
     * @param mode       see {@link InsertionMode}
     * @return remainder
     */
    private ItemStack distributeEqually(List<ItemRoutePath> routePaths, ItemStack stack, boolean simulate,
                                        InsertionMode mode) {
        // Since atomic mode cannot split the given itemstack over multiple destinations, we just cycle the destinations
        // for it
        if (mode == InsertionMode.ATOMIC) {
            routePaths.sort(Comparator.comparingInt(inv -> didTransferTo(inv, simulate)));
            return insertOrdered(routePaths, stack, simulate, InsertionMode.ATOMIC, true);
        }

        List<EnhancedRoundRobinData> transferred = new ArrayList<>();
        IntList steps = new IntArrayList();
        int min = Integer.MAX_VALUE;
        ItemStack simStack;

        // find inventories that are not full and get the amount that was inserted in total
        for (ItemRoutePath inv : routePaths) {
            simStack = stack.copy();
            int ins = stack.getCount() - insertIntoTarget(inv, simStack, true, true).getCount();
            if (ins <= 0) continue;

            int didTransfer = didTransferTo(inv, simulate);
            EnhancedRoundRobinData data = new EnhancedRoundRobinData(inv, ins, didTransfer);
            transferred.add(data);

            min = Math.min(min, didTransfer);

            if (!steps.contains(didTransfer)) {
                steps.add(didTransfer);
            }
        }

        if (transferred.isEmpty() || steps.isEmpty()) {
            return stack;
        }

        if (!simulate && min < Integer.MAX_VALUE) {
            // min is already multiplied by the transferred stacks' max size factors
            decrementBy(min);
        }

        transferred.sort(Comparator.comparingInt(data -> data.transferred));
        steps.sort(Integer::compare);

        if (transferred.get(0).transferred != steps.getInt(0)) {
            return stack;
        }

        int amount = stack.getCount();
        int c = amount / transferred.size();
        int m = amount % transferred.size();
        List<EnhancedRoundRobinData> transferredCopy = new ArrayList<>(transferred);
        int nextStep = steps.removeInt(0);

        // equally distribute items over all inventories
        // it takes into account how much was inserted in total
        // f.e. if inv1 has 2 inserted and inv2 has 6 inserted, it will first try to insert 4 into inv1 so that both
        // have 6 and then it will distribute the rest equally
        outer:
        while (amount > 0 && !transferredCopy.isEmpty()) {
            Iterator<EnhancedRoundRobinData> iterator = transferredCopy.iterator();
            while (iterator.hasNext()) {
                EnhancedRoundRobinData data = iterator.next();
                if (nextStep >= 0 && data.transferred >= nextStep)
                    break;

                int toInsert;
                if (nextStep <= 0) {
                    if (amount <= m) {
                        // break outer;
                        toInsert = 1;
                    } else {
                        toInsert = Math.min(c, amount);
                    }
                } else {
                    toInsert = Math.min(amount, nextStep - data.transferred);
                }
                if (data.toTransfer + toInsert >= data.maxInsertable) {
                    data.toTransfer = data.maxInsertable;
                    iterator.remove();
                } else {
                    data.toTransfer += toInsert;
                }

                data.transferred += toInsert;

                if ((amount -= toInsert) == 0) {
                    break outer;
                }
            }

            for (EnhancedRoundRobinData data : transferredCopy) {
                if (data.transferred < nextStep)
                    continue outer;
            }
            if (steps.isEmpty()) {
                if (nextStep >= 0) {
                    c = amount / transferredCopy.size();
                    m = amount % transferredCopy.size();
                    nextStep = -1;
                }
            } else {
                nextStep = steps.removeInt(0);
            }
        }

        int inserted = 0;

        // finally actually insert the item
        for (EnhancedRoundRobinData data : transferred) {
            ItemStack toInsert = stack.copy();
            toInsert.setCount(data.toTransfer);
            ItemStack ins = insertIntoTarget(data.routePath, toInsert, simulate, false);
            int insCount = data.toTransfer - ins.getCount();

            inserted += ins.getCount();
            if (insCount > 0) transferTo(data.routePath, simulate, ins.copyWithCount(insCount));
        }

        ItemStack remainder = stack.copy();
        remainder.shrink(inserted);
        return remainder;
    }

    /////////////////////////////////////
    // *** ENDPOINT INSERTION LOGIC ***//
    /////////////////////////////////////

    /// Insert items into a target inventory using the specified route
    private ItemStack insertIntoTarget(ItemRoutePath routePath, ItemStack stack, boolean simulate,
                                       boolean ignoreLimit) {
        int allowed = ignoreLimit ? stack.getCount() : checkTransferable(routePath, stack, simulate);
        if (allowed <= 0 || !routePath.matchesFilters(stack)) {
            return stack;
        }

        CoverBehavior pipeCover = routePath.getTargetPipe().getCoverContainer()
                .getCoverAtSide(routePath.getTargetFacing());
        CoverBehavior tileCover = getCoverOnNeighbour(routePath.getTargetPipe().getBlockPos(),
                routePath.getTargetFacing());

        if (pipeCover != null) {
            var defaultHandler = new ItemStackHandler(1);
            defaultHandler.setStackInSlot(0, stack.copy());
            IItemHandlerModifiable itemHandler = pipeCover.getItemHandlerCap(defaultHandler);
            if (itemHandler == null) {
                return stack;
            } else if (itemHandler != defaultHandler) {
                allowed = itemHandler.extractItem(0, allowed, true).getCount();
                if (allowed <= 0) {
                    return stack;
                }
            }
        }
        IItemHandler neighbourHandler = routePath.getHandler(network.getLevel());
        if (pipeCover instanceof RobotArmCover robotArm && robotArm.getIo() == IO.OUT) {
            return insertOverRobotArm(routePath, neighbourHandler, robotArm, stack, simulate, allowed, ignoreLimit);
        }
        if (tileCover instanceof RobotArmCover robotArm && robotArm.getIo() == IO.IN) {
            return insertOverRobotArm(routePath, neighbourHandler, robotArm, stack, simulate, allowed, ignoreLimit);
        }

        return insertIntoDestination(routePath, neighbourHandler, stack, simulate, allowed, ignoreLimit);
    }

    /// Insert into the actual destination
    private ItemStack insertIntoDestination(ItemRoutePath routePath, IItemHandler handler, ItemStack stack,
                                            boolean simulate, int allowed, boolean ignoreLimit) {
        // if the stack has exactly the allowed amount of items, just do the transfer
        if (stack.getCount() == allowed) {
            ItemStack rem = ItemHandlerHelper.insertItemStacked(handler, stack, simulate);
            if (!ignoreLimit) {
                transfer(routePath, stack, rem, simulate);
            }
            return rem;
        }

        // otherwise try to transfer at most the allowed amount of items, accounting for bigger/smaller stacks
        ItemStack toInsert = stack.copyWithCount(Math.min(allowed, stack.getCount()));
        ItemStack rem = ItemHandlerHelper.insertItemStacked(handler, toInsert, simulate);
        if (!ignoreLimit) {
            transfer(routePath, toInsert, rem, simulate);
        }

        ItemStack remainder = stack.copy();
        remainder.setCount(rem.getCount() + (stack.getCount() - toInsert.getCount()));
        return remainder;
    }

    /// Insert into a destination through a robot arm
    private ItemStack insertOverRobotArm(ItemRoutePath routePath, IItemHandler handler, RobotArmCover arm,
                                         ItemStack stack, boolean simulate, int allowed, boolean ignoreLimit) {
        int rate = arm.getFilterHandler().getFilter().supportsAmounts() ?
                arm.getFilterHandler().getFilter().testAmount(stack) : Integer.MAX_VALUE;
        int count;
        switch (arm.getTransferMode()) {
            case TRANSFER_ANY:
                return insertIntoDestination(routePath, handler, stack, simulate, allowed, ignoreLimit);
            case KEEP_EXACT:
                if (rate == Integer.MAX_VALUE) {
                    rate = arm.getGlobalTransferLimit();
                }
                count = rate - countStack(handler, stack, arm);
                if (count <= 0) return stack;

                count = Math.min(allowed, Math.min(stack.getCount(), count));
                return insertIntoDestination(routePath, handler, stack, simulate, count, ignoreLimit);
            case TRANSFER_EXACT:
                int max = allowed + arm.getBuffer();
                count = Math.min(max, Math.min(rate, stack.getCount()));
                if (count < rate) {
                    arm.buffer(allowed);
                    return stack;
                } else {
                    arm.clearBuffer();
                }

                int inserted = insertIntoDestination(routePath, handler, stack, true, count, ignoreLimit).getCount();
                if (inserted != (stack.getCount() - count)) {
                    return stack;
                }
                return insertIntoDestination(routePath, handler, stack, simulate, count, ignoreLimit);
        }
        return stack;
    }

    public static int countStack(IItemHandler handler, ItemStack stack, RobotArmCover arm) {
        if (arm == null) return 0;
        int count = 0;
        Filter<ItemStack> filter = arm.getFilterHandler().getFilter();
        boolean ignoreNBT = filter instanceof SimpleItemFilter simple && simple.isIgnoreNbt();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack slot = handler.getStackInSlot(i);
            if (slot.isEmpty()) continue;
            if (ignoreNBT && !ItemStack.isSameItem(stack, slot)) continue;
            if (!ignoreNBT && !GTUtil.isSameItemSameTags(stack, slot)) continue;
            if (arm.getFilterHandler().getFilter().test(slot)) {
                count += slot.getCount();
            }
        }
        return count;
    }

    public CoverBehavior getCoverOnNeighbour(BlockPos pos, Direction handlerFacing) {
        var level = pipe.getLevel();
        if (level == null) return null;
        BlockEntity tile = pipe.getLevel().getBlockEntity(pos.relative(handlerFacing));
        if (tile == null) return null;

        ICoverable coverable = GTCapabilityHelper.getCoverable(pipe.getLevel(), pos.relative(handlerFacing),
                handlerFacing.getOpposite());
        if (coverable == null) return null;
        return coverable.getCoverAtSide(handlerFacing.getOpposite());
    }

    private int getTotalSimulatedTransfers() {
        return this.simulatedTransfers.values().intStream().sum();
    }

    private int checkTransferable(ItemRoutePath routePath, ItemStack stack, boolean simulate) {
        int max = Math.round(routePath.getProperties().getTransferRate() * Item.MAX_STACK_SIZE);
        int amount = stack.getCount() * (Item.MAX_STACK_SIZE / stack.getMaxStackSize());
        // ensure items with maxStackSize > 64 aren't free
        if (!stack.isEmpty()) amount = Math.max(1, amount);

        if (simulate) {
            return Math.max(0, Math.min(max - getTotalSimulatedTransfers(), amount));
        } else {
            return Math.max(0, Math.min(max - pipe.getTransferredItemCount(), amount));
        }
    }

    private void transfer(ItemRoutePath routePath, ItemStack stack, ItemStack remainder, boolean simulate) {
        int stackAmount = stack.getCount() * (Item.MAX_STACK_SIZE / stack.getMaxStackSize());
        // ensure items with maxStackSize > 64 aren't free
        if (!stack.isEmpty()) stackAmount = Math.max(1, stackAmount);
        int remainderAmount = remainder.getCount() * (Item.MAX_STACK_SIZE / remainder.getMaxStackSize());
        if (!remainder.isEmpty()) remainderAmount = Math.max(1, remainderAmount);

        int amount = stackAmount - remainderAmount;
        if (simulate) {
            simulatedTransfers.addTo(routePath, amount);
        } else {
            pipe.getTransferredItems().addTo(routePath, amount);
        }
    }

    private void transferTo(ItemRoutePath handler, boolean simulate, ItemStack stack) {
        int amount = stack.getCount() * (Item.MAX_STACK_SIZE / stack.getMaxStackSize());
        // ensure items with maxStackSize > 64 aren't free
        if (!stack.isEmpty()) amount = Math.max(1, amount);

        if (simulate) {
            simulatedTransfersGlobalRoundRobin.addTo(handler.toFacingPos(), amount);
        } else {
            pipe.getTransferredGlobalRoundRobin().addTo(handler.toFacingPos(), amount);
        }
    }

    private int didTransferTo(ItemRoutePath handler, boolean simulate) {
        if (simulate) {
            return simulatedTransfersGlobalRoundRobin.getOrDefault(handler.toFacingPos(), 0);
        } else {
            return pipe.getTransferredGlobalRoundRobin().getOrDefault(handler.toFacingPos(), 0);
        }
    }

    private void decrementBy(int amount) {
        for (var entry : pipe.getTransferredGlobalRoundRobin().object2IntEntrySet()) {
            entry.setValue(entry.getIntValue() - amount);
        }
    }

    private enum InsertionMode {
        /** Stack being inserted can be split over multiple destinations */
        SPLIT,
        /**
         * The inserted stack should be treated as one logical item, and should not be split over multiple destinations,
         * or partially inserted
         */
        ATOMIC
    }

    private static class EnhancedRoundRobinData {

        private final ItemRoutePath routePath;
        private final int maxInsertable;
        private int transferred;
        private int toTransfer = 0;

        private EnhancedRoundRobinData(ItemRoutePath routePath, int maxInsertable, int transferred) {
            this.maxInsertable = maxInsertable;
            this.transferred = transferred;
            this.routePath = routePath;
        }
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {}

    @Override
    public int getSlots() {
        return 1;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int i) {
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int i) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }
}
