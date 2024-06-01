package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.core.ISizedFluidIngredient;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class RecipeHelper {

    public static int getRatioForDistillery(FluidIngredient fluidInput, FluidIngredient fluidOutput,
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

    public static boolean isFluidStackDivisibleForDistillery(FluidIngredient fluidStack, int divisor) {
        int amount = ((ISizedFluidIngredient)fluidStack).getAmount();
        return amount % divisor == 0 && amount / divisor >= 25;
    }
}
