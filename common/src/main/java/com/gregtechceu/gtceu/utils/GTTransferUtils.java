package com.gregtechceu.gtceu.utils;


import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import com.lowdragmc.lowdraglib.misc.ItemHandlerHelper;
import com.lowdragmc.lowdraglib.misc.ItemTransferList;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;

public class GTTransferUtils {

    public static int transferFluids(@Nonnull IFluidTransfer sourceHandler, @Nonnull IFluidTransfer destHandler) {
        return transferFluids(sourceHandler, destHandler, Integer.MAX_VALUE, fluidStack -> true);
    }

    public static int transferFluids(@Nonnull IFluidTransfer sourceHandler, @Nonnull IFluidTransfer destHandler, int transferLimit) {
        return transferFluids(sourceHandler, destHandler, transferLimit, fluidStack -> true);
    }

    public static int transferFluids(@Nonnull IFluidTransfer sourceHandler, @Nonnull IFluidTransfer destHandler, int transferLimit, @Nonnull Predicate<FluidStack> fluidFilter) {
        int fluidLeftToTransfer = transferLimit;

        for (int i = 0; i < sourceHandler.getTanks(); ++i) {
            FluidStack currentFluid = sourceHandler.getFluidInTank(i);
            if (currentFluid == FluidStack.empty() || currentFluid.getAmount() == 0 || !fluidFilter.test(currentFluid)) {
                continue;
            }

            currentFluid.setAmount(fluidLeftToTransfer);
            FluidStack fluidStack = sourceHandler.drain(currentFluid, false);
            if (fluidStack == FluidStack.empty() || fluidStack.getAmount() == 0) {
                continue;
            }

            long canInsertAmount = destHandler.fill(fluidStack, false);
            if (canInsertAmount > 0) {
                fluidStack.setAmount(canInsertAmount);
                fluidStack = sourceHandler.drain(fluidStack, true);
                if (fluidStack != FluidStack.empty() && fluidStack.getAmount() > 0) {
                    fillFluidAccountNotifiableList(destHandler, fluidStack, true);

                    fluidLeftToTransfer -= fluidStack.getAmount();
                    if (fluidLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return transferLimit - fluidLeftToTransfer;
    }

    public static boolean transferExactFluidStack(@Nonnull IFluidTransfer sourceHandler, @Nonnull IFluidTransfer destHandler, FluidStack fluidStack) {
        long amount = fluidStack.getAmount();
        FluidStack sourceFluid = sourceHandler.drain(fluidStack, true);
        if (sourceFluid == FluidStack.empty() || sourceFluid.getAmount() != amount) {
            return false;
        }
        long canInsertAmount = destHandler.fill(sourceFluid, true);
        if (canInsertAmount == amount) {
            sourceFluid = sourceHandler.drain(sourceFluid, false);
            if (sourceFluid != FluidStack.empty() && sourceFluid.getAmount() > 0) {
                destHandler.fill(sourceFluid, false);
                return true;
            }
        }
        return false;
    }

    public static void moveInventoryItems(IItemTransfer sourceInventory, IItemTransfer targetInventory) {
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
     * <br /><br />
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
    public static boolean addItemsToItemHandler(final IItemTransfer handler,
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
        items.forEach(stack -> insertItem(handler, stack, false));
        return true;
    }

    /**
     * Simulates the insertion of fluid into a target fluid handler, then optionally performs the insertion.
     * <br /><br />
     * Simulating will not modify any of the input parameters. Insertion will either succeed completely, or fail
     * without modifying anything.
     * This method should be called with {@code simulate} {@code true} first, then {@code simulate} {@code false},
     * only if it returned {@code true}.
     *
     * @param fluidHandler the target inventory
     * @param simulate     whether to simulate ({@code true}) or actually perform the insertion ({@code false})
     * @param fluidStacks  the items to insert into {@code fluidHandler}.
     * @return {@code true} if the insertion succeeded, {@code false} otherwise.
     */
    public static boolean addFluidsToFluidHandler(FluidTransferList fluidHandler,
                                                  boolean simulate,
                                                  List<FluidStack> fluidStacks) {
        if (simulate) {
            OverlayedFluidHandler overlayedFluidHandler = new OverlayedFluidHandler(fluidHandler);
            for (FluidStack fluidStack : fluidStacks) {
                long inserted = overlayedFluidHandler.insertFluid(fluidStack, fluidStack.getAmount());
                if (inserted != fluidStack.getAmount()) {
                    return false;
                }
            }
            return true;
        }

        for (FluidStack fluidStack : fluidStacks) {
            fillFluidAccountNotifiableList(fluidHandler, fluidStack, true);
        }
        return true;
    }

    public static long fillFluidAccountNotifiableList(IFluidTransfer handler, FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) return 0;
        if (handler instanceof FluidTransferList transferList) {
            var copied = stack.copy();
            for (var transfer : transferList.transfers) {
                var candidate = copied.copy();
                if (transfer instanceof NotifiableFluidTank notifiable) {
                    copied.shrink(notifiable.fillInternal(candidate, simulate));
                } else {
                    copied.shrink(transfer.fill(candidate, simulate));
                }
                if (copied.isEmpty()) break;
            }
            return stack.getAmount() - copied.getAmount();
        }
        return handler.fill(stack, simulate);
    }

    public static FluidStack drainFluidAccountNotifiableList(IFluidTransfer handler, FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) return FluidStack.empty();
        if (handler instanceof FluidTransferList transferList) {
            var copied = stack.copy();
            for (var transfer : transferList.transfers) {
                var candidate = copied.copy();
                if (transfer instanceof NotifiableFluidTank notifiable) {
                    copied.shrink(notifiable.drainInternal(candidate, simulate).getAmount());
                } else {
                    copied.shrink(transfer.drain(candidate, simulate).getAmount());
                }
                if (copied.isEmpty()) break;
            }
            copied.setAmount(stack.getAmount() - copied.getAmount());
            return copied;
        }
        return handler.drain(stack, simulate);
    }

    /**
     * Inserts items by trying to fill slots with the same item first, and then fill empty slots.
     */
    public static ItemStack insertItem(IItemTransfer handler, ItemStack stack, boolean simulate) {
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
                stack = insertItemAccountNotifiableList(handler, i, stack, simulate);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        for (int slot : emptySlots) {
            stack = insertItemAccountNotifiableList(handler, slot, stack, simulate);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    public static ItemStack insertItemAccountNotifiableList(IItemTransfer handler, int slot, ItemStack stack, boolean simulate) {
        if (handler instanceof ItemTransferList transferList) {
            int index = 0;
            for (var transfer : transferList.transfers) {
                if (slot - index < transfer.getSlots()) {
                    if (transfer instanceof NotifiableItemStackHandler notifiable) {
                        return notifiable.insertItemInternal(slot - index, stack, simulate);
                    } else {
                        return transfer.insertItem(slot - index, stack, simulate);
                    }
                }
                index += transfer.getSlots();
            }
            return stack;
        }
        return handler.insertItem(slot, stack, simulate);
    }

    /**
     * Only inerts to empty slots. Perfect for not stackable items
     */
    public static ItemStack insertToEmpty(IItemTransfer handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        int slots = handler.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack slotStack = handler.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                stack = insertItemAccountNotifiableList(handler, i, stack, simulate);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }
}
