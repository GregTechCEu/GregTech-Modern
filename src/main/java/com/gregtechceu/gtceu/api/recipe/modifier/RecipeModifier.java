package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mutates a recipe in-place for the given machine state.
 */
@FunctionalInterface
public interface RecipeModifier {

    RecipeModifier NO_MODIFIER = (m, g, r) -> null;

    Component DEFAULT_FAILURE = Component.translatable("gtceu.recipe_modifier.default_fail");

    /**
     * Applies this modifier to the passed recipe.
     * 
     * @param machine the machine which is requesting the modified recipe
     * @param recipe  the recipe to be modified in-place
     * @return the failure reason, or {@code null} if the modifier succeeded
     */
    @Nullable
    Component apply(@NotNull MetaMachine machine, RecipeHandlerGroup group, @NotNull GTRecipe recipe);

    /**
     * Utility method that logs the incorrect use of a RecipeModifier
     * 
     * @param type   the class of machine that is required by the RecipeModifier
     * @param actual the actual machine that was passed to the RecipeModifier
     * @return {@link #DEFAULT_FAILURE}
     */
    static Component nullWrongType(Class<?> type, MetaMachine actual) {
        GTCEu.LOGGER.error("Incorrect use of modifier, expected machine of type {}, received {}", type.getSimpleName(),
                actual.getDefinition().getName());
        return DEFAULT_FAILURE;
    }
}
