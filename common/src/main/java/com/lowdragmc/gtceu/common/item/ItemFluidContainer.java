package com.lowdragmc.gtceu.common.item;

import com.lowdragmc.gtceu.api.item.component.IRecipeRemainder;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import net.minecraft.world.item.ItemStack;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote ItemFluidContainer
 */
public class ItemFluidContainer implements IRecipeRemainder {
    @Override
    public ItemStack getRecipeRemained(ItemStack itemStack) {
        var transfer = FluidTransferHelper.getFluidTransfer(itemStack);
        if (transfer != null) {
            var drained = transfer.drain(1000, false);
            if (drained.getAmount() != 1000) return ItemStack.EMPTY;
            transfer.drain(1000, true);
            var copied = itemStack.copy();
            copied.setTag(null);
            return copied;
        }
        return itemStack;
    }

}
