package com.gregtechceu.gtceu.api.capability;

import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for multi-tank fluid handlers. Handles insertion logic, along with other standard
 * {@link IFluidTransfer} functionalities.
 *
 * @see com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank NotifiableFluidTank
 */
public interface IMultipleTankHandler extends IFluidTransfer, Iterable<IMultipleTankHandler.MultiFluidTankEntry> {

    /**
     * Comparator for entries that can be used in insertion logic
     */
    Comparator<MultiFluidTankEntry> ENTRY_COMPARATOR = (o1, o2) -> {
        // #1: non-empty tank first
        boolean empty1 = o1.getFluidAmount() <= 0;
        boolean empty2 = o2.getFluidAmount() <= 0;
        if (empty1 != empty2) return empty1 ? 1 : -1;

        // #2: filter priority
        IFilter<FluidStack> filter1 = o1.getFilter();
        IFilter<FluidStack> filter2 = o2.getFilter();
        if (filter1 == null) return filter2 == null ? 0 : 1;
        if (filter2 == null) return -1;
        return IFilter.FILTER_COMPARATOR.compare(filter1, filter2);
    };

    /**
     * @return unmodifiable view of {@code MultiFluidTankEntry}s. Note that it's still possible to access
     * and modify inner contents of the tanks.
     */
    @Nonnull
    List<MultiFluidTankEntry> getFluidTanks();

    /**
     * @return Number of tanks in this tank handler
     */
    int getTanks();

    @Nonnull
    MultiFluidTankEntry getTankAt(int index);

    /**
     * @return {@code false} if insertion to this fluid handler enforces input to be
     * filled in one slot at max. {@code true} if it bypasses the rule.
     */
    boolean allowSameFluidFill();

    /**
     * Tries to search tank with contents equal to {@code fluidStack}. If {@code fluidStack} is
     * {@code null}, an empty tank is searched instead.
     *
     * @param fluidStack Fluid stack to search index
     * @return Index corresponding to tank at {@link #getFluidTanks()} with matching
     */
    default int getIndexOfFluid(@Nullable FluidStack fluidStack) {
        List<MultiFluidTankEntry> fluidTanks = getFluidTanks();
        for (int i = 0; i < fluidTanks.size(); i++) {
            FluidStack tankStack = fluidTanks.get(i).getFluid();
            if (fluidStack == tankStack || tankStack != null && tankStack.isFluidEqual(fluidStack)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    default Iterator<MultiFluidTankEntry> iterator() {
        return getFluidTanks().iterator();
    }

    /**
     * Entry of multi fluid tanks. Retains reference to original {@link IMultipleTankHandler} for accessing
     * information such as {@link IMultipleTankHandler#allowSameFluidFill()}.
     */
    final class MultiFluidTankEntry implements IFluidTransfer, IFluidStorage, IFilteredFluidContainer {

        private final IMultipleTankHandler tank;
        private final IFluidStorage delegate;

        public MultiFluidTankEntry(@Nonnull IMultipleTankHandler tank, @Nonnull IFluidStorage delegate) {
            this.tank = tank;
            this.delegate = delegate;
        }

        @Nonnull
        public IMultipleTankHandler getTank() {
            return tank;
        }

        @Nonnull
        public IFluidStorage getDelegate() {
            return delegate;
        }

        public boolean allowSameFluidFill() {
            return tank.allowSameFluidFill();
        }

        @Nullable
        @Override
        public IFilter<FluidStack> getFilter() {
            return this.delegate instanceof IFilteredFluidContainer filtered ? filtered.getFilter() : null;
        }

        public CompoundTag trySerialize() {
            if (delegate instanceof FluidStorage fluidTank) {
                return fluidTank.serializeNBT();
            } else if (delegate instanceof ITagSerializable<?> serializable) {
                try {
                    return (CompoundTag) serializable.serializeNBT();
                } catch (ClassCastException ignored) {}
            }
            return new CompoundTag();
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public void tryDeserialize(CompoundTag tag) {
            if (delegate instanceof FluidStorage fluidTank) {
                fluidTank.deserializeNBT(tag);
            } else if (delegate instanceof ITagSerializable serializable) {
                try {
                    serializable.deserializeNBT(tag);
                } catch (ClassCastException ignored) {}
            }
        }

        @Override
        public @NotNull FluidStack getFluid() {
            return delegate.getFluid();
        }

        @Override
        public void setFluid(FluidStack fluid) {
            
        }

        @Override
        public long getFluidAmount() {
            return delegate.getFluidAmount();
        }

        @Override
        public long getCapacity() {
            return delegate.getCapacity();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return false;
        }

        @Override
        public long fill(FluidStack resource, boolean doFill) {
            return delegate.fill(resource, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.getAmount() <= 0) {
                return null;
            }
            if (delegate instanceof FluidStorage storage) {
                return storage.drain(resource, doDrain);
            }
            // just imitate the logic
            FluidStack fluid = delegate.getFluid();
            return fluid != FluidStack.empty() && fluid.isFluidEqual(resource) ? drain(resource.getAmount(), doDrain) : null;
        }

        @Nullable
        @Override
        public FluidStack drain(long maxDrain, boolean doDrain) {
            return delegate.drain(maxDrain, doDrain);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return this == obj || delegate.equals(obj);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}
