package com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;

import appeng.api.stacks.GenericStack;

import java.util.function.Predicate;

public interface IAutoPullPart extends IMultiPart {

    boolean isAutoPull();

    void setAutoPull(boolean autoPull);

    void setAutoPullTest(Predicate<GenericStack> test);

    /*
     * @Override
     * default void attachConfigurators(ConfiguratorPanel configuratorPanel) {
     * configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
     * GuiTextures.BUTTON_AUTO_PULL.getSubTexture(0, 0, 1, 0.5),
     * GuiTextures.BUTTON_AUTO_PULL.getSubTexture(0, 0.5, 1, 0.5),
     * this::isAutoPull,
     * (clickData, pressed) -> setAutoPull(pressed))
     * .setTooltipsSupplier(pressed -> List.of(Component.translatable("gtceu.gui.me_bus.auto_pull_button"))));
     * }
     */
}
