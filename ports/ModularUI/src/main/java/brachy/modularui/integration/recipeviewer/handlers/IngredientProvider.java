package brachy.modularui.integration.recipeviewer.handlers;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.entry.EntryList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * An interface for recipe viewers to get the ingredient from a widget to show recipes for example.
 * Implement this on {@link IWidget}.
 * No further registration needed.
 *
 * @param <I> type of the ingredient
 */
public interface IngredientProvider<I> extends IWidget {

    EntryList<I> getIngredients();

    default UnaryOperator<I> renderMappingFunction() {
        return UnaryOperator.identity();
    }

    default float chance() {
        return 1.0f;
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

    @NotNull
    default RecipeSlotRole getRecipeRole() {
        return RecipeSlotRole.RENDER_ONLY;
    }
}
