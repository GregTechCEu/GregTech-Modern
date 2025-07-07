package com.gregtechceu.gtceu.api.gui.fancy;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface IFancyUIProvider {

    /**
     * Create the main page ui.
     */
    Widget createMainPage(FancyMachineUIWidget widget);

    /**
     * Get the tab icon of this page.
     */
    IGuiTexture getTabIcon();

    /**
     * Get the title of this page.
     */
    Component getTitle();

    /**
     * Attach configurators to the left subtab list.
     */
    default void attachSideTabs(TabsWidget configuratorPanel) {}

    /**
     * Attach configurators to the left panel.
     */
    default void attachConfigurators(ConfiguratorPanel configuratorPanel) {}

    /**
     * Attach tooltips to the right panel
     */
    default void attachTooltips(TooltipsPanel tooltipsPanel) {}

    default boolean hasPlayerInventory() {
        return true;
    }

    /**
     * Get sub tabs, for example, multiblock will show all its parts tabs.
     */
    default List<IFancyUIProvider> getSubTabs() {
        return Collections.emptyList();
    }

    /**
     * Get tab's Tooltips
     */
    default List<Component> getTabTooltips() {
        return Collections.emptyList();
    }

    /**
     * Get tab's Tooltips Component
     */
    @Nullable
    default TooltipComponent getTabTooltipComponent() {
        return null;
    }

    @Nullable
    default PageGroupingData getPageGroupingData() {
        return null;
    }

    record PageGroupingData(@Nullable String groupKey, int groupPositionWeight) {}
}
