package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a function that returns a {@link ModifierFunction} given a {@link MetaMachine} and {@link GTRecipe}
 * <p>
 * The ModifierFunction describes how this RecipeModifier wants to modify the recipe for the given state
 * </p>
 */
@FunctionalInterface
public interface RecipeModifier {

    RecipeModifier NO_MODIFIER = (m, r) -> ModifierFunction.IDENTITY;

    /**
     * Get the ModifierFunction for the given state
     * 
     * @param machine the machine which is requesting the modifier
     * @param recipe  the recipe - will not be mutated
     * @return A {@link ModifierFunction} describing how the recipe should be modified
     */
    @Contract(pure = true)
    @NotNull
    ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe);

    /**
     * Gets the ModifierFunction for the given state and immediately applies it to the passed recipe
     * 
     * @param machine the machine which is requesting the modified recipe
     * @param recipe  the recipe to be modified - will not be mutated
     * @return A new {@link GTRecipe} which is the modified version of the argument, or {@code null} if the modifier
     *         fails
     */
    @Contract(pure = true)
    default @Nullable GTRecipe applyModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        return getModifier(machine, recipe).apply(recipe);
    }

    /**
     * Utility method that logs the incorrect use of a RecipeModifier
     * 
     * @param type   the class of machine that is required by the RecipeModifier
     * @param actual the actual machine that was passed to the RecipeModifier
     * @return {@link ModifierFunction#NULL}
     */
    static ModifierFunction nullWrongType(Class<?> type, MetaMachine actual) {
        GTCEu.LOGGER.error("Incorrect use of modifier, expected machine of type {}, received {}", type.getSimpleName(),
                actual.getDefinition().getName());
        return ModifierFunction.NULL;
    }
}
