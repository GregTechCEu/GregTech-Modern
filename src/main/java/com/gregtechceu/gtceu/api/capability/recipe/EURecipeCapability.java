package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerLong;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapEUIngredient;

import java.util.Collection;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote ItemRecipeCapability
 */
public class EURecipeCapability extends RecipeCapability<Long> {

    public final static EURecipeCapability CAP = new EURecipeCapability();

    protected EURecipeCapability() {
        super("eu", 0xFFFFFF00, SerializerLong.INSTANCE);
    }

    @Override
    public Long copyInner(Long content) {
        return content;
    }

    @Override
    public Long copyWithModifier(Long content, ContentModifier modifier) {
        return modifier.apply(content).longValue();
    }

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object ingredient) {
        return List.of(new MapEUIngredient((Long) ingredient));
    }

    @Override
    public List<Long> compressIngredients(Collection<Object> ingredients) {
        return List.of(ingredients.stream().map(Long.class::cast).reduce(0L, Long::sum));
    }
}
