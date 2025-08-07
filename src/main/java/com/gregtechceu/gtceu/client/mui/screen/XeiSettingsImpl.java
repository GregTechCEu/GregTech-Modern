package com.gregtechceu.gtceu.client.mui.screen;

import com.gregtechceu.gtceu.api.mui.base.XeiSettings;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.utils.Rectangle;
import com.gregtechceu.gtceu.integration.xei.XeiState;
import com.gregtechceu.gtceu.integration.xei.handlers.GhostIngredientSlot;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Keeps track of everything related to JEI in a Modular GUI.
 * By default, JEI is disabled in client only GUIs.
 * This class can be safely interacted with even when JEI/HEI is not installed.
 */
@OnlyIn(Dist.CLIENT)
public class XeiSettingsImpl implements XeiSettings {

    private XeiState xeiState = XeiState.DEFAULT;
    private final List<IWidget> jeiExclusionWidgets = new ArrayList<>();
    private final List<Rectangle> jeiExclusionAreas = new ArrayList<>();
    private final List<GhostIngredientSlot<?>> ghostIngredientSlots = new ArrayList<>();

    /**
     * Force JEI to be enabled
     */
    @Override
    public void forceEnabled() {
        this.xeiState = XeiState.ENABLED;
    }

    /**
     * Force JEI to be disabled
     */
    @Override
    public void forceDisabled() {
        this.xeiState = XeiState.DISABLED;
    }

    /**
     * Only enabled JEI in synced GUIs
     */
    @Override
    public void defaultXei() {
        this.xeiState = XeiState.DEFAULT;
    }

    /**
     * Checks if JEI is enabled for a given screen
     *
     * @param screen modular screen
     * @return true if jei is enabled
     */
    @Override
    public boolean isEnabled(ModularScreen screen) {
        return this.xeiState.test(screen);
    }

    /**
     * Adds an exclusion zone. JEI will always try to avoid exclusion zones. <br>
     * <b>If a widgets wishes to have an exclusion zone it should use {@link #addExclusionArea(IWidget)}!</b>
     *
     * @param area exclusion area
     */
    @Override
    public void addExclusionArea(Rectangle area) {
        if (!this.jeiExclusionAreas.contains(area)) {
            this.jeiExclusionAreas.add(area);
        }
    }

    /**
     * Removes an exclusion zone.
     *
     * @param area exclusion area to remove (must be the same instance)
     */
    @Override
    public void removeExclusionArea(Rectangle area) {
        this.jeiExclusionAreas.remove(area);
    }

    /**
     * Adds an exclusion zone of a widget. JEI will always try to avoid exclusion zones. <br>
     * Useful when a widget is outside its panel.
     *
     * @param area widget
     */
    @Override
    public void addExclusionArea(IWidget area) {
        if (!this.jeiExclusionWidgets.contains(area)) {
            this.jeiExclusionWidgets.add(area);
        }
    }

    /**
     * Removes a widget exclusion area.
     *
     * @param area widget
     */
    @Override
    public void removeExclusionArea(IWidget area) {
        this.jeiExclusionWidgets.remove(area);
    }

    /**
     * Adds a JEI ghost slots. Ghost slots can display an ingredient, but the ingredient does not really exist.
     * By calling this method users will be able to drag ingredients from JEI into the slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    @Override
    public <W extends IWidget & GhostIngredientSlot<?>> void addGhostIngredientSlot(W slot) {
        if (!this.ghostIngredientSlots.contains(slot)) {
            this.ghostIngredientSlots.add(slot);
        }
    }

    /**
     * Removes a JEI ghost slot.
     *
     * @param slot slot widget
     * @param <W>  slot widget type
     */
    @Override
    public <W extends IWidget & GhostIngredientSlot<?>> void removeGhostIngredientSlot(W slot) {
        this.ghostIngredientSlots.remove(slot);
    }

    @UnmodifiableView
    public List<Rectangle> getExclusionAreas() {
        return Collections.unmodifiableList(this.jeiExclusionAreas);
    }

    @UnmodifiableView
    public List<IWidget> getExclusionWidgets() {
        return Collections.unmodifiableList(this.jeiExclusionWidgets);
    }

    @UnmodifiableView
    public List<GhostIngredientSlot<?>> getGhostIngredientSlots() {
        return Collections.unmodifiableList(this.ghostIngredientSlots);
    }

    @ApiStatus.Internal
    public List<Rectangle> getAllExclusionAreas() {
        this.jeiExclusionWidgets.removeIf(widget -> !widget.isValid());
        List<Rectangle> areas = new ArrayList<>(this.jeiExclusionAreas);
        for (Iterator<IWidget> iterator = this.jeiExclusionWidgets.iterator(); iterator.hasNext();) {
            IWidget widget = iterator.next();
            if (!widget.isValid()) {
                iterator.remove();
                continue;
            }
            if (widget.isEnabled()) {
                areas.add(widget.getArea());
            }
        }
        return areas;
    }
}
