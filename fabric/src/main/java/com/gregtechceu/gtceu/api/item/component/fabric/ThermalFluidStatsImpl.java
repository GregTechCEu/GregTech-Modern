package com.gregtechceu.gtceu.api.item.component.fabric;

import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.ThermalFluidStats;
import com.gregtechceu.gtceu.api.misc.fabric.FluidCellStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;

/**
 * @author KilaBash
 * @date 2023/3/19
 * @implNote ThermalFluidStatsImpl
 */
public class ThermalFluidStatsImpl extends ThermalFluidStats {

    protected ThermalFluidStatsImpl(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        super(capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof, allowPartialFill);
    }

    public static ThermalFluidStats create(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        return new ThermalFluidStatsImpl(capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof, allowPartialFill);
    }

    @Override
    public void onAttached(IComponentItem item) {
        FluidStorage.ITEM.registerForItems((itemStack, context) -> new FluidCellStorage(context, capacity, allowPartialFill, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof), item);
    }

}
