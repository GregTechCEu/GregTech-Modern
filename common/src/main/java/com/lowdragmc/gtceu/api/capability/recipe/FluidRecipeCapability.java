package com.lowdragmc.gtceu.api.capability.recipe;

import com.lowdragmc.gtceu.api.recipe.content.ContentModifier;
import com.lowdragmc.gtceu.api.recipe.content.SerializerFluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote FluidRecipeCapability
 */
public class FluidRecipeCapability extends RecipeCapability<FluidStack> {

    public final static FluidRecipeCapability CAP = new FluidRecipeCapability();

    protected FluidRecipeCapability() {
        super("fluid", SerializerFluidStack.INSTANCE);
    }

    @Override
    public FluidStack copyInner(FluidStack content) {
        return content.copy();
    }

    @Override
    public FluidStack copyWithModifier(FluidStack content, ContentModifier modifier) {
        FluidStack copy = content.copy();
        copy.setAmount(modifier.apply(copy.getAmount()).intValue());
        return copy;
    }

}
