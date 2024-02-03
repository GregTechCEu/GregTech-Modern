package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapItemStackIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapItemStackNBTIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapTagIngredient;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.TagValueAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;

import java.util.Collection;
import java.util.List;

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
        return SizedIngredient.copy(content);
    }

    @Override
    public Ingredient copyWithModifier(Ingredient content, ContentModifier modifier) {
        return content instanceof SizedIngredient sizedIngredient ? SizedIngredient.create(sizedIngredient.getInner(), modifier.apply(sizedIngredient.getAmount()).intValue()) : SizedIngredient.create(content, modifier.apply(1).intValue());
    }

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object obj) {
        Ingredient ingredient = (Ingredient) obj;
        if (ingredient instanceof PartialNBTIngredient nbt) {
            return MapItemStackNBTIngredient.from(nbt);
        } if (ingredient instanceof SizedIngredient sized) {
            if (sized.getInner() instanceof PartialNBTIngredient nbt) {
                return MapItemStackNBTIngredient.from(nbt);
            } else if (((IngredientAccessor)sized.getInner()).getValues().length > 0 && ((IngredientAccessor)sized.getInner()).getValues()[0] instanceof Ingredient.TagValue tagValue) {
                return List.of(new MapTagIngredient(((TagValueAccessor)tagValue).getTag()));
            }
        } else if (((IngredientAccessor)ingredient).getValues().length > 0 && ((IngredientAccessor)ingredient).getValues()[0] instanceof Ingredient.TagValue tagValue) {
            return List.of(new MapTagIngredient(((TagValueAccessor)tagValue).getTag()));
        }
        return MapItemStackIngredient.from(ingredient);
    }

    @Override
    public List<Ingredient> compressIngredients(Collection<Object> ingredients) {
        List<Ingredient> list = new ObjectArrayList<>(ingredients.size());
        for (Object item : ingredients) {
            if (item instanceof Ingredient ingredient) {
                boolean isEqual = false;
                for (Ingredient obj : list) {
                    if (item.equals(obj)) {
                        isEqual = true;
                        break;
                    }
                }
                if (isEqual) continue;
                list.add(ingredient);
            }
        }
        return list;
    }

}
