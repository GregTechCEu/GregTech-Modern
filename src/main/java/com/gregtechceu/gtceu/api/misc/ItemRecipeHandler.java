package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemRecipeHandler implements IRecipeHandler<Ingredient> {

    @Getter
    public final IO handlerIO;
    public final CustomItemStackHandler storage;

    public ItemRecipeHandler(IO handlerIO, int slots) {
        this.handlerIO = handlerIO;
        this.storage = new CustomItemStackHandler(slots);
    }

    @Override
    public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, boolean simulate) {
        return NotifiableItemStackHandler.handleRecipe(io, recipe, left, simulate, this.handlerIO, storage);
    }

    @Override
    public @NotNull List<Object> getContents() {
        List<ItemStack> ingredients = new ArrayList<>();
        for (int i = 0; i < storage.getSlots(); ++i) {
            ItemStack stack = storage.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ingredients.add(stack);
            }
        }
        return new ArrayList<>(ingredients);
    }

    @Override
    public double getTotalContentAmount() {
        long amount = 0;
        for (int i = 0; i < storage.getSlots(); ++i) {
            ItemStack stack = storage.getStackInSlot(i);
            if (!stack.isEmpty()) {
                amount += stack.getCount();
            }
        }
        return amount;
    }

    @Override
    public int getSize() {
        return this.storage.getSlots();
    }

    @Override
    public RecipeCapability<Ingredient> getCapability() {
        return ItemRecipeCapability.CAP;
    }
}
