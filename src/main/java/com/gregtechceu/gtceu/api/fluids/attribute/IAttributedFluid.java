package com.gregtechceu.gtceu.api.fluids.attribute;

import com.gregtechceu.gtceu.api.fluids.MaterialFluidState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

public interface IAttributedFluid {

    /**
     * @return the attributes on the fluid
     */
    @NotNull
    @Unmodifiable
    Collection<FluidAttribute> getAttributes();

    /**
     * @param attribute the attribute to add
     */
    void addAttribute(@NotNull FluidAttribute attribute);

    @NotNull
    MaterialFluidState getState();
}
