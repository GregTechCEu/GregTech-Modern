package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FluidRecipeHandler implements IRecipeHandler<SizedFluidIngredient> {

    @Getter
    public final IO handlerIO;
    @Persisted
    @Getter
    private final CustomFluidTank[] storages;

    public FluidRecipeHandler(IO handlerIO, int slots, int capacity) {
        this.handlerIO = handlerIO;
        this.storages = new CustomFluidTank[slots];
        for (int i = 0; i < this.storages.length; i++) {
            this.storages[i] = new CustomFluidTank(capacity);
        }
    }

    @Override
    public List<SizedFluidIngredient> handleRecipeInner(IO io, GTRecipe recipe,
                                                        List<SizedFluidIngredient> left,
                                                        @Nullable String slotName, boolean simulate) {
        return NotifiableFluidTank.handleIngredient(io, recipe, left, simulate, this.handlerIO, getStorages());
    }

    @Override
    public List<Object> getContents() {
        List<FluidStack> ingredients = new ArrayList<>();
        for (CustomFluidTank storage : getStorages()) {
            FluidStack stack = storage.getFluid();
            if (!stack.isEmpty()) {
                ingredients.add(stack);
            }
        }
        return Arrays.asList(ingredients.toArray());
    }

    @Override
    public double getTotalContentAmount() {
        long amount = 0;
        for (CustomFluidTank storage : getStorages()) {
            FluidStack stack = storage.getFluid();
            if (!stack.isEmpty()) {
                amount += stack.getAmount();
            }
        }
        return amount;
    }

    @Override
    public int getSize() {
        return getStorages().length;
    }

    @Override
    public RecipeCapability<SizedFluidIngredient> getCapability() {
        return FluidRecipeCapability.CAP;
    }
}
