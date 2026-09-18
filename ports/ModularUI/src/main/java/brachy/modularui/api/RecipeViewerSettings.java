package brachy.modularui.api;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.recipeviewer.handlers.GhostIngredientSlot;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.utils.Rectangle;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

/**
 * Keeps track of everything related to recipe viewers in a Modular GUI.
 * By default, the recipe viewer is disabled in client only GUIs.
 * This class can be safely interacted with even when EMI/JEI/REI is not installed.
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

    @UnmodifiableView
    List<Rectangle> getExclusionAreas();

    @UnmodifiableView
    List<IWidget> getExclusionWidgets();

    @UnmodifiableView
    List<GhostIngredientSlot<?>> getGhostIngredientSlots();

    @ApiStatus.Internal
    List<Rectangle> getAllExclusionAreas();

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

        @Override
        public @UnmodifiableView List<Rectangle> getExclusionAreas() {
            return List.of();
        }

        @Override
        public @UnmodifiableView List<IWidget> getExclusionWidgets() {
            return List.of();
        }

        @Override
        public @UnmodifiableView List<GhostIngredientSlot<?>> getGhostIngredientSlots() {
            return List.of();
        }

        @Override
        public List<Rectangle> getAllExclusionAreas() {
            return List.of();
        }
    };
}
