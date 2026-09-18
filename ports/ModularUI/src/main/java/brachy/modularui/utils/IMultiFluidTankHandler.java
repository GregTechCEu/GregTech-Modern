package brachy.modularui.utils;

import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public interface IMultiFluidTankHandler extends IFluidHandler {

    IFluidTank getFluidTank(int index);
}
