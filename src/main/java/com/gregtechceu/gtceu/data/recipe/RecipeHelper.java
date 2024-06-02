package com.gregtechceu.gtceu.data.recipe;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import org.jetbrains.annotations.Nullable;

public class RecipeHelper {

    public static int getRatioForDistillery(SizedFluidIngredient fluidInput, SizedFluidIngredient fluidOutput,
                                            @Nullable ItemStack output) {
        int[] divisors = new int[] { 2, 5, 10, 25, 50 };
        int ratio = -1;

        for (int divisor : divisors) {

            if (!isFluidStackDivisibleForDistillery(fluidInput, divisor))
                continue;

            if (!isFluidStackDivisibleForDistillery(fluidOutput, divisor))
                continue;

            if (output != null && output.getCount() % divisor != 0)
                continue;

            ratio = divisor;
        }

        return Math.max(1, ratio);
    }

    public static boolean isFluidStackDivisibleForDistillery(SizedFluidIngredient fluidStack, int divisor) {
        return fluidStack.amount() % divisor == 0 && fluidStack.amount() / divisor >= 25;
    }
}
