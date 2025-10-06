package com.gregtechceu.gtceu.common.valueprovider;

import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;

/**
 * Returns a new {@link IntProvider} with a {@link ContentModifier} applied. Mainly for use in modifying the providers
 * used in {@link com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient}
 * and {@link com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient}
 * for recipe batches/parallels.
 */
public class ModifiedIntProvider {

    public static IntProvider of(IntProvider source, ContentModifier modifier) {
        if (source instanceof UniformInt uniform) {
            return UniformInt.of(modifier.apply(uniform.getMinValue()), modifier.apply(uniform.getMaxValue()));
        }
        if (source instanceof BiasedToBottomInt biased) {
            return BiasedToBottomInt.of(modifier.apply(biased.getMinValue()), modifier.apply(biased.getMaxValue()));
        }
        return new FlooredInt(
                new AddedFloat(
                        new MultipliedFloat(
                                new CastedFloat(source),
                                ConstantFloat.of((float) modifier.multiplier())),
                        ConstantFloat.of((float) modifier.addition())));
    }
}
