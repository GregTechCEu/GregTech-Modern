package com.gregtechceu.gtceu.api.capability;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * Interface for fluid containers ({@link com.lowdragmc.lowdraglib.side.fluid.IFluidStorage IFluidTank} or
 * {@link com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer IFluidHandler}) associated with {@link IFilter}.
 */
public interface IFilteredFluidContainer {

    /**
     * Compare logic for filtered instances.
     */
    Comparator<IFilteredFluidContainer> COMPARATOR = Comparator.nullsLast(
            Comparator.comparing(IFilteredFluidContainer::getFilter, IFilter.FILTER_COMPARATOR)
    );

    /**
     * @return instance of {@link IFilter} associated to this object, or {@code null} if there's no filter
     * associated.
     */
    @Nullable
    IFilter<FluidStack> getFilter();
}
