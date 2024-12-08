package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.gregtechceu.gtceu.common.cover.ConveyorCover;
import com.gregtechceu.gtceu.common.cover.ItemFilterCover;
import com.gregtechceu.gtceu.common.cover.RobotArmCover;
import com.gregtechceu.gtceu.common.cover.data.DistributionMode;
import com.gregtechceu.gtceu.common.cover.data.FilterMode;
import com.gregtechceu.gtceu.utils.FacingPos;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ItemNetHandler implements IItemHandlerModifiable {

    @Getter
    private ItemPipeNet net;
    private ItemPipeBlockEntity pipe;
    private final Level world;
    @Getter
    private final Direction facing;
    private final Map<FacingPos, Integer> simulatedTransfersGlobalRoundRobin = new HashMap<>();
    private int simulatedTransfers = 0;

    private final ItemStackHandler testHandler = new ItemStackHandler(1);

    public ItemNetHandler(ItemPipeNet net, ItemPipeBlockEntity pipe, Direction facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
        this.world = pipe.getPipeLevel();
    }

    private long getLevelTime() {
        return net.getLevel().getGameTime();
    }

    public void updateNetwork(ItemPipeNet net) {
        this.net = net;
    }

    public void updatePipe(ItemPipeBlockEntity pipe) {
        this.pipe = pipe;
    }

    private void copyTransferred() {
        simulatedTransfers = pipe.getTransferredItems();
        simulatedTransfersGlobalRoundRobin.clear();
        simulatedTransfersGlobalRoundRobin.putAll(pipe.getTransferred());
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return stack;

        if (net == null || pipe == null || pipe.isInValid() || pipe.isBlocked(facing)) {
            return stack;
        }

        copyTransferred();
        CoverBehavior pipeCover = pipe.getCoverContainer().getCoverAtSide(facing);
        CoverBehavior tileCover = getCoverOnNeighbour(pipe.getPipePos(), facing);

        boolean pipeConveyor = pipeCover instanceof ConveyorCover, tileConveyor = tileCover instanceof ConveyorCover;
        // abort if there are two conveyors
        if (pipeConveyor && tileConveyor) return stack;

        if (tileCover != null && !checkImportCover(tileCover, false, stack))
            return stack;

        if (!pipeConveyor && !tileConveyor)
            return insertFirst(stack, simulate);

        ConveyorCover conveyor = (ConveyorCover) (pipeConveyor ? pipeCover : tileCover);
        if (conveyor.getIo() == (pipeConveyor ? IO.IN : IO.OUT)) {
            boolean roundRobinGlobal = conveyor.getDistributionMode() == DistributionMode.ROUND_ROBIN_GLOBAL;
            if (roundRobinGlobal || conveyor.getDistributionMode() == DistributionMode.ROUND_ROBIN_PRIO)
                return insertRoundRobin(stack, simulate, roundRobinGlobal);
        }

        return insertFirst(stack, simulate);
    }

    public static boolean checkImportCover(CoverBehavior cover, boolean onPipe, ItemStack stack) {
        if (cover == null) return true;
        if (cover instanceof ItemFilterCover filter) {
            return (filter.getFilterMode() != FilterMode.FILTER_BOTH &&
                    (filter.getFilterMode() != FilterMode.FILTER_INSERT || !onPipe) &&
                    (filter.getFilterMode() != FilterMode.FILTER_EXTRACT || onPipe)) ||
                    filter.getItemFilter().test(stack);
        }
        return true;
    }

    public ItemStack insertFirst(ItemStack stack, boolean simulate) {
        for (ItemRoutePath inv : net.getNetData(pipe.getPipePos(), facing)) {
            stack = insert(inv, stack, simulate);
            if (stack.isEmpty())
                return ItemStack.EMPTY;
        }
        return stack;
    }

    public ItemStack insertRoundRobin(ItemStack stack, boolean simulate, boolean global) {
        List<ItemRoutePath> routePaths = net.getNetData(pipe.getPipePos(), facing);
        if (routePaths.isEmpty())
            return stack;
        if (routePaths.size() == 1)
            return insert(routePaths.get(0), stack, simulate);
        List<ItemRoutePath> routePathsCopy = new ArrayList<>(routePaths);

        if (global) {
            stack = insertToHandlersEnhanced(routePathsCopy, stack, routePaths.size(), simulate);
        } else {
            stack = insertToHandlers(routePathsCopy, stack, simulate);
            if (!stack.isEmpty() && !routePathsCopy.isEmpty())
                stack = insertToHandlers(routePathsCopy, stack, simulate);
        }

        return stack;
    }

    /**
     * Inserts items equally to all handlers
     * if it couldn't insert all items, the handler will be removed
     *
     * @param copy     to insert to
     * @param stack    to insert
     * @param simulate simulate
     * @return remainder
     */
    private ItemStack insertToHandlers(List<ItemRoutePath> copy, ItemStack stack, boolean simulate) {
        Iterator<ItemRoutePath> routePathIterator = copy.listIterator();
        int inserted = 0;
        int count = stack.getCount();
        int c = count / copy.size();
        int m = c == 0 ? count % copy.size() : 0;
        while (routePathIterator.hasNext()) {
            ItemRoutePath routePath = routePathIterator.next();

            int amount = c;
            if (m > 0) {
                amount++;
                m--;
            }
            amount = Math.min(amount, stack.getCount() - inserted);
            if (amount == 0) break;
            ItemStack toInsert = stack.copy();
            toInsert.setCount(amount);
            int r = insert(routePath, toInsert, simulate).getCount();
            if (r < amount) {
                inserted += (amount - r);
            }
            if (r == 1 && c == 0 && amount == 1) {
                m++;
            }

            if (r > 0)
                routePathIterator.remove();
        }

        ItemStack remainder = stack.copy();
        remainder.setCount(count - inserted);
        return remainder;
    }

    private ItemStack insertToHandlersEnhanced(List<ItemRoutePath> copy, ItemStack stack, int dest, boolean simulate) {
        List<EnhancedRoundRobinData> transferred = new ArrayList<>();
        IntList steps = new IntArrayList();
        int min = Integer.MAX_VALUE;
        ItemStack simStack;

        // find inventories that are not full and get the amount that was inserted in total
        for (ItemRoutePath inv : copy) {
            simStack = stack.copy();
            int ins = stack.getCount() - insert(inv, simStack, true, true).getCount();
            if (ins <= 0)
                continue;
            int didTransfer = didTransferTo(inv, simulate);
            EnhancedRoundRobinData data = new EnhancedRoundRobinData(inv, ins, didTransfer);
            transferred.add(data);

            min = Math.min(min, didTransfer);

            if (!steps.contains(didTransfer)) {
                steps.add(didTransfer);
            }
        }

        if (transferred.isEmpty() || steps.isEmpty())
            return stack;

        if (!simulate && min < Integer.MAX_VALUE) {
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
            int i = 0;
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
                i++;
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
            int ins = data.toTransfer - insert(data.routePath, toInsert, simulate).getCount();
            inserted += ins;
            transferTo(data.routePath, simulate, ins);
        }

        ItemStack remainder = stack.copy();
        remainder.shrink(inserted);
        return remainder;
    }

    public ItemStack insert(ItemRoutePath handler, ItemStack stack, boolean simulate) {
        return insert(handler, stack, simulate, false);
    }

    public ItemStack insert(ItemRoutePath routePath, ItemStack stack, boolean simulate, boolean ignoreLimit) {
        int allowed = ignoreLimit ? stack.getCount() :
                checkTransferable(routePath.getProperties().getTransferRate(), stack.getCount(), simulate);
        if (allowed == 0 || !routePath.matchesFilters(stack)) {
            return stack;
        }
        CoverBehavior pipeCover = routePath.getTargetPipe().getCoverContainer()
                .getCoverAtSide(routePath.getTargetFacing());
        CoverBehavior tileCover = getCoverOnNeighbour(routePath.getTargetPipe().getPipePos(),
                routePath.getTargetFacing());

        if (pipeCover != null) {
            testHandler.setStackInSlot(0, stack.copy());
            IItemHandlerModifiable itemHandler = pipeCover.getItemHandlerCap(testHandler);
            if (itemHandler == null || (itemHandler != testHandler &&
                    (allowed = itemHandler.extractItem(0, allowed, true).getCount()) <= 0)) {
                testHandler.setStackInSlot(0, ItemStack.EMPTY);
                return stack;
            }
            testHandler.setStackInSlot(0, ItemStack.EMPTY);
        }
        IItemHandler neighbourHandler = routePath.getHandler(net.getLevel());
        if (pipeCover instanceof RobotArmCover robotArm && robotArm.getIo() == IO.OUT) {
            return insertOverRobotArm(neighbourHandler, robotArm, stack, simulate, allowed, ignoreLimit);
        }
        if (tileCover instanceof RobotArmCover robotArm && robotArm.getIo() == IO.IN) {
            return insertOverRobotArm(neighbourHandler, robotArm, stack, simulate, allowed, ignoreLimit);
        }

        return insert(neighbourHandler, stack, simulate, allowed, ignoreLimit);
    }

    private ItemStack insert(IItemHandler handler, ItemStack stack, boolean simulate, int allowed,
                             boolean ignoreLimit) {
        if (stack.getCount() == allowed) {
            ItemStack re = ItemHandlerHelper.insertItemStacked(handler, stack, simulate);
            if (!ignoreLimit)
                transfer(simulate, stack.getCount() - re.getCount());
            return re;
        }
        ItemStack toInsert = stack.copy();
        toInsert.setCount(Math.min(allowed, stack.getCount()));
        int r = ItemHandlerHelper.insertItemStacked(handler, toInsert, simulate).getCount();
        if (!ignoreLimit)
            transfer(simulate, toInsert.getCount() - r);
        ItemStack remainder = stack.copy();
        remainder.setCount(r + (stack.getCount() - toInsert.getCount()));
        return remainder;
    }

    public CoverBehavior getCoverOnNeighbour(BlockPos pos, Direction handlerFacing) {
        BlockEntity tile = pipe.getLevel().getBlockEntity(pos.relative(handlerFacing));
        if (tile != null) {
            ICoverable coverable = GTCapabilityHelper.getCoverable(pipe.getLevel(), pos.relative(handlerFacing),
                    handlerFacing.getOpposite());
            if (coverable == null) return null;
            return coverable.getCoverAtSide(handlerFacing.getOpposite());
        }
        return null;
    }

    public ItemStack insertOverRobotArm(IItemHandler handler, RobotArmCover arm, ItemStack stack, boolean simulate,
                                        int allowed, boolean ignoreLimit) {
        int rate;
        boolean isStackSpecific = false;
        rate = arm.getFilterHandler().getFilter().testItemCount(stack);
        int count;
        switch (arm.getTransferMode()) {
            case TRANSFER_ANY:
                return insert(handler, stack, simulate, allowed, ignoreLimit);
            case KEEP_EXACT:
                count = rate - countStack(handler, stack, arm, isStackSpecific);
                if (count <= 0) return stack;
                count = Math.min(allowed, Math.min(stack.getCount(), count));
                return insert(handler, stack, simulate, count, ignoreLimit);
            case TRANSFER_EXACT:
                int max = allowed + arm.getBuffer();
                count = Math.min(max, Math.min(rate, stack.getCount()));
                if (count < rate) {
                    arm.buffer(allowed);
                    return stack;
                } else {
                    arm.clearBuffer();
                }
                if (insert(handler, stack, true, count, ignoreLimit).getCount() != stack.getCount() - count) {
                    return stack;
                }
                return insert(handler, stack, simulate, count, ignoreLimit);
        }
        return stack;
    }

    public static int countStack(IItemHandler handler, ItemStack stack, RobotArmCover arm, boolean isStackSpecific) {
        if (arm == null) return 0;
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack slot = handler.getStackInSlot(i);
            if (slot.isEmpty()) continue;
            if (isStackSpecific ? ItemStackHashStrategy.comparingAllButCount().equals(stack, slot) :
                    arm.getFilterHandler().getFilter().test(slot)) {
                count += slot.getCount();
            }
        }
        return count;
    }

    private int checkTransferable(float rate, int amount, boolean simulate) {
        int max = (int) ((rate * 64) + 0.5);
        if (simulate)
            return Math.max(0, Math.min(max - simulatedTransfers, amount));
        else
            return Math.max(0, Math.min(max - pipe.getTransferredItems(), amount));
    }

    private void transfer(boolean simulate, int amount) {
        if (simulate)
            simulatedTransfers += amount;
        else
            pipe.addTransferredItems(amount);
    }

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

    private void transferTo(ItemRoutePath handler, boolean simulate, int amount) {
        if (simulate)
            simulatedTransfersGlobalRoundRobin.merge(handler.toFacingPos(), amount, Integer::sum);
        else
            pipe.getTransferred().merge(handler.toFacingPos(), amount, Integer::sum);
    }

    private boolean contains(ItemRoutePath handler, boolean simulate) {
        return simulate ? simulatedTransfersGlobalRoundRobin.containsKey(handler.toFacingPos()) :
                pipe.getTransferred().containsKey(handler.toFacingPos());
    }

    private int didTransferTo(ItemRoutePath handler, boolean simulate) {
        if (simulate)
            return simulatedTransfersGlobalRoundRobin.getOrDefault(handler.toFacingPos(), 0);
        return pipe.getTransferred().getOrDefault(handler.toFacingPos(), 0);
    }

    private void resetTransferred(boolean simulated) {
        if (simulated)
            simulatedTransfersGlobalRoundRobin.clear();
        else
            pipe.resetTransferred();
    }

    private void decrementBy(int amount) {
        for (Map.Entry<FacingPos, Integer> entry : pipe.getTransferred().entrySet()) {
            entry.setValue(entry.getValue() - amount);
        }
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {}

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
}
