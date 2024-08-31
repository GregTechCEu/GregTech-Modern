package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ExtendedOutputItem extends OutputItem implements OutputReplacement {
    public SizedIngredient ingredient;

    public ExtendedOutputItem(Ingredient ingredient, int count) {
        super(SizedIngredient.create(ingredient, count).kjs$getFirst(), Double.NaN, null);
        this.ingredient = SizedIngredient.create(ingredient, count);
    }

    private ExtendedOutputItem(OutputItem item) {
        this(Ingredient.of(item.item), item.getCount());
    }
    public static ExtendedOutputItem of(Object o) {
        return new ExtendedOutputItem(OutputItem.of(o));
    }
    public static ExtendedOutputItem fromOutputItem(OutputItem item) {
        return new ExtendedOutputItem(item);
    }
    @Override
    public OutputItem withCount(int count) {
        ingredient = SizedIngredient.create(ingredient.getInner(), count);
        return super.withCount(count);
    }
    @Override
    public int getCount() {
        return ingredient.getAmount();
    }
    @Override
    public Object replaceOutput(RecipeJS recipe, ReplacementMatch match, OutputReplacement original) {
        if (original instanceof ExtendedOutputItem o) {
            return new ExtendedOutputItem(o.ingredient, this.ingredient.getAmount());
        }
        return this;
    }
}
