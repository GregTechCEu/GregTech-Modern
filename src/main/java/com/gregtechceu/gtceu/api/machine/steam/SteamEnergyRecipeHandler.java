package com.gregtechceu.gtceu.api.machine.steam;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.utils.GTMath;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SteamEnergyRecipeHandler implements IRecipeHandler<EnergyStack> {

    private final NotifiableFluidTank steamTank;
    private final double conversionRate; // mB steam per EU

    public SteamEnergyRecipeHandler(NotifiableFluidTank steamTank, double conversionRate) {
        this.steamTank = steamTank;
        this.conversionRate = conversionRate;
    }

    @Override
    public List<EnergyStack> handleRecipeInner(IO io, GTRecipe recipe, List<EnergyStack> left, boolean simulate) {
        for (var it = left.listIterator(); it.hasNext();) {
            EnergyStack stack = it.next();
            if (stack.isEmpty()) {
                it.remove();
                continue;
            }

            long totalEU = stack.getTotalEU();
            int totalSteam = GTMath.saturatedCast((long) Math.ceil(totalEU * conversionRate));
            if (totalSteam > 0) {
                var steam = io == IO.IN ? SizedFluidIngredient.of(GTMaterials.Steam.getFluidTag(), totalSteam) :
                        SizedFluidIngredient.of(GTMaterials.Steam.getFluid(totalSteam));
                List<SizedFluidIngredient> list = new ArrayList<>();
                list.add(steam);
                List<SizedFluidIngredient> leftSteam = steamTank.handleRecipeInner(io, recipe, list, simulate);
                if (leftSteam == null || leftSteam.isEmpty()) {
                    it.remove();
                } else {
                    totalEU = (long) (leftSteam.get(0).amount() / conversionRate);
                    it.set(new EnergyStack(totalEU));
                }
            }
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    public @NotNull List<Object> getContents() {
        List<FluidStack> tankContents = new ArrayList<>();
        for (int i = 0; i < steamTank.getTanks(); ++i) {
            FluidStack stack = steamTank.getFluidInTank(i);
            if (!stack.isEmpty()) {
                tankContents.add(stack);
            }
        }
        long sum = tankContents.stream().mapToLong(FluidStack::getAmount).sum();
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
        long sum = tankContents.stream().mapToLong(FluidStack::getAmount).sum();
        return (long) Math.ceil(sum * conversionRate);
    }

    @Override
    public RecipeCapability<EnergyStack> getCapability() {
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
