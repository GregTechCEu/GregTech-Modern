package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.utils.RedstoneUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import net.minecraft.core.Direction;

public class FluidDetectorCover extends DetectorCover {
    public FluidDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return getFluidTransfer() != null;
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        IFluidTransfer fluidHandler = getFluidTransfer();
        if (fluidHandler == null)
            return;

        long storedFluid = 0;
        long fluidCapacity = 0;

        for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
            FluidStack content = fluidHandler.getFluidInTank(tank);
            if (!content.isEmpty())
                storedFluid += content.getAmount();

            fluidCapacity += fluidHandler.getTankCapacity(tank);
        }

        if (fluidCapacity == 0)
            return;

        setRedstoneSignalOutput(RedstoneUtil.computeRedstoneValue(storedFluid, fluidCapacity, isInverted()));
    }

    protected IFluidTransfer getFluidTransfer() {
        return FluidTransferHelper.getFluidTransfer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
    }
}
