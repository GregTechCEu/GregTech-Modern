package com.gregtechceu.gtceu.api.recipe.ingredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.IntersectionFluidIngredient;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FluidIngredientEquality {

    public static final Comparator<FluidStack> STACK_COMPARATOR = Comparator
            .comparing(stack -> BuiltInRegistries.FLUID.getKey(stack.getFluid()));

    public static final Comparator<FluidStack> FLUID_STACK_COMPARATOR = Comparator.comparingInt(FluidStack::getAmount)
            .thenComparing(FluidStack::getFluid, Comparator.comparing(BuiltInRegistries.FLUID::getKey));

    public static final Comparator<FluidIngredient> INGREDIENT_COMPARATOR = new Comparator<>() {

        @Override
        public int compare(FluidIngredient first, FluidIngredient second) {
            if (first instanceof DataComponentFluidIngredient strict1 && strict1.isStrict()) {
                if (second instanceof DataComponentFluidIngredient strict2 && strict2.isStrict()) {
                    return strict1.test(strict2.generateStacks().findFirst().orElse(FluidStack.EMPTY)) ? 0 : 1;
                }
                return 1;
            }
            if (first instanceof DataComponentFluidIngredient partial1 && !partial1.isStrict()) {
                if (second instanceof DataComponentFluidIngredient partial2 && !partial2.isStrict()) {
                    if (partial1.getStacks().length != partial2.getStacks().length)
                        return 1;
                    for (FluidStack stack : partial1.getStacks()) {
                        if (!partial2.test(stack)) {
                            return 1;
                        }
                    }
                    return 0;
                }
                return 1;
            }

            if (first instanceof IntersectionFluidIngredient intersection1) {
                if (second instanceof IntersectionFluidIngredient intersection2) {
                    List<FluidIngredient> ingredients1 = Lists.newArrayList(intersection1.children());
                    List<FluidIngredient> ingredients2 = Lists.newArrayList(intersection2.children());
                    if (ingredients1.size() != ingredients2.size()) return 1;

                    ingredients1.sort(this);
                    ingredients2.sort(this);

                    for (int i = 0; i < ingredients1.size(); ++i) {
                        FluidIngredient ingredient1 = ingredients1.get(i);
                        FluidIngredient ingredient2 = ingredients2.get(i);
                        int result = compare(ingredient1, ingredient2);
                        if (result != 0) {
                            return result;
                        }
                    }
                    return 0;
                }
                return 1;
            }

            if (first.getStacks().length != second.getStacks().length)
                return first.getStacks().length - second.getStacks().length;
            FluidStack[] values1 = first.getStacks();
            FluidStack[] values2 = first.getStacks();
            if (values1.length != values2.length) return 1;

            Arrays.parallelSort(values1, FLUID_STACK_COMPARATOR);
            Arrays.parallelSort(values2, FLUID_STACK_COMPARATOR);

            for (int i = 0; i < values1.length; ++i) {
                FluidStack value1 = values1[i];
                FluidStack value2 = values2[i];
                int result = FLUID_STACK_COMPARATOR.compare(value1, value2);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }
    };
}
