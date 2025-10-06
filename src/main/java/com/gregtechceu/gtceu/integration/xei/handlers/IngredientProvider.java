package com.gregtechceu.gtceu.integration.xei.handlers;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.integration.xei.entry.EntryList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * An interface for recipe viewers to get the ingredient from a widget to show recipes for example.
 * Implement this on {@link com.gregtechceu.gtceu.api.mui.base.widget.IWidget}.
 * No further registration needed.
 *
 * @param <I> type of the ingredient
 */
public interface IngredientProvider<I> {

    EntryList<I> getIngredients();

    default UnaryOperator<I> renderMappingFunction() {
        return UnaryOperator.identity();
    }

    default float chance() {
        return 1.0f;
    }

    default IO ingredientIO() {
        return null;
    }

    /**
     * @return the class of the ingredient this slot contains
     */
    @NotNull
    Class<I> ingredientClass();

    /**
     * @return a recipeviewer-specific ingredient instance, or {@code null} if defaulting to the class-based logic.
     */
    @Nullable
    default Object ingredientOverride() {
        return null;
    }
}
