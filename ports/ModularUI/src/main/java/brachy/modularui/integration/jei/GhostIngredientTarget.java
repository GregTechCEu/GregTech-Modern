package brachy.modularui.integration.jei;

import brachy.modularui.integration.recipeviewer.handlers.GhostIngredientSlot;

import net.minecraft.client.renderer.Rect2i;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;

public class GhostIngredientTarget<I> implements IGhostIngredientHandler.Target<I> {

    private final GhostIngredientSlot<I> ghostSlot;

    public GhostIngredientTarget(GhostIngredientSlot<I> ghostSlot) {
        this.ghostSlot = ghostSlot;
    }

    @Override
    public Rect2i getArea() {
        return this.ghostSlot.getArea().asRect2i();
    }

    @Override
    public void accept(I ingredient) {
        if (this.ghostSlot.ingredientHandlingOverride(ingredient)) {
            return;
        }
        ingredient = this.ghostSlot.castGhostIngredientIfValid(ingredient);
        if (ingredient == null) {
            throw new IllegalStateException("Ghost slot did accept ingredient before, but now it doesn't.");
        }
        this.ghostSlot.setGhostIngredient(ingredient);
    }
}
