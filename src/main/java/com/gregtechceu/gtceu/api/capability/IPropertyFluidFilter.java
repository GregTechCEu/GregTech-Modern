package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluids.attribute.IAttributedFluid;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static com.gregtechceu.gtceu.api.fluids.FluidConstants.CRYOGENIC_FLUID_THRESHOLD;

public interface IPropertyFluidFilter extends Predicate<FluidStack> {

    @Override
    default boolean test(@NotNull FluidStack stack) {
        Fluid fluid = stack.getFluid();
        FluidType fluidType = fluid.getFluidType();
        if (fluidType.getTemperature() < CRYOGENIC_FLUID_THRESHOLD && !isCryoProof()) return false;

        if (fluid instanceof IAttributedFluid attributedFluid) {
            FluidState state = attributedFluid.getState();
            if (!canContain(state)) return false;

            for (FluidAttribute attribute : attributedFluid.getAttributes()) {
                if (!canContain(attribute)) {
                    return false;
                }
            }

            // plasma ignores temperature requirements
            if (state == FluidState.PLASMA) return true;
        } else {
            if (fluidType.isLighterThanAir() && !canContain(FluidState.GAS)) {
                return false;
            }
            if (!canContain(FluidState.LIQUID)) {
                return false;
            }
        }

        return fluidType.getTemperature() <= getMaxFluidTemperature();
    }

    /**
     * @param state the state to check
     * @return if the state can be contained
     */
    boolean canContain(@NotNull FluidState state);

    /**
     * @param attribute the attribute to check
     * @return if the attribute can be contained
     */
    boolean canContain(@NotNull FluidAttribute attribute);

    /**
     * Set the container as able to contain an attribute
     *
     * @param attribute  the attribute to change containment status for
     * @param canContain whether the attribute can be contained
     */
    void setCanContain(@NotNull FluidAttribute attribute, boolean canContain);

    @NotNull
    @UnmodifiableView
    Collection<@NotNull FluidAttribute> getContainedAttributes();

    /**
     * Append tooltips about containment info
     *
     * @param tooltip             the tooltip to append to
     * @param showToolsInfo       if the "hold shift" line should mention tool info
     * @param showTemperatureInfo if the temperature information should be displayed
     */
    default void appendTooltips(@NotNull List<Component> tooltip, boolean showToolsInfo, boolean showTemperatureInfo) {
        if (GTUtil.isShiftDown()) {
            if (showTemperatureInfo)
                tooltip.add(Component.translatable("gtceu.fluid_pipe.max_temperature",
                        FormattingUtil.formatNumbers(getMaxFluidTemperature())));
            if (isGasProof()) tooltip.add(Component.translatable("gtceu.fluid_pipe.gas_proof"));
            else tooltip.add(Component.translatable("gtceu.fluid_pipe.not_gas_proof"));
            if (isPlasmaProof()) tooltip.add(Component.translatable("gtceu.fluid_pipe.plasma_proof"));
            if (isCryoProof()) tooltip.add(Component.translatable("gtceu.fluid_pipe.cryo_proof"));
            getContainedAttributes().forEach(a -> a.appendContainerTooltips(tooltip::add));
        } else if (isGasProof() || isCryoProof() || isPlasmaProof() || !getContainedAttributes().isEmpty()) {
            if (showToolsInfo) {
                tooltip.add(Component.translatable("gtceu.tooltip.tool_fluid_hold_shift"));
            } else {
                tooltip.add(Component.translatable("gtceu.tooltip.fluid_pipe_hold_shift"));
            }
        }
    }

    /**
     * This is always checked, regardless of the contained fluid being a {@link IAttributedFluid} or not
     *
     * @return the maximum allowed temperature for a fluid
     */
    int getMaxFluidTemperature();

    /**
     * This is always checked, regardless of the contained fluid being a {@link IAttributedFluid} or not
     *
     * @return whether this filter allows gases
     */
    boolean isGasProof();

    /**
     * @return whether this filter allows cryogenic fluids
     */
    boolean isCryoProof();

    /**
     * @return whether this filter allows plasmas
     */
    boolean isPlasmaProof();
}
