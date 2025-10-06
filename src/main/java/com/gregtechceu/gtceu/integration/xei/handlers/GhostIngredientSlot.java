package com.gregtechceu.gtceu.integration.xei.handlers;

import com.gregtechceu.gtceu.api.mui.base.XeiSettings;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiElement;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.Rectangle;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for compat with recipe viewers' ghost slots.
 * Implement this on any {@link IWidget}.
 * This slot must then be manually registered in something like {@link Widget#onInit()}
 * with {@link XeiSettings#addGhostIngredientSlot(IWidget)}
 *
 * @param <I> type of the ingredient
 */
public interface GhostIngredientSlot<I> extends IGuiElement {

    /**
     * Puts the ingredient in this ghost slot.
     * Was cast with {@link #castGhostIngredientIfValid(Object)}.
     *
     * @param ingredient ingredient to put
     */
    void setGhostIngredient(@NotNull I ingredient);

    /**
     * Tries to cast an ingredient to the type of this slot.
     * Returns null if the ingredient can't be cast.
     * Must be consistent.
     *
     * @param ingredient ingredient to cast
     * @return cast ingredient or null
     */
    @Nullable
    I castGhostIngredientIfValid(@NotNull Object ingredient);

    /**
     * @return the class of the ingredient this slot expects
     */
    Class<I> ingredientClass();

    /**
     * A way to handle recipeviewer-specific ingredient instances.
     *
     * @return {@code true} if handling the ingredient yourself.
     */
    default boolean ingredientHandlingOverride(Object ingredient) {
        return false;
    }

    default void drawHighlight(GuiContext context, Rectangle area, boolean hovering) {
        int color = hovering ? Color.argb(76, 201, 25, 128) : Color.argb(19, 201, 10, 64);
        GuiDraw.drawRect(context.getGraphics(), 0, 0, area.width, area.height, color);
    }
}
