package com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface IAutoPullPart extends IMultiPart {

    boolean isAutoPull();

    void setAutoPull(boolean autoPull);

    @Override
    default void addedToController(IMultiController controller) {}

    @Override
    default void removedFromController(IMultiController controller) {}

    @Override
    default void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON_AUTO_PULL.getSubTexture(0, 0, 1, 0.5),
                GuiTextures.BUTTON_AUTO_PULL.getSubTexture(0, 0.5, 1, 0.5),
                this::isAutoPull,
                (clickData, pressed) -> setAutoPull(pressed))
                .setTooltipsSupplier(pressed -> List.of(Component.translatable("gtceu.gui.me_bus.auto_pull_button"))));
    }
}
