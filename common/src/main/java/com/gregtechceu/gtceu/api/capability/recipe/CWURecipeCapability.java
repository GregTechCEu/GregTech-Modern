package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerInteger;

public class CWURecipeCapability extends RecipeCapability<Integer> {
    public final static CWURecipeCapability CAP = new CWURecipeCapability();

    protected CWURecipeCapability() {
        super("cwu", 0xFF22cae0, SerializerInteger.INSTANCE);
    }

    @Override
    public Integer copyInner(Integer content) {
        return content;
    }

    @Override
    public Integer copyWithModifier(Integer content, ContentModifier modifier) {
        return modifier.apply(content).intValue();
    }
}
