package com.gregtechceu.gtceu.api.misc.forge;

import com.gregtechceu.gtceu.api.capability.IThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.item.component.ThermalFluidStats;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class SimpleThermalFluidHandlerItemStack extends FluidHandlerItemStackSimple
                                                implements IThermalFluidHandlerItemStack {

    @Getter
    private final int maxFluidTemperature;
    @Getter
    private final boolean gasProof;
    @Getter
    private final boolean acidProof;
    @Getter
    private final boolean cryoProof;
    @Getter
    private final boolean plasmaProof;

    public SimpleThermalFluidHandlerItemStack(@NotNull ItemStack container, int capacity, int maxFluidTemperature,
                                              boolean gasProof, boolean acidProof, boolean cryoProof,
                                              boolean plasmaProof) {
        super(GTDataComponents.FLUID_CONTENT, container, capacity);
        this.maxFluidTemperature = maxFluidTemperature;
        this.gasProof = gasProof;
        this.acidProof = acidProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
    }

    public SimpleThermalFluidHandlerItemStack(@NotNull ItemStack container, ThermalFluidStats stats) {
        super(GTDataComponents.FLUID_CONTENT, container, stats.capacity);
        this.maxFluidTemperature = stats.maxFluidTemperature;
        this.gasProof = stats.gasProof;
        this.acidProof = stats.acidProof;
        this.cryoProof = stats.cryoProof;
        this.plasmaProof = stats.plasmaProof;
    }
}
