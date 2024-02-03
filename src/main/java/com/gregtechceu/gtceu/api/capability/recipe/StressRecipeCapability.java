package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerFloat;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapStressIngredient;

import java.util.Collection;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote StressRecipeCapability
 */
public class StressRecipeCapability extends RecipeCapability<Float> {

    public final static StressRecipeCapability CAP = new StressRecipeCapability();

    protected StressRecipeCapability() {
        super("su", 0xFF77A400, SerializerFloat.INSTANCE);
    }

    @Override
    public Float copyInner(Float content) {
        return content;
    }

    @Override
    public Float copyWithModifier(Float content, ContentModifier modifier) {
        return modifier.apply(content).floatValue();
    }

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object ingredient) {
        return List.of(new MapStressIngredient((Float) ingredient));
    }

    @Override
    public List<Float> compressIngredients(Collection<Object> ingredients) {
        return List.of(ingredients.stream().map(Float.class::cast).reduce(0f, Float::sum));
    }
}
