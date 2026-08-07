package com.gregtechceu.gtceu.common.fluid.potion;

import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import org.jetbrains.annotations.NotNull;

public class PotionItemFluidHandler extends FluidHandlerItemStackSimple.SwapEmpty {

    public PotionItemFluidHandler(ItemStack container) {
        // the item component is never actually used, so we don't give it anything.
        super(GTDataComponents.FLUID_CONTENT, container,
                new ItemStack(Items.GLASS_BOTTLE), PotionFluidHelper.BOTTLE_AMOUNT);
    }

    public PotionItemFluidHandler(ItemStack container, Void ignored) {
        this(container);
    }

    @Override
    protected void setContainerToEmpty() {
        container = emptyContainer;
    }

    @Override
    protected void setFluid(@NotNull FluidStack fluid) {
        // no-op
    }

    @Override
    public @NotNull FluidStack getFluid() {
        return PotionFluidHelper.getFluidFromPotionItem(getContainer(), PotionFluidHelper.BOTTLE_AMOUNT);
    }
}
