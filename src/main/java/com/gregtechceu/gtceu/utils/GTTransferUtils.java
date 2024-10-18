package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerList;

import com.lowdragmc.lowdraglib.misc.ItemHandlerHelper;
import com.lowdragmc.lowdraglib.misc.ItemTransferList;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

public class GTTransferUtils {

    /**
     * Gets the FluidHandler from the adjacent block on the side connected to the caller
     *
     * @param level  Level
     * @param pos    BlockPos of the machine which is calling
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

    public static int transferFluids(@NotNull IFluidHandler sourceHandler, @NotNull IFluidHandler destHandler) {
        return transferFluids(sourceHandler, destHandler, Integer.MAX_VALUE, fluidStack -> true);
    }

    public static int transferFluids(@NotNull IFluidHandler sourceHandler, @NotNull IFluidHandler destHandler,
                                     int transferLimit) {
        return transferFluids(sourceHandler, destHandler, transferLimit, fluidStack -> true);
    }

    public static int transferFluids(@NotNull IFluidHandler sourceHandler, @NotNull IFluidHandler destHandler,
                                     int transferLimit, @NotNull Predicate<FluidStack> fluidFilter) {
        int fluidLeftToTransfer = transferLimit;

        for (int i = 0; i < sourceHandler.getTanks(); ++i) {
            FluidStack currentFluid = sourceHandler.getFluidInTank(i);
            if (currentFluid.isEmpty() ||
                    !fluidFilter.test(currentFluid)) {
                continue;
            }

            currentFluid.setAmount(fluidLeftToTransfer);
            FluidStack fluidStack = sourceHandler.drain(currentFluid, FluidAction.SIMULATE);
            if (fluidStack.isEmpty()) {
                continue;
            }

            int canInsertAmount = destHandler.fill(fluidStack, FluidAction.SIMULATE);
            if (canInsertAmount > 0) {
                fluidStack.setAmount(canInsertAmount);
                fluidStack = sourceHandler.drain(fluidStack, FluidAction.EXECUTE);
                if (fluidStack != FluidStack.EMPTY && fluidStack.getAmount() > 0) {
                    fillFluidAccountNotifiableList(destHandler, fluidStack, FluidAction.EXECUTE);

                    fluidLeftToTransfer -= fluidStack.getAmount();
                    if (fluidLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return transferLimit - fluidLeftToTransfer;
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

    // TODO: Clean this up to use FluidUtil and move it back to caller
    public static int transferFiltered(@Nonnull IFluidHandler sourceHandler, @Nonnull IFluidHandler destHandler,
                                       int transferLimit, @Nonnull Predicate<FluidStack> fluidFilter) {
        int fluidLeftToTransfer = transferLimit;
        for (int i = 0; i < sourceHandler.getTanks(); i++) {
            FluidStack currentFluid = sourceHandler.getFluidInTank(i).copy();
            if (currentFluid.isEmpty() || !fluidFilter.test(currentFluid)) {
                continue;
            }

            currentFluid.setAmount(fluidLeftToTransfer);
            var drained = sourceHandler.drain(currentFluid, FluidAction.SIMULATE);
            if (drained.isEmpty()) {
                continue;
            }

            var canInsertAmount = destHandler.fill(drained.copy(), FluidAction.SIMULATE);
            if (canInsertAmount > 0) {
                drained.setAmount(canInsertAmount);
                drained = sourceHandler.drain(drained, FluidAction.EXECUTE);
                if (!drained.isEmpty()) {
                    destHandler.fill(drained, FluidAction.EXECUTE);
                    fluidLeftToTransfer -= drained.getAmount();
                    if (fluidLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return transferLimit - fluidLeftToTransfer;
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
     * <br />
     * <br />
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
    public static boolean addFluidsToFluidHandler(FluidHandlerList fluidHandler,
                                                  boolean simulate,
                                                  List<FluidStack> fluidStacks) {
        if (simulate) {
            OverlayedFluidHandler overlayedFluidHandler = new OverlayedFluidHandler(fluidHandler);
            for (FluidStack fluidStack : fluidStacks) {
                int inserted = overlayedFluidHandler.insertFluid(fluidStack, fluidStack.getAmount());
                if (inserted != fluidStack.getAmount()) {
                    return false;
                }
            }
            return true;
        }

        for (FluidStack fluidStack : fluidStacks) {
            fillFluidAccountNotifiableList(fluidHandler, fluidStack, FluidAction.EXECUTE);
        }
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

    public static ItemStack insertItemAccountNotifiableList(IItemTransfer handler, int slot, ItemStack stack,
                                                            boolean simulate) {
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

    public static ItemStack extractItemAccountNotifiableList(
                                                             IItemTransfer handler, int slot, int amount,
                                                             boolean simulate) {
        if (handler instanceof ItemTransferList transferList) {
            int index = 0;
            for (var transfer : transferList.transfers) {
                if (slot - index < transfer.getSlots()) {
                    if (transfer instanceof NotifiableItemStackHandler notifiable) {
                        return notifiable.extractItemInternal(slot - index, amount, simulate);
                    } else {
                        return transfer.extractItem(slot - index, amount, simulate);
                    }
                }
                index += transfer.getSlots();
            }
            return ItemStack.EMPTY;
        }
        return handler.extractItem(slot, amount, simulate);
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
