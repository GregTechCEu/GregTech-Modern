package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import dev.latvian.mods.kubejs.core.IngredientKJS;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.MapJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.Wrapper;
import org.jetbrains.annotations.Nullable;

public class ExtendedOutputItem extends OutputItem implements OutputReplacement {

    public SizedIngredient ingredient;

    public ExtendedOutputItem(Ingredient ingredient, int count, IntProvider rolls) {
        super(((IngredientKJS) ingredient).kjs$getFirst().copyWithCount(count), Double.NaN, rolls);
        this.ingredient = SizedIngredient.create(ingredient, count);
    }

    public ExtendedOutputItem(ItemStack stack, IntProvider rolls) {
        super(stack, Double.NaN, rolls);
        this.ingredient = SizedIngredient.create(stack);
    }

    public static ExtendedOutputItem of(Ingredient ingredient, int count) {
        if (ingredient instanceof SizedIngredient sized) {
            ingredient = sized.getInner();
            if (count == 1) return of(ingredient, sized.getAmount());
        }
        IntProvider rolls = null;
        if (ingredient instanceof IntProviderIngredient intProvider) {
            rolls = intProvider.getCountProvider();
            ingredient = intProvider.getInner();
        }
        return new ExtendedOutputItem(ingredient, count, rolls);
    }

    public static ExtendedOutputItem of(Object o) {
        return of(o, null);
    }

    public static ExtendedOutputItem of(Object o, @Nullable RecipeJS recipe) {
        if (o instanceof Wrapper w) {
            o = w.unwrap();
        }

        if (o instanceof ExtendedOutputItem extendedOutput) {
            return extendedOutput;
        } else if (o instanceof InputItem input) {
            return ExtendedOutputItem.of(input.ingredient, input.count);
        } else if (o instanceof IntProviderIngredient intProvider) {
            return new ExtendedOutputItem(intProvider.getInner(), 1, intProvider.getCountProvider());
        }

        OutputItem item = recipe != null ? recipe.readOutputItem(o) : OutputItem.of(o);
        IntProvider rolls = item.rolls;

        var map = MapJS.of(o);
        if (map != null && map.containsKey("count_provider")) {
            IntProvider intProvider = UtilsJS.intProviderOf(map.get("count_provider"));
            if (!(intProvider instanceof ConstantInt c && c.getValue() == 0)) {
                rolls = intProvider;
            }
        }
        return new ExtendedOutputItem(item.item, rolls);
    }

    public static ExtendedOutputItem fromOutputItem(OutputItem item) {
        if (item instanceof ExtendedOutputItem extended) {
            return extended;
        }
        return new ExtendedOutputItem(item.item, item.rolls);
    }

    @Override
    public OutputItem withCount(int count) {
        return new ExtendedOutputItem(ingredient.getInner(), count, rolls);
    }

    @Override
    public OutputItem withRolls(IntProvider rolls) {
        Ingredient ingredient = this.ingredient.getInner();
        if (ingredient instanceof IntProviderIngredient intProvider) {
            ingredient = intProvider.getInner();
        }
        return new ExtendedOutputItem(ingredient, 1, rolls);
    }

    @Override
    public int getCount() {
        return ingredient.getAmount();
    }

    @Override
    public Object replaceOutput(RecipeJS recipe, ReplacementMatch match, OutputReplacement original) {
        if (original instanceof OutputItem o) {
            return new ExtendedOutputItem(this.ingredient.getInner(), o.getCount(), o.rolls);
        }
        return new ExtendedOutputItem(this.ingredient.getInner(), this.getCount(), rolls);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InputItem ignoreNBT() {
        var console = ConsoleJS.getCurrent(ConsoleJS.SERVER);
        console.warn("You don't need to call .ignoreNBT() anymore, all item ingredients ignore NBT by default!");
        return InputItem.of(ingredient.getInner(), ingredient.getAmount());
    }
}
