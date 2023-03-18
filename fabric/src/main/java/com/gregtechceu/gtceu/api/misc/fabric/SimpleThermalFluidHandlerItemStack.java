package com.gregtechceu.gtceu.api.misc.fabric;

import com.gregtechceu.gtceu.api.capability.IThermalFluidHandlerItemStack;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.world.item.ItemStack;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote FluidHandlerHelperImpl
 */
public class SimpleThermalFluidHandlerItemStack extends FluidHandlerItemStack.SwapEmpty implements IThermalFluidHandlerItemStack {
    public final int maxFluidTemperature;
    private final boolean gasProof;
    private final boolean acidProof;
    private final boolean cryoProof;
    private final boolean plasmaProof;

    public SimpleThermalFluidHandlerItemStack(ContainerItemContext container, ItemStack emptyContainer, int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
        super(container, emptyContainer, capacity);
        this.maxFluidTemperature = maxFluidTemperature;
        this.gasProof = gasProof;
        this.acidProof = acidProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
    }

    @Override
    public int getMaxFluidTemperature() {
        return maxFluidTemperature;
    }

    @Override
    public boolean isGasProof() {
        return gasProof;
    }

    @Override
    public boolean isAcidProof() {
        return acidProof;
    }

    @Override
    public boolean isCryoProof() {
        return cryoProof;
    }

    @Override
    public boolean isPlasmaProof() {
        return plasmaProof;
    }
}
