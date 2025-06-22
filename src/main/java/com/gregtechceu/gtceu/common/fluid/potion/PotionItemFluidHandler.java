package com.gregtechceu.gtceu.common.fluid.potion;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import org.jetbrains.annotations.NotNull;

public class PotionItemFluidHandler extends FluidHandlerItemStackSimple.SwapEmpty {

    public PotionItemFluidHandler(ItemStack potion) {
        super(potion, new ItemStack(Items.GLASS_BOTTLE), PotionFluidHelper.BOTTLE_AMOUNT);
    }

    @Override
    protected void setFluid(FluidStack fluid) {
        // no-op
    }

    @Override
    public @NotNull FluidStack getFluid() {
        return PotionFluidHelper.getFluidFromPotionItem(getContainer(), PotionFluidHelper.BOTTLE_AMOUNT);
    }
}
