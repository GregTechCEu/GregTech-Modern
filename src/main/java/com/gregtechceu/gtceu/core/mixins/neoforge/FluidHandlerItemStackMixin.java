package com.gregtechceu.gtceu.core.mixins.neoforge;

import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FluidHandlerItemStack.class, remap = false)
public abstract class FluidHandlerItemStackMixin implements IFluidHandlerItem, IFluidHandlerModifiable {
    @Shadow
    protected abstract void setFluid(FluidStack fluid);

    @Override
    public void setFluidInTank(int tank, FluidStack fluid) {
        setFluid(fluid);
    }
}
