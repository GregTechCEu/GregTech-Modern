package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import net.minecraft.MethodsReturnNonnullByDefault;

import org.jetbrains.annotations.NotNull;

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
    public GTRecipe apply(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params, @NotNull OCResult result) {
        GTRecipe modifiedRecipe = recipe;

        for (RecipeModifier modifier : modifiers) {
            if (modifiedRecipe != null) {
                modifiedRecipe.duration = result.getDuration();
                modifiedRecipe.tickInputs.put(EURecipeCapability.CAP, List.of(new Content(result.getEut(),
                        ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null)));
                modifiedRecipe = modifier.apply(machine, modifiedRecipe, params, result);

            }
        }

        return modifiedRecipe;
    }
}
