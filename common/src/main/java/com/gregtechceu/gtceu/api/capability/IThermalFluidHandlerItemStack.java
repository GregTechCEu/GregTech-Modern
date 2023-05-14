package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.data.chemical.fluid.FluidType;
import com.gregtechceu.gtceu.api.data.chemical.fluid.FluidTypes;
import com.gregtechceu.gtlib.side.fluid.FluidHelper;
import com.gregtechceu.gtlib.side.fluid.FluidStack;
import net.minecraft.world.level.material.Fluid;

/**
 * Interface for FluidHandlerItemStacks which handle GT's unique fluid mechanics
 * @see FluidType
 * @see FluidTypes
 * @see MaterialFluid
 */
public interface IThermalFluidHandlerItemStack {

    /**
     *
     * @param stack the {@link FluidStack} to check
     * @return whether the FluidStack can be used to fill this fluid container
     */
    default boolean canFillFluidType(FluidStack stack) {
        if (stack == null || stack.getFluid() == null) return false;

        Fluid fluid = stack.getFluid();
        var temp = FluidHelper.getTemperature(stack);
        if (temp > getMaxFluidTemperature()) return false;
        // fluids less than 120K are cryogenic
        if (temp < 120 && !isCryoProof()) return false;
        if (FluidHelper.isLighterThanAir(stack) && !isGasProof()) return false;

        // TODO custom fluid
//        for (RegistryEntry<Fluid> entry : GTRegistries.REGISTRATE.getAll(Registry.FLUID_REGISTRY)) {
//            if (entry.get() == fluid) {
//                FluidType fluidType = ((MaterialFluid) fluid).getFluidType();
//                if (fluidType == FluidTypes.ACID && !isAcidProof()) return false;
//                if (fluidType == FluidTypes.PLASMA && !isPlasmaProof()) return false;
//            }
//        }
        return true;
    }

    /**
     * This is always checked, regardless of the contained fluid being a {@link MaterialFluid} or not
     *
     * @return the maximum allowed temperature for a fluid to be stored in this container
     */
    int getMaxFluidTemperature();

    /**
     * This is always checked, regardless of the contained fluid being a {@link MaterialFluid} or not
     *
     * @return true if this fluid container allows gases, otherwise false
     */
    boolean isGasProof();

    /**
     * @see FluidTypes
     *
     * @return true if this fluid container allows acids, otherwise false
     */
    boolean isAcidProof();

    /**
     * @see FluidTypes
     *
     * @return true if this fluid container allows cryogenics, otherwise false
     */
    boolean isCryoProof();

    /**
     * @see FluidTypes
     *
     * @return true if this fluid container allows plasmas, otherwise false
     */
    boolean isPlasmaProof();
}
