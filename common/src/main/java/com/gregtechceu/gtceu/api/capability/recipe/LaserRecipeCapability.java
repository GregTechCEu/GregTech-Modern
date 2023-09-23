package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
import com.gregtechceu.gtceu.api.recipe.content.SerializerLong;

public class LaserRecipeCapability extends RecipeCapability<Long> {

    public final static LaserRecipeCapability CAP = new LaserRecipeCapability();

    protected LaserRecipeCapability() {
        super("laser", 0xFFFFFF00, SerializerLong.INSTANCE);
    }

    @Override
    public Long copyInner(Long content) {
        return content;
    }

    @Override
    public Long copyWithModifier(Long content, ContentModifier modifier) {
        return modifier.apply(content).longValue();
    }
}
