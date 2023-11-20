package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerInteger;
import com.gregtechceu.gtceu.api.recipe.content.SerializerLong;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote ItemRecipeCapability
 */
public class CWURecipeCapability extends RecipeCapability<Integer> {

    public final static CWURecipeCapability CAP = new CWURecipeCapability();

    protected CWURecipeCapability() {
        super("cwu", 0xFFEEEE00, SerializerInteger.INSTANCE);
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
