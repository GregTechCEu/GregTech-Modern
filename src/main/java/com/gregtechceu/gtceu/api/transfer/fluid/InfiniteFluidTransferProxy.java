package com.gregtechceu.gtceu.api.transfer.fluid;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class InfiniteFluidTransferProxy extends FluidTransferDelegate {

    private final boolean infiniteSource;
    private final boolean infiniteSink;

    public InfiniteFluidTransferProxy(IFluidHandlerModifiable delegate, boolean infiniteSource, boolean infiniteSink) {
        super(delegate);

        this.infiniteSource = infiniteSource;
        this.infiniteSink = infiniteSink;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (infiniteSink)
            return resource.getAmount();

        return super.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (infiniteSource)
            return resource.copy();

        return super.drain(resource, action);
    }
}
