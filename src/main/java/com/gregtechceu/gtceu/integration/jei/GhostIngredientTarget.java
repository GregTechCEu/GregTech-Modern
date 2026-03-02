package com.gregtechceu.gtceu.integration.jei;

import com.gregtechceu.gtceu.integration.recipeviewer.handlers.GhostIngredientSlot;

import net.minecraft.client.renderer.Rect2i;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import org.jetbrains.annotations.NotNull;

public class GhostIngredientTarget<I> implements IGhostIngredientHandler.Target<I> {

    private final GhostIngredientSlot<I> ghostSlot;

    public GhostIngredientTarget(GhostIngredientSlot<I> ghostSlot) {
        this.ghostSlot = ghostSlot;
    }

    @Override
    public @NotNull Rect2i getArea() {
        return this.ghostSlot.getArea().asRect2i();
    }

    @Override
    public void accept(@NotNull I ingredient) {
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
