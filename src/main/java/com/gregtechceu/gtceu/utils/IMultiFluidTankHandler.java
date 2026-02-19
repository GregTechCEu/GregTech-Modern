package com.gregtechceu.gtceu.utils;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IMultiFluidTankHandler extends IFluidHandler {

    IFluidTank getFluidTank(int index);
}
