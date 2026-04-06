package com.gregtechceu.gtceu.api.misc.forge;

import com.gregtechceu.gtceu.api.capability.IThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ThermalFluidHandlerItemStack extends FluidHandlerItemStack implements IThermalFluidHandlerItemStack {

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

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     * @param capacity  The maximum capacity of this fluid tank.
     */
    public ThermalFluidHandlerItemStack(@NotNull ItemStack container, int capacity, int maxFluidTemperature,
                                        boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
        super(GTDataComponents.FLUID_CONTENT, container, capacity);
        this.maxFluidTemperature = maxFluidTemperature;
        this.gasProof = gasProof;
        this.acidProof = acidProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return IThermalFluidHandlerItemStack.super.canFillFluidType(fluid);
    }
}
