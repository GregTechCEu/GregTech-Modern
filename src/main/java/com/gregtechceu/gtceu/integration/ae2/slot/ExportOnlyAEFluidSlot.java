package com.gregtechceu.gtceu.integration.ae2.slot;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;

import net.minecraft.MethodsReturnNonnullByDefault;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExportOnlyAEFluidSlot extends ExportOnlyAESlot implements IFluidStorage {

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
            return FluidStack.create(fluidKey.getFluid(), this.stock.amount(),
                    fluidKey.getTag());
        }
        return FluidStack.empty();
    }

    @Override
    public void setFluid(FluidStack fluid) {
        // NO-OP
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return false;
    }

    @Override
    public long getFluidAmount() {
        return this.stock != null ? this.stock.amount() : 0;
    }

    @Override
    public long getCapacity() {
        // Its capacity is always 0.
        return 0;
    }

    @Override
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        return 0;
    }

    @Override
    public long fill(FluidStack resource, boolean doFill) {
        return 0;
    }

    @Override
    public boolean supportsFill(int tank) {
        return false;
    }

    @Override
    public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        return this.drain(resource, simulate, notifyChanges);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain, boolean notifyChanges) {
        if (this.getFluid().isFluidEqual(resource)) {
            return this.drain(resource.getAmount(), doDrain, notifyChanges);
        }
        return FluidStack.empty();
    }

    @Override
    public FluidStack drain(long maxDrain, boolean simulate, boolean notifyChanges) {
        if (this.stock == null || !(this.stock.what() instanceof AEFluidKey fluidKey)) {
            return FluidStack.empty();
        }
        int drained = (int) Math.min(this.stock.amount(), maxDrain);
        FluidStack result = FluidStack.create(fluidKey.getFluid(), drained, fluidKey.getTag());
        if (!simulate) {
            this.stock = new GenericStack(this.stock.what(), this.stock.amount() - drained);
            if (this.stock.amount() == 0) {
                this.stock = null;
            }
            if (notifyChanges) onContentsChanged();
        }
        return result;
    }

    @Override
    public boolean supportsDrain(int tank) {
        return tank == 0;
    }

    @Override
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

    @Deprecated
    @Override
    public Object createSnapshot() {
        return Pair.of(this.config, this.stock);
    }

    @Deprecated
    @Override
    public void restoreFromSnapshot(Object snapshot) {
        if (snapshot instanceof Pair<?, ?> pair) {
            this.config = (GenericStack) pair.getFirst();
            this.stock = (GenericStack) pair.getSecond();
        }
    }
}
