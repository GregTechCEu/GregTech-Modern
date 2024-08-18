package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.MethodsReturnNonnullByDefault;

import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeModifierList implements RecipeModifier {

    private final RecipeModifier[] modifiers;

    public RecipeModifierList(RecipeModifier... modifiers) {
        this.modifiers = modifiers;
    }

    @Nullable
    @Override
    public RecipeHolder<GTRecipe> apply(MetaMachine machine, @NotNull RecipeHolder<GTRecipe> recipe) {
        RecipeHolder<GTRecipe> modifiedRecipe = recipe;

        for (RecipeModifier modifier : modifiers) {
            if (modifiedRecipe != null) {
                modifiedRecipe = modifier.apply(machine, modifiedRecipe);
            }
        }

        return modifiedRecipe;
    }
}
