package com.gregtechceu.gtceu.integration.recipeviewer.handlers;

import org.jetbrains.annotations.ApiStatus;

/**
 * An interface to handle recipe transfers.
 * Implement this on {@link com.gregtechceu.gtceu.client.mui.screen.ModularScreen}.
 * No further registration needed.
 */
@ApiStatus.Experimental
public interface RecipeTransferHandler<T> {

    @SuppressWarnings("unchecked")
    @ApiStatus.NonExtendable
    @ApiStatus.Internal
    default RecipeTransferError transferRecipeSafe(Object recipe, boolean maxTransfer, boolean simulate) {
        return this.transferRecipe((T) recipe, maxTransfer, simulate);
    }

    /**
     * Transfers a recipe viewer recipe.
     *
     * @param recipe      the recipe
     * @param maxTransfer true if shift is being held
     * @param simulate    if the transfer is simulated
     * @return a transfer error or null if successful
     */
    RecipeTransferError transferRecipe(T recipe, boolean maxTransfer, boolean simulate);

    Class<T> getRecipeClass();
}
