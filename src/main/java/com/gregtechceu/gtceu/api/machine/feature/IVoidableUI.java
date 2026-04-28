package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable.VoidingMode;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;

import net.minecraft.network.chat.Component;

import java.util.List;

public final class IVoidableUI {

    private IVoidableUI() {}

    public static void attachConfigurators(Object configuratorPanelObject, IVoidable controller) {
        if (!(configuratorPanelObject instanceof ConfiguratorPanel configuratorPanel)) {
            return;
        }
        configuratorPanel.attachConfigurators(new VoidableModeConfigurator(controller));
    }

    private static final class VoidableModeConfigurator implements IFancyConfiguratorButton {

        private final IVoidable controller;

        private VoidableModeConfigurator(IVoidable controller) {
            this.controller = controller;
        }

        @Override
        public IGuiTexture getIcon() {
            return GuiTextures.BUTTON_VOID_MULTIBLOCK.getSubTexture(0, controller.getVoidingMode().ordinal() * 0.25, 1,
                    0.25);
        }

        @Override
        public List<Component> getTooltips() {
            return List.of(Component.translatable("gtceu.gui.multiblock.voiding_mode"),
                    Component.translatable(controller.getVoidingMode().getTooltipKey()));
        }

        @Override
        public void onClick(ClickData clickData) {
            var values = VoidingMode.VALUES;
            controller.setVoidingMode(values[(controller.getVoidingMode().ordinal() + 1) % values.length]);
        }
    }
}
