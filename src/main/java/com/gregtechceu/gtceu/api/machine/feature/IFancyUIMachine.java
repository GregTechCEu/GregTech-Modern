package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.gui.fancy.*;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public interface IFancyUIMachine extends IUIMachine, IFancyUIProvider {

    @Override
    default ModularUI createUI(Player entityPlayer) {
        return FancyUIMachineUI.createUI(this, entityPlayer);
    }

    /**
     * We should not override this method in general, and use {@link IFancyUIMachine#createUIWidget()} instead,
     */
    @Override
    default Widget createMainPage(FancyMachineUIWidget widget) {
        return FancyUIMachineUI.createMainPage(this, widget);
    }

    /**
     * Create the core widget of this machine.
     */
    default Widget createUIWidget() {
        return FancyUIMachineUI.createDefaultWidget(this);
    }

    @Override
    default IGuiTexture getTabIcon() {
        return FancyUIMachineUI.getTabIcon(this);
    }

    @Override
    default void attachSideTabs(TabsWidget sideTabs) {
        FancyUIMachineUI.attachSideTabs(this, sideTabs);
    }

    @Override
    default void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        FancyUIMachineUI.attachConfigurators(this, configuratorPanel);
    }

    @Override
    default void attachTooltips(TooltipsPanel tooltipsPanel) {
        FancyUIMachineUI.attachTooltips(this, tooltipsPanel);
    }

    @Override
    default List<Component> getTabTooltips() {
        var list = new ArrayList<Component>();
        list.add(Component.translatable(self().getDefinition().getDescriptionId()));
        return list;
    }

    @Override
    default Component getTitle() {
        return Component.translatable(self().getDefinition().getDescriptionId());
    }
}
