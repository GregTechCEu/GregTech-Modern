package com.gregtechceu.gtceu.integration.recipeviewer.handlers;

import net.minecraft.network.chat.Component;

import lombok.Getter;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import java.util.List;

/**
 * A reason that a recipe transfer couldn't happen.
 * <p>
 * Recipe transfer errors can be created with {@link IRecipeTransferHandlerHelper} or you can implement your own.
 * These errors are returned from {@link RecipeTransferHandler#transferRecipe(Object, boolean, boolean)}.
 * </p>
 */
public abstract sealed class RecipeTransferError permits RecipeTransferError.Internal, RecipeTransferError.UserFacing,
                                                 RecipeTransferError.Cosmetic {

    public abstract boolean allowsTransfer();

    /**
     * Return the ARGB color of the additional button highlight for {@link Cosmetic}.
     * For example, return 0 to disable the colored highlight. Default color is orange.
     */
    public int getButtonHighlightColor() {
        return 0x80FFA500;
    }

    /**
     * Called on {@link UserFacing} and {@link Cosmetic} errors.
     */
    public List<Component> getTooltip() {
        return List.of();
    }

    /**
     * Get the estimated number of inputs of the recipe that cannot be found in the container.
     *
     * This is used to help sort recipes with more matches first, so that if a player
     * has many (or all) of the items required for a recipe in their inventory, it is shown first.
     *
     * @return the number of input recipes slots are missing ingredient's in the player's inventory.
     *         Return -1 by default to avoid sorting
     *
     * @since 19.2.0
     */
    public int getMissingCountHint() {
        return -1;
    }

    /**
     * Errors where the Transfer handler is broken or does not work.
     * These errors will hide the recipe transfer button, and do not display anything to the user.
     */
    public static final class Internal extends RecipeTransferError {

        public static final Internal INSTANCE = new Internal();

        private Internal() {}

        @Override
        public boolean allowsTransfer() {
            return false;
        }
    }

    /**
     * Errors that the player can fix. Missing items, inventory full, etc.
     * Something informative will be shown to the player.
     */
    public static non-sealed class UserFacing extends RecipeTransferError {

        public UserFacing() {}

        @Override
        public boolean allowsTransfer() {
            return false;
        }

        /**
         * An error that shows a tooltip.
         */
        public static class Tooltip extends UserFacing {

            @Getter
            private final List<Component> message;

            public Tooltip(Component message) {
                this(List.of(message));
            }

            public Tooltip(List<Component> message) {
                this.message = message;
            }

            @Override
            public List<Component> getTooltip() {
                return message;
            }
        }
    }

    /**
     * Errors that still allow the usage of the recipe transfer button.
     * Hovering over the button will display the error, however the button is active and can be used.
     *
     * @since 6.0.2
     */
    public static non-sealed class Cosmetic extends RecipeTransferError {

        public Cosmetic() {}

        @Override
        public boolean allowsTransfer() {
            return true;
        }
    }
}
