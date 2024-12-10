package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.NotNull;

public class RecipeModifierList implements RecipeModifier {

    private final RecipeModifier[] modifiers;

    public RecipeModifierList(RecipeModifier... modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        ModifierFunction result = ModifierFunction.IDENTITY;
        var runningRecipe = recipe.copy();
        for (RecipeModifier modifier : modifiers) {
            var func = modifier.getModifier(machine, runningRecipe);
            runningRecipe = func.apply(runningRecipe);
            if (runningRecipe == null) return ModifierFunction.NULL;
            result = func.compose(result);

        }
        return result;
    }
}
//
// if (modifiedRecipe != null && result.getDuration() != 0) {
// if (modifiedRecipe.data.getBoolean("duration_is_total_cwu")) {
// modifiedRecipe.duration = (int) (modifiedRecipe.duration * (1.f - .025f * result.getOcLevel()));
// } else {
// modifiedRecipe.duration = result.getDuration();
// }
// if (result.getEut() > 0) {
// modifiedRecipe.tickInputs.put(EURecipeCapability.CAP, List.of(new Content(result.getEut(),
// ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null)));
// } else if (result.getEut() < 0) {
// modifiedRecipe.tickOutputs.put(EURecipeCapability.CAP, List.of(new Content(-result.getEut(),
// ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null)));
// }
//
// if (result.getParallel() > 1) {
// modifiedRecipe = ParallelLogic.applyParallel(machine, modifiedRecipe, result.getParallel(), false)
// .getFirst();
// }
// modifiedRecipe.ocLevel = result.getOcLevel();
// }
// result.reset();
