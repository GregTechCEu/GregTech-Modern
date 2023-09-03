package com.gregtechceu.gtceu.api.capability.recipe.forge;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.forge.SerializerGasStack;
import mekanism.api.chemical.gas.GasStack;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote FluidRecipeCapability
 */
public class GasRecipeCapability extends RecipeCapability<GasStack> {

    public final static GasRecipeCapability CAP = new GasRecipeCapability();

    protected GasRecipeCapability() {
        super("gas", 0xFF3C70EE, true, SerializerGasStack.INSTANCE);
    }

    @Override
    public GasStack copyInner(GasStack content) {
        return content.copy();
    }

    @Override
    public GasStack copyWithModifier(GasStack content, ContentModifier modifier) {
        if (content.isEmpty()) return content.copy();
        GasStack copy = content.copy();
        copy.setAmount(modifier.apply(copy.getAmount()).longValue());
        return copy;
    }

}
