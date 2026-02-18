package com.gregtechceu.gtceu.api.mui.base;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.utils.Rectangle;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.integration.recipeviewer.handlers.GhostIngredientSlot;

import org.jetbrains.annotations.ApiStatus;

/**
 * Keeps track of everything related to JEI in a Modular GUI.
 * By default, JEI is disabled in client only GUIs.
 * This class can be safely interacted with even when JEI/HEI is not installed.
 */
@ApiStatus.NonExtendable
public interface RecipeViewerSettings {

    /**
     * Force recipe viewer to be enabled
     */
    void enable();

    /**
     * Force recipe viewer to be disabled
     */
    void disable();

    /**
     * Only enable the recipe viewer in synced GUIs
     */
    void defaultState();

    /**
     * Checks if the recipe viewer is enabled for a given screen
     *
     * @param screen modular screen
     * @return true if the recipe viewer is enabled
     */
    boolean isEnabled(ModularScreen screen);

    /**
     * Adds an exclusion zone. Recipe viewers will always try to avoid exclusion zones. <br>
     * <b>If a widgets wishes to have an exclusion zone it should use {@link #addExclusionArea(IWidget)}!</b>
     *
     * @param area exclusion area
     */
    void addExclusionArea(Rectangle area);

    /**
     * Removes an exclusion zone.
     *
     * @param area exclusion area to remove (must be the same instance)
     */
    void removeExclusionArea(Rectangle area);

    /**
     * Adds an exclusion zone of a widget. Recipe viewers will always try to avoid exclusion zones. <br>
     * Useful when a widget is outside its panel.
     *
     * @param area widget
     */
    void addExclusionArea(IWidget area);

    /**
     * Removes a widget exclusion area.
     *
     * @param area widget
     */
    void removeExclusionArea(IWidget area);

    /**
     * Adds a recipe viewer ghost slot. Ghost slots can display an ingredient, but the ingredient does not really exist.
     * By calling this method users will be able to drag ingredients from recipe viewers into the slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    <W extends IWidget & GhostIngredientSlot<?>> void addGhostIngredientSlot(W slot);

    /**
     * Removes a recipe viewer ghost slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    <W extends IWidget & GhostIngredientSlot<?>> void removeGhostIngredientSlot(W slot);

    RecipeViewerSettings DUMMY = new RecipeViewerSettings() {

        @Override
        public void enable() {}

        @Override
        public void disable() {}

        @Override
        public void defaultState() {}

        @Override
        public boolean isEnabled(ModularScreen screen) {
            return false;
        }

        @Override
        public void addExclusionArea(Rectangle area) {}

        @Override
        public void removeExclusionArea(Rectangle area) {}

        @Override
        public void addExclusionArea(IWidget area) {}

        @Override
        public void removeExclusionArea(IWidget area) {}

        @Override
        public <W extends IWidget & GhostIngredientSlot<?>> void addGhostIngredientSlot(W slot) {}

        @Override
        public <W extends IWidget & GhostIngredientSlot<?>> void removeGhostIngredientSlot(W slot) {}
    };
}
