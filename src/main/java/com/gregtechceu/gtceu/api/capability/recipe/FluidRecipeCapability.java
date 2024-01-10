package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.SerializerFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote FluidRecipeCapability
 */
public class FluidRecipeCapability extends RecipeCapability<FluidIngredient> {

    public final static FluidRecipeCapability CAP = new FluidRecipeCapability();

    protected FluidRecipeCapability() {
        super("fluid", 0xFF3C70EE, SerializerFluidIngredient.INSTANCE);
    }

    @Override
    public FluidIngredient copyInner(FluidIngredient content) {
        return content.copy();
    }

    @Override
    public FluidIngredient copyWithModifier(FluidIngredient content, ContentModifier modifier) {
        if (content.isEmpty()) return content.copy();
        FluidIngredient copy = content.copy();
        copy.setAmount(modifier.apply(copy.getAmount()).intValue());
        return copy;
    }

}
