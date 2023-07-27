package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote ItemRecipeCapability
 */
public class ItemRecipeCapability extends RecipeCapability<Ingredient> {

    public final static ItemRecipeCapability CAP = new ItemRecipeCapability();

    protected ItemRecipeCapability() {
        super("item", 0xFFD96106, SerializerIngredient.INSTANCE);
    }

    @Override
    public Ingredient copyInner(Ingredient content) {
        if (content instanceof SizedIngredient sizedIngredient) {
            return SizedIngredient.copy(sizedIngredient);
        }
        if (content.getClass() == Ingredient.class) {
            return IngredientAccessor.create(Arrays.stream(((IngredientAccessor) content).getValues()));
        }
        return super.copyInner(content);
    }

    @Override
    public Ingredient copyWithModifier(Ingredient content, ContentModifier modifier) {
        Ingredient copy = copyInner(content);
        return copy instanceof SizedIngredient sizedIngredient ? SizedIngredient.create(copy, modifier.apply(sizedIngredient.getAmount()).intValue()) : SizedIngredient.create(copy, modifier.apply(1).intValue());
    }

}
