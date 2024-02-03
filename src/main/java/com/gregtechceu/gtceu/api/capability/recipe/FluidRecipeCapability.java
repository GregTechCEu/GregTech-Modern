package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.SerializerFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapFluidIngredient;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collection;
import java.util.List;

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

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object ingredient) {
        return MapFluidIngredient.from((FluidIngredient) ingredient);
    }

    @Override
    public List<FluidIngredient> compressIngredients(Collection<Object> ingredients) {
        List<FluidIngredient> list = new ObjectArrayList<>(ingredients.size());
        for (Object item : ingredients) {
            if (item instanceof FluidIngredient fluid) {
                boolean isEqual = false;
                for (FluidIngredient obj : list) {
                    if (item.equals(obj)) {
                        isEqual = true;
                        break;
                    }
                }
                if (isEqual) continue;
                list.add(fluid);

            }
        }
        return list;
    }
}
