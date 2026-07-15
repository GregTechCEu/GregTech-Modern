package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import net.minecraft.network.chat.Component;

import java.util.List;

/** Adds same-content slot protection controls to a GTM fancy machine UI. */
public interface IAllowSameUIProvider extends IFancyUIMachine {

    @Override
    default void attachConfigurators(ConfiguratorPanel left, ConfiguratorPanel right) {
        IFancyUIMachine.super.attachConfigurators(left, right);
        attachAllowSameConfigurators(right);
    }

    default void attachAllowSameConfigurators(ConfiguratorPanel right) {
        if (!(this instanceof MetaMachine machine)) return;

        for (var trait : machine.getTraits()) {
            if (!(trait instanceof IAllowSameContainer container)) continue;

            ButtonData button = getButtonData(trait);
            if (button == null) continue;

            right.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                    button.texture().getSubTexture(0, 0, 1, 0.5),
                    button.texture().getSubTexture(0, 0.5, 1, 0.5),
                    () -> !container.isAllowSame(),
                    (clickData, pressed) -> container.setAllowSame(!pressed))
                    .setTooltipsSupplier(pressed -> List.of(
                            Component.translatable(button.titleKey()).append(": ").append(Component.translatable(
                                    pressed ? "gtceu.gui.allow_same.enabled" : "gtceu.gui.allow_same.disabled")),
                            (pressed ? Component.translatable("gtceu.gui.allow_same.not_allowed") : Component.empty())
                                    .append(Component.translatable(button.tooltipKey())))));
        }
    }

    private static ButtonData getButtonData(Object trait) {
        if (trait instanceof NotifiableItemStackHandler handler && handler.getSlots() > 1 &&
                handler.capabilityIO != IO.NONE) {
            return switch (handler.capabilityIO) {
                case IN -> new ButtonData(GuiTextures.ALLOW_SAME_ITEM_INPUT,
                        "gtceu.gui.allow_same.item_input.title",
                        "gtceu.gui.allow_same.item_input.tooltip");
                case OUT -> new ButtonData(GuiTextures.ALLOW_SAME_ITEM_OUTPUT,
                        "gtceu.gui.allow_same.item_output.title",
                        "gtceu.gui.allow_same.item_output.tooltip");
                default -> null;
            };
        }
        if (trait instanceof NotifiableFluidTank tank && tank.getTanks() > 1 && tank.getCapabilityIO() != IO.NONE) {
            return switch (tank.getCapabilityIO()) {
                case IN -> new ButtonData(GuiTextures.ALLOW_SAME_FLUID_INPUT,
                        "gtceu.gui.allow_same.fluid_input.title",
                        "gtceu.gui.allow_same.fluid_input.tooltip");
                case OUT -> new ButtonData(GuiTextures.ALLOW_SAME_FLUID_OUTPUT,
                        "gtceu.gui.allow_same.fluid_output.title",
                        "gtceu.gui.allow_same.fluid_output.tooltip");
                default -> null;
            };
        }
        return null;
    }

    record ButtonData(com.lowdragmc.lowdraglib.gui.texture.ResourceTexture texture, String titleKey,
                      String tooltipKey) {}
}
