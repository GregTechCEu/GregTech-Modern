package brachy.modularui.integration.recipeviewer.handlers;

import brachy.modularui.ModularUI;
import brachy.modularui.integration.emi.handler.EmiScreenHandler;
import brachy.modularui.integration.jei.handler.JeiScreenHandler;
import brachy.modularui.integration.rei.handler.REIScreenHandler;
import brachy.modularui.screen.ScreenWrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class RecipeViewerHandler {

    private static RecipeViewerHandler current = null;

    @NotNull
    public static RecipeViewerHandler getCurrent() {
        if (current == null) {
            Supplier<Function<Class<ScreenWrapper>, ? extends RecipeViewerHandler>> supplier;
            if (ModularUI.Mods.EMI.isLoaded()) {
                supplier = () -> EmiScreenHandler::of;
            } else if (ModularUI.Mods.REI.isLoaded()) {
                supplier = () -> REIScreenHandler::of;
            } else if (ModularUI.Mods.JEI.isLoaded()) {
                supplier = () -> JeiScreenHandler::of;
            } else {
                supplier = () -> cls -> DUMMY;
            }
            current = supplier.get().apply(ScreenWrapper.class);
        }
        return current;
    }

    public abstract void setSearchFocused(boolean focused);

    public abstract boolean isSearchFocused();

    public abstract @Nullable Object getCurrentlyDragged();

    public boolean isHoveringOver(GhostIngredientSlot<?> slot) {
        Object currentlyDragged = getCurrentlyDragged();
        if (currentlyDragged == null) {
            return false;
        } else if (currentlyDragged instanceof Iterable<?> iterable) {
            for (Object dragged : iterable) {
                if (slot.castGhostIngredientIfValid(dragged) != null) {
                    return true;
                }
            }
            return false;
        } else {
            return slot.castGhostIngredientIfValid(currentlyDragged) != null;
        }
    }

    private static final RecipeViewerHandler DUMMY = new RecipeViewerHandler() {

        @Override
        public void setSearchFocused(boolean focused) {}

        @Override
        public boolean isSearchFocused() {
            return false;
        }

        @Override
        public @Nullable Object getCurrentlyDragged() {
            return null;
        }
    };
}
