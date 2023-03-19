package com.gregtechceu.gtceu.api.item.component.fabric;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.ThermalFluidStats;
import com.gregtechceu.gtceu.api.misc.fabric.SimpleThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.misc.fabric.ThermalFluidHandlerItemStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;

/**
 * @author KilaBash
 * @date 2023/3/19
 * @implNote ThermalFluidStatsImpl
 */
public class ThermalFluidStatsImpl extends ThermalFluidStats{
    protected ThermalFluidStatsImpl(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        super(capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof, allowPartialFill);
    }

    public static ThermalFluidStats create(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        return new ThermalFluidStatsImpl(capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof, allowPartialFill);
    }

    @Override
    public void onAttached(ComponentItem item) {
        FluidStorage.ITEM.registerForItems((itemStack, context) -> {
            if (allowPartialFill) {
                return new ThermalFluidHandlerItemStack(context, capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof);
            }
            return new SimpleThermalFluidHandlerItemStack(context, item.asItem().getDefaultInstance(), capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof);
        }, item);
    }
}
