package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.gui.editor.EditableUI;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.gui.widget.GhostCircuitSlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import com.google.common.collect.Tables;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

final class SimpleTieredMachineUI {

    private SimpleTieredMachineUI() {}

    static void attachConfigurators(SimpleTieredMachine machine, ConfiguratorPanel configuratorPanel) {
        if (machine.autoOutput.supportsAutoOutputFluids()) {
            configuratorPanel.attachConfigurators(createAutoOutputFluidConfigurator(machine));
        }
        if (machine.autoOutput.supportsAutoOutputItems()) {
            configuratorPanel.attachConfigurators(createAutoOutputItemConfigurator(machine));
        }

        if (machine.isCircuitSlotEnabled()) {
            configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(machine.circuitInventory.storage));
        }
    }

    private static IFancyConfigurator createAutoOutputFluidConfigurator(SimpleTieredMachine machine) {
        return createAutoOutputConfigurator(
                GuiTextures.IO_CONFIG_FLUID_MODES_BUTTON,
                "gtceu.gui.fluid_auto_output",
                machine.autoOutput::isAutoOutputFluids,
                (cd, nextState) -> machine.autoOutput.setAllowAutoOutputFluids(nextState));
    }

    private static IFancyConfigurator createAutoOutputItemConfigurator(SimpleTieredMachine machine) {
        return createAutoOutputConfigurator(
                GuiTextures.IO_CONFIG_ITEM_MODES_BUTTON,
                "gtceu.gui.item_auto_output",
                machine.autoOutput::isAutoOutputItems,
                (cd, nextState) -> machine.autoOutput.setAllowAutoOutputItems(nextState));
    }

    private static IFancyConfigurator createAutoOutputConfigurator(ResourceTexture modesButtonTexture,
                                                                   String tooltipBaseLangKey,
                                                                   BooleanSupplier stateSupplier,
                                                                   BiConsumer<ClickData, Boolean> onToggle) {
        var toggle = new IFancyConfiguratorButton.Toggle(
                new GuiTextureGroup(
                        GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0, 1, 0.5),
                        modesButtonTexture.getSubTexture(0, 1 / 3f, 1, 1 / 3f)),
                new GuiTextureGroup(
                        GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0.5, 1, 0.5),
                        modesButtonTexture.getSubTexture(0, 2 / 3f, 1, 1 / 3f)),
                stateSupplier,
                onToggle);

        toggle.setTooltipsSupplier(enabled -> {
            var key = tooltipBaseLangKey + '.' + (enabled ? "enabled" : "disabled");
            return List.of(Component.translatable(key));
        });

        return toggle;
    }

    static EditableMachineUI createEditableUI(Identifier path, GTRecipeType recipeType) {
        return new EditableMachineUI("simple", path, () -> {
            WidgetGroup template = recipeType.getRecipeUI().createEditableUITemplate(false, false).createDefault();
            SlotWidget batterySlot = createBatterySlot().createDefault();
            WidgetGroup group = new WidgetGroup(0, 0, template.getSize().width,
                    Math.max(template.getSize().height, 78));
            template.setSelfPosition(new Position(0, (group.getSize().height - template.getSize().height) / 2));
            batterySlot.setSelfPosition(new Position(group.getSize().width / 2 - 9, group.getSize().height - 18));
            group.addWidget(batterySlot);
            group.addWidget(template);

            // TODO fix this.
            // if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
            // SlotWidget circuitSlot = createCircuitConfigurator().createDefault();
            // circuitSlot.setSelfPosition(new Position(120, 62));
            // group.addWidget(circuitSlot);
            // }

            return group;
        }, (template, machine) -> {
            if (machine instanceof SimpleTieredMachine tieredMachine) {
                var storages = Tables.newCustomTable(new EnumMap<>(IO.class),
                        LinkedHashMap<RecipeCapability<?>, Object>::new);
                storages.put(IO.IN, ItemRecipeCapability.CAP, tieredMachine.importItems.storage);
                storages.put(IO.OUT, ItemRecipeCapability.CAP, tieredMachine.exportItems.storage);
                storages.put(IO.IN, FluidRecipeCapability.CAP, tieredMachine.importFluids);
                storages.put(IO.OUT, FluidRecipeCapability.CAP, tieredMachine.exportFluids);
                storages.put(IO.IN, CWURecipeCapability.CAP, tieredMachine.importComputation);
                storages.put(IO.OUT, CWURecipeCapability.CAP, tieredMachine.exportComputation);

                tieredMachine.getRecipeType().getRecipeUI().createEditableUITemplate(false, false).setupUI(template,
                        new GTRecipeTypeUI.RecipeHolder(tieredMachine.recipeLogic::getProgressPercent,
                                storages,
                                new CompoundTag(),
                                Collections.emptyList(),
                                false, false));
                createBatterySlot().setupUI(template, tieredMachine);
                // createCircuitConfigurator().setupUI(template, tieredMachine);
            }
        });
    }

    /**
     * Create a battery slot widget.
     */
    static EditableUI<SlotWidget, SimpleTieredMachine> createBatterySlot() {
        return new EditableUI<>("battery_slot", SlotWidget.class, () -> {
            var slotWidget = new SlotWidget();
            slotWidget.setBackground(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY);
            return slotWidget;
        }, (slotWidget, machine) -> {
            slotWidget.setHandlerSlot(machine.chargerInventory, 0);
            slotWidget.setCanPutItems(true);
            slotWidget.setCanTakeItems(true);
            slotWidget.setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.charger_slot.tooltip",
                    GTValues.VNF[machine.getTier()], GTValues.VNF[machine.getTier()]).toArray(Component[]::new));
        });
    }

    /**
     * Create a ghost circuit slot widget.
     */
    static EditableUI<GhostCircuitSlotWidget, SimpleTieredMachine> createCircuitConfigurator() {
        return new EditableUI<>("circuit_configurator", GhostCircuitSlotWidget.class, () -> {
            var slotWidget = new GhostCircuitSlotWidget();
            slotWidget.setBackground(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY);
            return slotWidget;
        }, (slotWidget, machine) -> {
            slotWidget.setCircuitInventory(machine.circuitInventory);
            slotWidget.setCanPutItems(false);
            slotWidget.setCanTakeItems(false);
            slotWidget.setHoverTooltips(
                    LangHandler.getMultiLang("gtceu.gui.configurator_slot.tooltip").toArray(Component[]::new));
        });
    }
}
