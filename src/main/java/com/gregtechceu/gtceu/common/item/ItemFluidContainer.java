package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.IRecipeRemainder;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidType;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote ItemFluidContainer
 */
public class ItemFluidContainer implements IRecipeRemainder {

    @Override
    public ItemStack getRecipeRemained(ItemStack itemStack) {
        var storage = new ItemStackTransfer(itemStack);
        var transfer = FluidTransferHelper.getFluidTransfer(storage, 0);
        if (transfer != null) {
            var drained = transfer.drain(FluidType.BUCKET_VOLUME, true);
            if (drained.getAmount() != FluidType.BUCKET_VOLUME) return ItemStack.EMPTY;
            transfer.drain(FluidType.BUCKET_VOLUME, false);
            var copied = storage.getStackInSlot(0);
            copied.setTag(null);
            return copied;
        }
        return storage.getStackInSlot(0);
    }
}
