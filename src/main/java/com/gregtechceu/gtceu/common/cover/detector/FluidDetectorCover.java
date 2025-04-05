package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.utils.RedstoneUtil;

import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidDetectorCover extends DetectorCover {

    public FluidDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && getFluidHandler() != null;
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        IFluidHandler fluidHandler = getFluidHandler();
        if (fluidHandler == null)
            return;

        int storedFluid = 0;
        int fluidCapacity = 0;

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

    protected IFluidHandler getFluidHandler() {
        return FluidUtil.getFluidHandler(coverHolder.getLevel(), coverHolder.getPos(), attachedSide).resolve()
                .orElse(null);
    }
}
