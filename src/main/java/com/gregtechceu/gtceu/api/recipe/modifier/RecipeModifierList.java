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

import java.util.List;

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
    public GTRecipe apply(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params,
                          @NotNull OCResult result) {
        GTRecipe modifiedRecipe = recipe;
        for (RecipeModifier modifier : modifiers) {
            if (modifiedRecipe != null) {
                modifiedRecipe = modifier.apply(machine, modifiedRecipe, params, result);
            }
        }

        if (modifiedRecipe != null && result.getDuration() != 0) {
            if (modifiedRecipe.data.getBoolean("duration_is_total_cwu")) {
                modifiedRecipe.duration = (int) (modifiedRecipe.duration * (1.f - .025f * result.getOcLevel()));
            } else {
                modifiedRecipe.duration = result.getDuration();
            }
            if (result.getEut() > 0) {
                modifiedRecipe.tickInputs.put(EURecipeCapability.CAP, List.of(new Content(result.getEut(),
                        ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null)));
            } else if (result.getEut() < 0) {
                modifiedRecipe.tickOutputs.put(EURecipeCapability.CAP, List.of(new Content(-result.getEut(),
                        ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null)));
            }

            if (result.getParallel() > 1) {
                modifiedRecipe = ParallelLogic.applyParallel(machine, modifiedRecipe, result.getParallel(), false)
                        .getFirst();
            }
            modifiedRecipe.ocLevel = result.getOcLevel();
        }
        result.reset();

        return modifiedRecipe;
    }
}
