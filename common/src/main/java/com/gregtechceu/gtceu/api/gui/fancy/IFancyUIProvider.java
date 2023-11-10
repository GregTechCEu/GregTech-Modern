package com.gregtechceu.gtceu.api.gui.fancy;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/6/27
 * @implNote IFancyUIProvider
 */
public interface IFancyUIProvider {

    /**
     * Create the main page ui.
     */
    Widget createMainPage();

    /**
     * Get the tab icon of this page.
     */
    IGuiTexture getTabIcon();


    /**
     * Attach configurators to the left panel.
     */
    void attachConfigurators(ConfiguratorPanel configuratorPanel);

    /**
     * Attach tooltips to the right panel
     */
    void attachTooltips(TooltipsPanel tooltipsPanel);

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
     * Set up the top tabs. Only if it's a main page will it be called.
     */
    default void setupTabs(TabsWidget tabsWidget) {
        tabsWidget.setMainTab(this);
        getSubTabs().forEach(tabsWidget::attachSubTab);
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
}
