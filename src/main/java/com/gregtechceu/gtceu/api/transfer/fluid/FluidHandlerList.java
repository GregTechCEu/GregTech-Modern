package com.gregtechceu.gtceu.api.transfer.fluid;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class FluidHandlerList implements IFluidHandlerModifiable, INBTSerializable<CompoundTag> {

    public final IFluidHandler[] handlers;

    @Setter
    protected Predicate<FluidStack> filter = fluid -> true;

    public FluidHandlerList(IFluidHandler... handlers) {
        this.handlers = handlers;
    }

    public FluidHandlerList(List<IFluidHandler> handlers) {
        this.handlers = handlers.toArray(IFluidHandler[]::new);
    }

    @Override
    public int getTanks() {
        return Arrays.stream(handlers).mapToInt(IFluidHandler::getTanks).sum();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        int index = 0;
        for (IFluidHandler handler : handlers) {
            if (tank - index < handler.getTanks()) {
                return handler.getFluidInTank(tank - index);
            }
            index += handler.getTanks();
        }
        return FluidStack.EMPTY;
    }

    @Override
    public void setFluidInTank(int tank, FluidStack stack) {
        int index = 0;
        for (IFluidHandler handler : handlers) {
            if (handler instanceof IFluidHandlerModifiable modifiable) {
                if (tank - index < modifiable.getTanks()) {
                    modifiable.setFluidInTank(tank - index, stack);
                    return;
                }
            }
            index += handler.getTanks();
        }
    }

    @Override
    public int getTankCapacity(int tank) {
        int index = 0;
        for (IFluidHandler handler : handlers) {
            if (tank - index < handler.getTanks()) {
                return handler.getTankCapacity(tank - index);
            }
            index += handler.getTanks();
        }
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if (!filter.test(stack)) return false;

        int index = 0;
        for (IFluidHandler handler : handlers) {
            if (tank - index < handler.getTanks()) {
                return handler.isFluidValid(tank - index, stack);
            }
            index += handler.getTanks();
        }
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !filter.test(resource)) return 0;
        var copied = resource.copy();
        for (IFluidHandler handler : handlers) {
            var candidate = copied.copy();
            copied.shrink(handler.fill(candidate, action));
            if (copied.isEmpty()) break;
        }
        return resource.getAmount() - copied.getAmount();
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !filter.test(resource)) return FluidStack.EMPTY;
        var copied = resource.copy();
        for (IFluidHandler handler : handlers) {
            var candidate = copied.copy();
            copied.shrink(handler.drain(candidate, action).getAmount());
            if (copied.isEmpty()) break;
        }
        copied.setAmount(resource.getAmount() - copied.getAmount());
        return copied;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain == 0) return FluidStack.EMPTY;
        FluidStack totalDrained = null;
        for (IFluidHandler handler : handlers) {
            if (totalDrained == null || totalDrained.isEmpty()) {
                totalDrained = handler.drain(maxDrain, action);
                if (totalDrained.isEmpty()) totalDrained = null;
                else maxDrain -= totalDrained.getAmount();
            } else {
                FluidStack copy = totalDrained.copy();
                copy.setAmount(maxDrain);
                FluidStack drain = handler.drain(copy, action);
                totalDrained.grow(drain.getAmount());
                maxDrain -= drain.getAmount();
            }
            if (maxDrain <= 0) break;
        }
        return totalDrained == null ? FluidStack.EMPTY : totalDrained;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var list = new ListTag();
        for (IFluidHandler handler : handlers) {
            if (handler instanceof INBTSerializable<?> serializable) {
                list.add(serializable.serializeNBT());
            } else {
                GTCEu.LOGGER.warn("[FluidHandlerList] internal tank doesn't support serialization");
            }
        }
        tag.put("tanks", list);
        tag.putByte("type", list.getElementType());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        var list = nbt.getList("tanks", nbt.getByte("type"));
        for (int i = 0; i < list.size(); i++) {
            if (handlers[i] instanceof INBTSerializable serializable) {
                serializable.deserializeNBT(list.get(i));
            } else {
                GTCEu.LOGGER.warn("[FluidHandlerList] internal tank doesn't support serialization");
            }
        }
    }

    @Override
    public boolean supportsFill(int tank) {
        for (IFluidHandler handler : handlers) {
            if (tank >= handler.getTanks()) {
                tank -= handler.getTanks();
                continue;
            }

            if (handler instanceof IFluidHandlerModifiable modifiable) {
                return modifiable.supportsFill(tank);
            }
        }

        return true;
    }

    @Override
    public boolean supportsDrain(int tank) {
        for (IFluidHandler handler : handlers) {
            if (tank >= handler.getTanks()) {
                tank -= handler.getTanks();
                continue;
            }

            if (handler instanceof IFluidHandlerModifiable modifiable) {
                return modifiable.supportsDrain(tank);
            }
        }

        return true;
    }
}
