package com.gregtechceu.gtceu.common.valueprovider;

import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

import net.minecraft.util.valueproviders.*;

/**
 * Returns a new {@link IntProvider} with a {@link ContentModifier} applied. Mainly for use in modifying the providers
 * used in {@link com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient}
 * and {@link com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient}
 * for recipe batches/parallels.
 */
public class ModifiedIntProvider {

    public static IntProvider of(IntProvider source, ContentModifier modifier) {
        if (source instanceof ClampedNormalInt normal) {
            return ofNormal(normal, modifier);
        }
        if (source instanceof UniformInt uniform) {
            return ofNormal(uniform, modifier);
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

    public static ClampedNormalInt ofNormal(IntProvider source, ContentModifier modifier) {
        int min = modifier.apply(source.getMinValue());
        int max = modifier.apply(source.getMaxValue());

        float mean = (min + max) / 2f;
        int s = max - min + 1;
        float sd = (float) Math.sqrt((s * s - 1) / 12f);

        return ClampedNormalInt.of(mean, sd, min, max);
    }
}
