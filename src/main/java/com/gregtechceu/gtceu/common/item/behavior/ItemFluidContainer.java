package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.item.component.IRecipeRemainder;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote ItemFluidContainer
 */
public class ItemFluidContainer implements IRecipeRemainder {

    @Override
    public ItemStack getRecipeRemained(ItemStack itemStack) {
        var storage = new CustomItemStackHandler(itemStack);
        var transfer = FluidTransferHelper.getFluidTransfer(storage, 0);
        if (transfer != null) {
            var drained = transfer.drain(FluidHelper.getBucket(), IFluidHandler.FluidAction.SIMULATE);
            if (drained.getAmount() != FluidHelper.getBucket()) return ItemStack.EMPTY;
            transfer.drain(FluidHelper.getBucket(), IFluidHandler.FluidAction.EXECUTE);
            var copied = storage.getStackInSlot(0);
            // clear all components.
            for (var key : copied.getComponentsPatch().entrySet()) {
                copied.remove(key.getKey());
            }
            return copied;
        }
        return storage.getStackInSlot(0);
    }
}
