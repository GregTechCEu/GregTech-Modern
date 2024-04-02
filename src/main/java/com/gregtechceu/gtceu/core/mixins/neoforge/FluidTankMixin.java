package com.gregtechceu.gtceu.core.mixins.neoforge;

import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FluidTank.class, remap = false)
public abstract class FluidTankMixin implements IFluidHandlerModifiable, IFluidTank {
    @Shadow
    protected FluidStack fluid;

    @Shadow
    protected abstract void onContentsChanged();
    @Shadow
    public abstract void setFluid(FluidStack fluid);

    @Shadow
    public abstract int fill(FluidStack resource, FluidAction action);

    @Shadow
    public abstract @NotNull FluidStack drain(int maxDrain, FluidAction action);

    @Override
    public void setFluidInTank(int tank, FluidStack fluid) {
        setFluid(fluid);
        this.onContentsChanged();
    }
}
