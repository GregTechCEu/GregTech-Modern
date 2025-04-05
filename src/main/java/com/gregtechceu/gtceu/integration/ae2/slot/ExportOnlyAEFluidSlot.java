package com.gregtechceu.gtceu.integration.ae2.slot;

import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExportOnlyAEFluidSlot extends ExportOnlyAESlot implements IFluidHandlerModifiable, IFluidTank {

    public ExportOnlyAEFluidSlot() {
        super();
    }

    public ExportOnlyAEFluidSlot(@Nullable GenericStack config, @Nullable GenericStack stock) {
        super(config, stock);
    }

    @Override
    public void addStack(GenericStack stack) {
        if (this.stock == null) {
            this.stock = stack;
        } else {
            this.stock = GenericStack.sum(this.stock, stack);
        }
        onContentsChanged();
    }

    @Override
    public void setStock(@Nullable GenericStack stack) {
        if (this.stock == null && stack == null) {
            return;
        } else if (stack == null) {
            this.stock = null;
        } else {
            if (stack.equals(stock)) return;
            this.stock = stack;
        }
        onContentsChanged();
    }

    @Override
    public FluidStack getFluid() {
        if (this.stock != null && this.stock.what() instanceof AEFluidKey fluidKey) {
            return fluidKey.toStack(GTMath.saturatedCast(this.stock.amount()));
        }
        return FluidStack.EMPTY;
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return false;
    }

    @Override
    public int getFluidAmount() {
        return this.stock != null ? GTMath.saturatedCast(this.stock.amount()) : 0;
    }

    @Override
    public int getCapacity() {
        // Its capacity is always 0.
        return 0;
    }

    @Override
    public int getTanks() {
        return 0;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public void setFluidInTank(int tank, FluidStack stack) {}

    @Override
    public int getTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public boolean supportsFill(int tank) {
        return false;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (this.getFluid().isFluidEqual(resource)) {
            return this.drain(resource.getAmount(), action);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (this.stock == null || !(this.stock.what() instanceof AEFluidKey fluidKey)) {
            return FluidStack.EMPTY;
        }
        int drained = (int) Math.min(this.stock.amount(), maxDrain);
        FluidStack result = fluidKey.toStack(drained);
        if (action.execute()) {
            this.stock = new GenericStack(this.stock.what(), this.stock.amount() - drained);
            if (this.stock.amount() == 0) {
                this.stock = null;
            }
            onContentsChanged();
        }
        return result;
    }

    @Override
    public boolean supportsDrain(int tank) {
        return tank == 0;
    }

    public void onContentsChanged() {
        if (onContentsChanged != null) {
            onContentsChanged.run();
        }
    }

    @Override
    public ExportOnlyAEFluidSlot copy() {
        return new ExportOnlyAEFluidSlot(
                this.config == null ? null : ExportOnlyAESlot.copy(this.config),
                this.stock == null ? null : ExportOnlyAESlot.copy(this.stock));
    }
}
