package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerList;
import com.gregtechceu.gtceu.api.transfer.item.IBundleInsertable;
import com.gregtechceu.gtceu.api.transfer.item.ITransferAmountLimiter;
import com.gregtechceu.gtceu.api.transfer.item.IVirtualItemHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class GTTransferUtils {

    /**
     * Gets the FluidHandler from the adjacent block on the side connected to the caller
     *
     * @param level  Level of caller
     * @param pos    BlockPos of caller
     * @param facing Direction to get the FluidHandler from
     * @return LazyOpt of the IFluidHandler described above
     */
    public static LazyOptional<IFluidHandler> getAdjacentFluidHandler(Level level, BlockPos pos, Direction facing) {
        return FluidUtil.getFluidHandler(level, pos.relative(facing), facing.getOpposite());
    }

    // Same as above, but returns the presence
    public static boolean hasAdjacentFluidHandler(Level level, BlockPos pos, Direction facing) {
        return getAdjacentFluidHandler(level, pos, facing).isPresent();
    }

    /**
     * Get the ItemHandler Capability from the given block
     * 
     * @param level Level of block
     * @param pos   BlockPos of block
     * @param side  Side of block
     * @return LazyOpt of ItemHandler of given block
     */
    public static LazyOptional<IItemHandler> getItemHandler(Level level, BlockPos pos, @Nullable Direction side) {
        BlockState state = level.getBlockState(pos);
        if (state.hasBlockEntity()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side);
            }
        }
        return LazyOptional.empty();
    }

    // Same as getAdjacentFluidHandler, but for ItemHandler
    public static LazyOptional<IItemHandler> getAdjacentItemHandler(Level level, BlockPos pos, Direction facing) {
        return getItemHandler(level, pos.relative(facing), facing.getOpposite());
    }

    // Same as above, but returns the presence
    public static boolean hasAdjacentItemHandler(Level level, BlockPos pos, Direction facing) {
        return getAdjacentItemHandler(level, pos, facing).isPresent();
    }

    /**
     * Transfer fluids with the given filter
     * 
     * @param source        FluidHandler to drain from
     * @param dest          FluidHandler to fill
     * @param filter        Filter to test FluidStacks
     * @param transferLimit Amount to transfer
     * @return Remaining amount that was not transferred
     */
    public static int transferFluidsFiltered(@NotNull IFluidHandler source, @NotNull IFluidHandler dest,
                                             @NotNull Predicate<FluidStack> filter, int transferLimit) {
        int toTransfer = transferLimit;
        for (int i = 0; i < source.getTanks(); i++) {
            FluidStack fluid = source.getFluidInTank(i);
            if (fluid.isEmpty() || !filter.test(fluid)) continue;

            fluid = new FluidStack(fluid, toTransfer);
            var transferred = FluidUtil.tryFluidTransfer(dest, source, fluid, true);
            toTransfer -= transferred.getAmount();
            if (toTransfer <= 0) break;
        }
        return transferLimit - toTransfer;
    }

    // Override to transfer as much as possible
    public static void transferFluidsFiltered(@NotNull IFluidHandler source, @NotNull IFluidHandler dest,
                                              @NotNull Predicate<FluidStack> filter) {
        transferFluidsFiltered(source, dest, filter, Integer.MAX_VALUE);
    }

    /**
     * Transfer items with the given filter
     * 
     * @param source        ItemHandler to extract from
     * @param dest          ItemHandler to insert into
     * @param filter        Filter to test ItemStacks
     * @param transferLimit Maximum amount to transfer
     * @return Remaining amount that wasn't transferred
     */
    public static int transferItemsFiltered(@NotNull IItemHandler source, @NotNull IItemHandler dest,
                                            @NotNull Predicate<ItemStack> filter, int transferLimit) {
        int toTransfer = transferLimit;
        for (int i = 0; i < source.getSlots(); i++) {
            ItemStack stack = source.getStackInSlot(i);
            if (stack.isEmpty() || !filter.test(stack)) continue;

            var canExtract = source.extractItem(i, toTransfer, true);
            if (canExtract.isEmpty()) continue;
            int canInsert = canExtract.getCount() -
                    ItemHandlerHelper.insertItemStacked(dest, canExtract, true).getCount();
            if (canInsert > 0) {
                var extracted = source.extractItem(i, canInsert, false);
                var remainder = ItemHandlerHelper.insertItemStacked(dest, extracted, false);
                toTransfer -= (canInsert - remainder.getCount());
                if (toTransfer <= 0) break;
            }
        }
        return transferLimit - toTransfer;
    }

    // Override to transfer as much as possible
    public static void transferItemsFiltered(@NotNull IItemHandler source, @NotNull IItemHandler dest,
                                             @NotNull Predicate<ItemStack> filter) {
        transferItemsFiltered(source, dest, filter, Integer.MAX_VALUE);
    }

    public static void moveInventoryItems(IItemHandlerModifiable sourceInventory,
                                          IItemHandlerModifiable targetInventory) {
        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty()) {
                continue;
            }
            ItemStack remainder = insertItem(targetInventory, sourceStack, true);
            int amountToInsert = sourceStack.getCount() - remainder.getCount();
            if (amountToInsert > 0) {
                sourceStack = sourceInventory.extractItem(srcIndex, amountToInsert, false);
                insertItem(targetInventory, sourceStack, false);
            }
        }
    }

    /**
     * Simulates the insertion of items into a target inventory, then optionally performs the insertion.
     * <br />
     * <br />
     * Simulating will not modify any of the input parameters. Insertion will either succeed completely, or fail
     * without modifying anything.
     * This method should be called with {@code simulate} {@code true} first, then {@code simulate} {@code false},
     * only if it returned {@code true}.
     *
     * @param handler  the target inventory
     * @param simulate whether to simulate ({@code true}) or actually perform the insertion ({@code false})
     * @param items    the items to insert into {@code handler}.
     * @return {@code true} if the insertion succeeded, {@code false} otherwise.
     */
    public static boolean addItemsToItemHandler(final IItemHandlerModifiable handler,
                                                final boolean simulate,
                                                final List<ItemStack> items) {
        // determine if there is sufficient room to insert all items into the target inventory
        if (simulate) {
            OverlayedItemHandler overlayedItemHandler = new OverlayedItemHandler(handler);
            Object2IntMap<ItemStack> stackKeyMap = GTHashMaps.fromItemStackCollection(items);

            for (Object2IntMap.Entry<ItemStack> entry : stackKeyMap.object2IntEntrySet()) {
                int amountToInsert = entry.getIntValue();
                int amount = overlayedItemHandler.insertStackedItemStack(entry.getKey(), amountToInsert);
                if (amount > 0) {
                    return false;
                }
            }
            return true;
        }

        // perform the merge.
        items.forEach(stack -> ItemHandlerHelper.insertItemStacked(handler, stack, false));
        return true;
    }

    public static int fillFluidAccountNotifiableList(IFluidHandler fluidHandler, FluidStack stack, FluidAction action) {
        if (stack.isEmpty()) return 0;
        if (fluidHandler instanceof FluidHandlerList handlerList) {
            var copied = stack.copy();
            for (var handler : handlerList.handlers) {
                var candidate = copied.copy();
                if (handler instanceof NotifiableFluidTank notifiable) {
                    copied.shrink(notifiable.fillInternal(candidate, action));
                } else {
                    copied.shrink(handler.fill(candidate, action));
                }
                if (copied.isEmpty()) break;
            }
            return stack.getAmount() - copied.getAmount();
        }
        return fluidHandler.fill(stack, action);
    }

    public static FluidStack drainFluidAccountNotifiableList(IFluidHandler fluidHandler, FluidStack stack,
                                                             FluidAction action) {
        if (stack.isEmpty()) return FluidStack.EMPTY;
        if (fluidHandler instanceof FluidHandlerList handlerList) {
            var copied = stack.copy();
            for (var handler : handlerList.handlers) {
                var candidate = copied.copy();
                if (handler instanceof NotifiableFluidTank notifiable) {
                    copied.shrink(notifiable.drainInternal(candidate, action).getAmount());
                } else {
                    copied.shrink(handler.drain(candidate, action).getAmount());
                }
                if (copied.isEmpty()) break;
            }
            copied.setAmount(stack.getAmount() - copied.getAmount());
            return copied;
        }
        return fluidHandler.drain(stack, action);
    }

    public static boolean transferExactFluidStack(@NotNull IFluidHandler sourceHandler,
                                                  @NotNull IFluidHandler destHandler, FluidStack fluidStack) {
        int amount = fluidStack.getAmount();
        FluidStack sourceFluid = sourceHandler.drain(fluidStack, FluidAction.SIMULATE);
        if (sourceFluid == FluidStack.EMPTY || sourceFluid.getAmount() != amount) {
            return false;
        }
        int canInsertAmount = destHandler.fill(sourceFluid, FluidAction.SIMULATE);
        if (canInsertAmount == amount) {
            sourceFluid = sourceHandler.drain(sourceFluid, FluidAction.EXECUTE);
            if (sourceFluid != FluidStack.EMPTY && sourceFluid.getAmount() > 0) {
                destHandler.fill(sourceFluid, FluidAction.EXECUTE);
                return true;
            }
        }
        return false;
    }

    /**
     * Inserts items by trying to fill slots with the same item first, and then fill empty slots. <br>
     * Seems like close to duplicate behavior of {@link ItemHandlerHelper#insertItemStacked}
     */
    public static ItemStack insertItem(IItemHandler handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        if (!stack.isStackable()) {
            return insertToEmpty(handler, stack, simulate);
        }

        IntList emptySlots = new IntArrayList();
        int slots = handler.getSlots();

        for (int i = 0; i < slots; i++) {
            ItemStack slotStack = handler.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                emptySlots.add(i);
            } else if (ItemHandlerHelper.canItemStacksStack(stack, slotStack)) {
                stack = handler.insertItem(i, stack, simulate);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        for (int slot : emptySlots) {
            stack = handler.insertItem(slot, stack, simulate);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    /**
     * Only inerts to empty slots. Perfect for not stackable items
     */
    public static ItemStack insertToEmpty(IItemHandler handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        int slots = handler.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack slotStack = handler.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                stack = handler.insertItem(i, stack, simulate);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    public static boolean extractItemsFromPlayerInv(Player player, List<ItemStack> items, boolean simulate) {
        var inventory = player.getInventory();

        for (var stack : items) {
            var found = ContainerHelper.clearOrCountMatchingItems(inventory, (s) -> s.is(stack.getItem()),
                    stack.getCount(), simulate);
            if (found < stack.getCount()) return false;
        }

        if (!simulate) player.inventoryMenu.broadcastChanges();
        return true;
    }

    /// Attempts to insert the provided item stack in the given item handler as a single, indivisible unit that should
    /// not be split over multiple
    /// inventories if the target item handler represents a virtual inventory backing some kind of item distribution
    /// system.
    /// This is only a hint to the compatible implementation that the given item stack should not be split, which can be
    /// ignored.
    public static ItemStack insertItemBundle(IItemHandler targetInventory, ItemStack stack, boolean simulate) {
        return targetInventory instanceof IBundleInsertable bundle ?
                bundle.insertItemBundle(stack, simulate) :
                ItemHandlerHelper.insertItem(targetInventory, stack, simulate);
    }

    @AllArgsConstructor
    private static class GroupItemInfo {

        public final ItemStack itemStack;
        public int totalCount;
    }

    /// Convenience wrapper around main stockInventoryItems overload that takes max transfer amount in items, and
    /// returns the amount of items that have been transferred
    public static int stockInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory,
                                          int maxTransferAmount, ToIntFunction<ItemStack> itemKeepAmountProvider) {
        TotalTransferAmountLimiter transferAmountLimiter = new TotalTransferAmountLimiter(maxTransferAmount);
        stockInventoryItems(sourceInventory, targetInventory, transferAmountLimiter, itemKeepAmountProvider);
        return maxTransferAmount - transferAmountLimiter.getTotalRemainingTransferAmount();
    }

    /// Similar to moveInventoryItems, but instead attempts to maintain amount of items per type as provided by
    /// itemKeepAmountProvider in target inventory.
    public static void stockInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory,
                                           ITransferAmountLimiter transferAmountLimiter,
                                           ToIntFunction<ItemStack> itemKeepAmountProvider) {
        // Delegate to the virtual item handler specific implementation if possible
        if (targetInventory instanceof IVirtualItemHandler virtualItemHandler) {
            virtualItemHandler.stockInventoryItems(sourceInventory, transferAmountLimiter, itemKeepAmountProvider);
            return;
        }

        Predicate<ItemStack> itemStackPredicate = itemStack -> itemKeepAmountProvider.applyAsInt(itemStack) > 0;
        Map<ItemStack, GroupItemInfo> targetItemAmounts = countInventoryItemsByMatchSlot(targetInventory,
                itemStackPredicate);
        Map<ItemStack, GroupItemInfo> sourceItemAmounts = countInventoryItemsByMatchSlot(sourceInventory,
                itemStackPredicate);

        Iterator<ItemStack> iterator = sourceItemAmounts.keySet().iterator();
        while (iterator.hasNext()) {
            ItemStack filteredItem = iterator.next();
            GroupItemInfo sourceInfo = sourceItemAmounts.get(filteredItem);
            int itemToKeepAmount = itemKeepAmountProvider.applyAsInt(sourceInfo.itemStack);

            int itemAmount = 0;
            if (targetItemAmounts.containsKey(filteredItem)) {
                GroupItemInfo destItemInfo = targetItemAmounts.get(filteredItem);
                itemAmount = destItemInfo.totalCount;
            }
            if (itemAmount < itemToKeepAmount) {
                sourceInfo.totalCount = itemToKeepAmount - itemAmount;
            } else {
                iterator.remove();
            }
        }

        moveInventoryItemGroups(sourceInventory, targetInventory, sourceItemAmounts, transferAmountLimiter,
                itemStackPredicate);
    }

    /// Moves inventory items between item handlers in specified groups
    private static void moveInventoryItemGroups(IItemHandler sourceInventory, IItemHandler targetInventory,
                                                Map<ItemStack, GroupItemInfo> itemInfos,
                                                ITransferAmountLimiter transferAmountLimiter,
                                                Predicate<ItemStack> itemStackPredicate) {
        for (int i = 0; i < sourceInventory.getSlots(); i++) {
            ItemStack itemStack = sourceInventory.getStackInSlot(i);
            if (itemStack.isEmpty() || !itemStackPredicate.test(itemStack) || !itemInfos.containsKey(itemStack)) {
                continue;
            }

            GroupItemInfo itemInfo = itemInfos.get(itemStack);

            ItemStack extractedStack = sourceInventory.extractItem(i,
                    Math.min(itemInfo.totalCount, Math.min(transferAmountLimiter.getRemainingTransferAmount(itemStack),
                            itemStack.getCount())),
                    true);

            ItemStack remainderStack = ItemHandlerHelper.insertItemStacked(targetInventory, extractedStack, true);
            int amountToInsert = extractedStack.getCount() - remainderStack.getCount();

            if (amountToInsert > 0) {
                extractedStack = sourceInventory.extractItem(i, amountToInsert, false);

                if (!extractedStack.isEmpty()) {

                    ItemHandlerHelper.insertItemStacked(targetInventory, extractedStack, false);
                    transferAmountLimiter.notifyItemTransferred(extractedStack);
                    itemInfo.totalCount -= extractedStack.getCount();

                    if (itemInfo.totalCount == 0) {
                        itemInfos.remove(itemStack);
                        if (itemInfos.isEmpty()) {
                            break;
                        }
                    }
                    if (!transferAmountLimiter.canTransferMoreItems()) {
                        break;
                    }
                }
            }
        }
    }

    /// Groups identical items together and calculates total amount of items per type in inventory
    private static Map<ItemStack, GroupItemInfo> countInventoryItemsByMatchSlot(IItemHandler inventory,
                                                                                Predicate<ItemStack> itemStackPredicate) {
        Map<ItemStack, GroupItemInfo> result = new Object2ObjectOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());

        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty() || !itemStackPredicate.test(itemStack)) {
                continue;
            }

            var itemInfo = result.computeIfAbsent(itemStack, s -> new GroupItemInfo(s, 0));

            itemInfo.totalCount += itemStack.getCount();
        }
        return result;
    }
}
