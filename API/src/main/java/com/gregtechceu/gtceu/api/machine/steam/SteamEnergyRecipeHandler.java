package com.gregtechceu.gtceu.api.machine.steam;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/15
 * @implNote SteamEnergyRecipeHandler
 */
public class SteamEnergyRecipeHandler implements IRecipeHandler<Long> {

    private final NotifiableFluidTank steamTank;
    private final double conversionRate; // energy units per millibucket

    public SteamEnergyRecipeHandler(NotifiableFluidTank steamTank, double conversionRate) {
        this.steamTank = steamTank;
        this.conversionRate = conversionRate;
    }

    @Override
    public List<Long> handleRecipeInner(IO io, GTRecipe recipe, List<Long> left, @Nullable String slotName,
                                        boolean simulate) {
        long sum = left.stream().reduce(0L, Long::sum);
        int realSum = (int) Math.ceil(sum * conversionRate);
        if (realSum > 0) {
            var steam = io == IO.IN ? FluidIngredient.of(GTMaterials.Steam.getFluidTag(), realSum) :
                    FluidIngredient.of(GTMaterials.Steam.getFluid(realSum));
            var list = new ArrayList<FluidIngredient>();
            list.add(steam);
            var leftSteam = steamTank.handleRecipeInner(io, recipe, list, slotName, simulate);
            if (leftSteam == null || leftSteam.isEmpty()) return null;
            sum = (long) (leftSteam.get(0).getAmount() / conversionRate);
        }
        return sum <= 0 ? null : Collections.singletonList(sum);
    }

    @Override
    public List<Object> getContents() {
        List<FluidStack> tankContents = new ArrayList<>();
        for (int i = 0; i < steamTank.getTanks(); ++i) {
            FluidStack stack = steamTank.getFluidInTank(i);
            if (!stack.isEmpty()) {
                tankContents.add(stack);
            }
        }
        long sum = tankContents.stream().map(FluidStack::getAmount).reduce(0, Integer::sum);
        long realSum = (long) Math.ceil(sum * conversionRate);
        return List.of(realSum);
    }

    @Override
    public double getTotalContentAmount() {
        List<FluidStack> tankContents = new ArrayList<>();
        for (int i = 0; i < steamTank.getTanks(); ++i) {
            FluidStack stack = steamTank.getFluidInTank(i);
            if (!stack.isEmpty()) {
                tankContents.add(stack);
            }
        }
        long sum = tankContents.stream().map(FluidStack::getAmount).reduce(0, Integer::sum);
        return (long) Math.ceil(sum * conversionRate);
    }

    @Override
    public RecipeCapability<Long> getCapability() {
        return EURecipeCapability.CAP;
    }

    public long getCapacity() {
        return steamTank.getTankCapacity(0);
    }

    public long getStored() {
        FluidStack stack = steamTank.getFluidInTank(0);
        if (stack != FluidStack.EMPTY) {
            return stack.getAmount();
        }
        return 0;
    }
}
