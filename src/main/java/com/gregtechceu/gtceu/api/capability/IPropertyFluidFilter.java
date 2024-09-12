package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluids.attribute.IAttributedFluid;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public interface IPropertyFluidFilter extends Predicate<FluidStack> {

    @Override
    default boolean test(@NotNull FluidStack stack) {
        Fluid fluid = stack.getFluid();
        if (FluidHelper.getTemperature(stack) < getMinFluidTemperature()) return false;

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
            if (FluidHelper.isLighterThanAir(stack) && !canContain(FluidState.GAS)) {
                return false;
            }
            if (!canContain(FluidState.LIQUID)) {
                return false;
            }
        }

        return FluidHelper.getTemperature(stack) <= getMaxFluidTemperature();
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
     * @param tooltip the tooltip to append to
     */
    default void appendTooltips(@NotNull List<Component> tooltip) {
        tooltip.add(Component.translatable("gtceu.fluid_pipe.max_temperature", getMaxFluidTemperature()));
        tooltip.add(Component.translatable("gtceu.fluid_pipe.min_temperature", getMinFluidTemperature()));
        if (isGasProof()) tooltip.add(Component.translatable("gtceu.fluid_pipe.gas_proof"));
        else tooltip.add(Component.translatable("gtceu.fluid_pipe.not_gas_proof"));
        if (isPlasmaProof()) tooltip.add(Component.translatable("gtceu.fluid_pipe.plasma_proof"));
        getContainedAttributes().forEach(a -> a.appendContainerTooltips(tooltip::add));
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
     * @return the minimum allowed temperature for a fluid
     */
    int getMinFluidTemperature();

    /**
     * This is always checked, regardless of the contained fluid being a {@link IAttributedFluid} or not
     *
     * @return whether this filter allows gases
     */
    default boolean isGasProof() {
        return canContain(FluidState.GAS);
    }

    /**
     * @return whether this filter allows plasmas
     */
    default boolean isPlasmaProof() {
        return canContain(FluidState.PLASMA);
    }
}
