package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeModifierList implements RecipeModifier {
    private final RecipeModifier[] modifiers;

    public RecipeModifierList(RecipeModifier... modifiers) {
        this.modifiers = modifiers;
    }

    @Nullable
    @Override
    public GTRecipe apply(MetaMachine machine, @Nullable GTRecipe recipe) {
        GTRecipe modifiedRecipe = recipe;

        for (RecipeModifier modifier : modifiers) {
            modifiedRecipe = modifier.apply(machine, modifiedRecipe);
        }

        return modifiedRecipe;
    }
}
