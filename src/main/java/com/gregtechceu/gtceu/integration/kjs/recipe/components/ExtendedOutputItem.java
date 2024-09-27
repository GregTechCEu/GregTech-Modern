package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.google.gson.JsonElement;
import dev.latvian.mods.kubejs.core.IngredientKJS;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.NativeObject;

import java.util.Map;

public class ExtendedOutputItem extends OutputItem implements OutputReplacement {

    public SizedIngredient ingredient;

    public ExtendedOutputItem(Ingredient ingredient, int count) {
        super(((IngredientKJS) ingredient).kjs$getFirst().copyWithCount(count), Double.NaN, null);
        // reset the ingredient if it's an int provider.
        if (ingredient instanceof IntProviderIngredient intProvider) {
            intProvider.setItemStacks(null);
            intProvider.setSampledCount(null);
        }
        this.ingredient = SizedIngredient.create(ingredient, count);
    }

    public ExtendedOutputItem(ItemStack stack) {
        super(stack, Double.NaN, null);
        this.ingredient = SizedIngredient.create(StrictNBTIngredient.of(stack));
    }

    private ExtendedOutputItem(OutputItem item) {
        this(Ingredient.of(item.item), item.getCount());
    }

    public static ExtendedOutputItem of(Object o) {
        if (o instanceof ExtendedOutputItem extendedOutput) {
            return extendedOutput;
        } else if (o instanceof ItemStack stack) {
            if (stack.hasTag()) {
                return new ExtendedOutputItem(StrictNBTIngredient.of(stack), stack.getCount());
            } else {
                return new ExtendedOutputItem(Ingredient.of(stack), stack.getCount());
            }
        } else if (o instanceof InputItem input) {
            return new ExtendedOutputItem(input.ingredient, input.count);
        } else if (o instanceof OutputItem output) {
            return ExtendedOutputItem.fromOutputItem(output);
        } else if (o instanceof NativeObject nativeObject) {
            InputItem input = InputItem.of(nativeObject);
            return new ExtendedOutputItem(input.ingredient, input.count);
        } else if (o instanceof JsonElement json) {
            InputItem input = InputItem.of(json);
            return new ExtendedOutputItem(input.ingredient, input.count);
        } else if (o instanceof CompoundTag tag) {
            InputItem input = InputItem.of(tag);
            return new ExtendedOutputItem(input.ingredient, input.count);
        } else if (o instanceof Map<?, ?> map) {
            InputItem input = InputItem.of(map);
            return new ExtendedOutputItem(input.ingredient, input.count);
        }

        OutputItem output = OutputItem.of(o);
        if (output.item.hasTag()) {
            return new ExtendedOutputItem(StrictNBTIngredient.of(output.item), output.getCount());
        }
        return new ExtendedOutputItem(output);
    }

    public static ExtendedOutputItem fromOutputItem(OutputItem item) {
        if (item instanceof ExtendedOutputItem extended) {
            return extended;
        }
        return new ExtendedOutputItem(item);
    }

    @Override
    public OutputItem withCount(int count) {
        ingredient = SizedIngredient.create(ingredient.getInner(), count);
        return super.withCount(count);
    }

    @Override
    public OutputItem withRolls(IntProvider rolls) {
        IntProviderIngredient ingredient;
        if (this.ingredient.getInner() instanceof IntProviderIngredient intProvider) {
            ingredient = new IntProviderIngredient(intProvider.getInner(), rolls);
        } else {
            ingredient = new IntProviderIngredient(this.ingredient.getInner(), rolls);
        }
        return new ExtendedOutputItem(ingredient, this.ingredient.getAmount());
    }

    @Override
    public int getCount() {
        return ingredient.getAmount();
    }

    @Override
    public Object replaceOutput(RecipeJS recipe, ReplacementMatch match, OutputReplacement original) {
        if (original instanceof ExtendedOutputItem o) {
            return new ExtendedOutputItem(this.ingredient, o.getCount());
        }
        return super.replaceOutput(recipe, match, original);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InputItem ignoreNBT() {
        var console = ConsoleJS.getCurrent(ConsoleJS.SERVER);
        console.warn("You don't need to call .ignoreNBT() anymore, all item ingredients ignore NBT by default!");
        return InputItem.of(ingredient.getInner(), ingredient.getAmount());
    }
}
