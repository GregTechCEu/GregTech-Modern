package com.gregtechceu.gtceu.api.mui.base;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.utils.Rectangle;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.integration.xei.handlers.GhostIngredientSlot;

import org.jetbrains.annotations.ApiStatus;

/**
 * Keeps track of everything related to JEI in a Modular GUI.
 * By default, JEI is disabled in client only GUIs.
 * This class can be safely interacted with even when JEI/HEI is not installed.
 */
@ApiStatus.NonExtendable
public interface XeiSettings {

    /**
     * Force XEI to be enabled
     */
    void forceEnabled();

    /**
     * Force XEI to be disabled
     */
    void forceDisabled();

    /**
     * Only enabled XEI in synced GUIs
     */
    void defaultXei();

    /**
     * Checks if XEI is enabled for a given screen
     *
     * @param screen modular screen
     * @return true if xei is enabled
     */
    boolean isEnabled(ModularScreen screen);

    /**
     * Adds an exclusion zone. XEI will always try to avoid exclusion zones. <br>
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
     * Adds an exclusion zone of a widget. XEI will always try to avoid exclusion zones. <br>
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
     * Adds a XEI ghost slot. Ghost slots can display an ingredient, but the ingredient does not really exist.
     * By calling this method users will be able to drag ingredients from JEI into the slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    <W extends IWidget & GhostIngredientSlot<?>> void addGhostIngredientSlot(W slot);

    /**
     * Removes a XEI ghost slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    <W extends IWidget & GhostIngredientSlot<?>> void removeGhostIngredientSlot(W slot);

    XeiSettings DUMMY = new XeiSettings() {

        @Override
        public void forceEnabled() {}

        @Override
        public void forceDisabled() {}

        @Override
        public void defaultXei() {}

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
