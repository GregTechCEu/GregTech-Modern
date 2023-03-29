package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.SerializerFluidStack;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote FluidRecipeCapability
 */
public class FluidRecipeCapability extends RecipeCapability<FluidStack> {

    public final static FluidRecipeCapability CAP = new FluidRecipeCapability();

    protected FluidRecipeCapability() {
        super("fluid", 0xFF3C70EE, SerializerFluidStack.INSTANCE);
    }

    @Override
    public FluidStack copyInner(FluidStack content) {
        return content.copy();
    }

    @Override
    public FluidStack copyWithModifier(FluidStack content, ContentModifier modifier) {
        if (content.isEmpty()) return content.copy();
        FluidStack copy = content.copy();
        copy.setAmount(modifier.apply(copy.getAmount()).intValue());
        return copy;
    }

}
