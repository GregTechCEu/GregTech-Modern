package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.DrawableStack;
import com.gregtechceu.gtceu.api.mui.drawable.DynamicDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.ItemDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.*;
import com.gregtechceu.gtceu.api.mui.widgets.*;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Grid;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.mojang.blaze3d.platform.InputConstants;

public class GTMuiWidgets {

    public static Flow createTitleBar(MachineDefinition definition, int panelWidth) {
        var displayItem = definition.asStack();
        String hatchName = displayItem.getHoverName().getString();
        hatchName = hatchName.replaceAll("§.", "").trim();

        int borderRadius = 5;
        int iconSize = 16;
        int minPanelWidth = (int) (panelWidth * 0.9f) - (iconSize + (borderRadius * 2));
        int textTitleWidth = TextRenderer.getFont().width(hatchName);

        int textRows = (int) Math.ceil((double) textTitleWidth / minPanelWidth);
        int textHeightPerRow = (int) (IKey.renderer.getFontHeight());
        int textHeight = textHeightPerRow * textRows + borderRadius;

        int rowWidth = Math.min((int) (0.9 * panelWidth), (iconSize + (borderRadius * 4) + textTitleWidth));

        return new Row()
                .coverChildrenHeight()
                .mainAxisAlignment(Alignment.MainAxis.CENTER)
                .width(rowWidth)
                .top(-(textHeight + borderRadius))
                .rightRel(0.45f)
                .background(GTGuiTextures.BACKGROUND)
                .child(new ItemDrawable(displayItem)
                        .asIcon().size(iconSize)
                        .asWidget()
                        .marginLeft(borderRadius))
                .mainAxisAlignment(Alignment.MainAxis.START)
                .child(IKey.str(hatchName)
                        .asWidget()
                        .paddingTop(1)
                        .margin(borderRadius, borderRadius, borderRadius, 1)
                        .size(Math.min(minPanelWidth, textTitleWidth), textHeight));
    }

    public static ToggleButton createPowerButton(IRecipeLogicMachine recipeLogicMachine, PanelSyncManager syncManager) {
        BooleanSyncValue power = new BooleanSyncValue(() -> recipeLogicMachine.getRecipeLogic().isWorkingEnabled(),
                recipeLogicMachine::setWorkingEnabled);
        syncManager.syncValue("working_enabled", power);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(power::getBoolValue, power::setBoolValue))
                .selectedBackground(GTGuiTextures.BUTTON_POWER[1])
                .background(GTGuiTextures.BUTTON_POWER[0])
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable(
                        recipeLogicMachine.getRecipeLogic().isWorkingEnabled() ? "behaviour.soft_hammer.enabled" :
                                "behaviour.soft_hammer.disabled"))));
    }

    public static ProgressWidget createProgressBar(WorkableTieredMachine workableMachine, UITexture texture, int size) {
        return new ProgressWidget()
                .texture(texture, size)
                .progress(() -> workableMachine.getProgress() / (double) workableMachine.getMaxProgress());
    }

    public static ItemSlot createBatterySlot(SimpleTieredMachine tieredMachine, PanelSyncManager syncManager) {
        ItemSlotSH battery = new ItemSlotSH(new ModularSlot(tieredMachine.getChargerInventory(), 0));
        syncManager.syncValue("battery", battery);
        return new ItemSlot().syncHandler("battery").background(GTGuiTextures.SLOT, GTGuiTextures.CHARGER_OVERLAY);
    }

    public static ToggleButton createAutoOutputItemButton(SimpleTieredMachine machine, PanelSyncManager syncManager) {
        BooleanSyncValue itemOutputs = new BooleanSyncValue(machine::isAutoOutputItems,
                machine::setAutoOutputItems);
        syncManager.syncValue("auto_output_items", itemOutputs);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(itemOutputs::getBoolValue, itemOutputs::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable("gtceu.gui.item_auto_output",
                        Component.translatable(itemOutputs.getBoolValue() ? "cover.voiding.label.enabled" :
                                "cover.voiding.label.disabled")))));
    }

    public static ToggleButton createAutoOutputFluidButton(SimpleTieredMachine machine, PanelSyncManager syncManager) {
        BooleanSyncValue fluidOutputs = new BooleanSyncValue(machine::isAutoOutputFluids,
                machine::setAutoOutputFluids);
        syncManager.syncValue("auto_output_fluids", fluidOutputs);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(fluidOutputs::getBoolValue, fluidOutputs::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable("gtceu.gui.fluid_auto_output",
                        Component.translatable(fluidOutputs.getBoolValue() ? "cover.voiding.label.enabled" :
                                "cover.voiding.label.disabled")))));
    }

    public static ToggleButton createInputFromOutputItem(SimpleTieredMachine machine, PanelSyncManager syncManager) {
        BooleanSyncValue inputFromOutputItem = new BooleanSyncValue(machine::isAllowInputFromOutputSideItems,
                machine::setAllowInputFromOutputSideItems);
        syncManager.syncValue("input_from_output_item", inputFromOutputItem);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(inputFromOutputItem::getBoolValue, inputFromOutputItem::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable("gtceu.gui.item_input_from_output",
                        Component.translatable(inputFromOutputItem.getBoolValue() ? "cover.voiding.label.enabled" :
                                "cover.voiding.label.disabled")))));
    }

    public static ToggleButton createInputFromOutputFluid(SimpleTieredMachine machine, PanelSyncManager syncManager) {
        BooleanSyncValue inputFromOutputFluid = new BooleanSyncValue(machine::isAllowInputFromOutputSideFluids,
                machine::setAllowInputFromOutputSideFluids);
        syncManager.syncValue("input_from_output_fluid", inputFromOutputFluid);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(inputFromOutputFluid::getBoolValue, inputFromOutputFluid::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable("gtceu.gui.fluid_input_from_output",
                        Component.translatable(inputFromOutputFluid.getBoolValue() ? "cover.voiding.label.enabled" :
                                "cover.voiding.label.disabled")))));
    }

    public static ButtonWidget<?> createCircuitSlotPanel(SimpleTieredMachine machine, ModularPanel parentPanel,
                                                         PanelSyncManager syncManager) {
        IntSyncValue circuitSyncValue = new IntSyncValue(() -> {
            if (machine.getCircuitInventory().getStackInSlot(0).isEmpty()) return -1;
            return IntCircuitBehaviour.getCircuitConfiguration(machine.getCircuitInventory().getStackInSlot(0));
        },
                (v) -> machine.getCircuitInventory().setStackInSlot(0,
                        (v < 0 ? ItemStack.EMPTY : IntCircuitBehaviour.stack(v))));
        syncManager.syncValue("circuit_slot", circuitSyncValue);

        Grid buttonGrid = new Grid()
                .coverChildren()
                .mapTo(8, 32, i -> new ToggleButton()
                        .size(18)
                        .padding(1)
                        .overlay(new ItemDrawable().setItem(IntCircuitBehaviour.stack(i + 1)))
                        .value(new BoolValue.Dynamic(() -> (i + 1) == circuitSyncValue.getIntValue(),
                                (v) -> {
                                    if (v) circuitSyncValue.setValue(i + 1);
                                })));

        ModularPanel circuitPanel = new Dialog<>("circuit_panel")
                .setDisablePanelsBelow(false)
                .setDraggable(true)
                .setCloseOnOutOfBoundsClick(true)
                .relative(parentPanel)
                .leftRelOffset(0.0f, -180)
                .height(105)
                .child(new Column()
                        .padding(2)
                        .fullHeight()
                        .coverChildren()
                        .childPadding(7)
                        .top(3)
                        .alignX(Alignment.Center)
                        .child(IKey.lang("item.gtceu.circuit.integrated.gui").asWidget())
                        .child(buttonGrid));

        IPanelHandler circuitPanelHandler = syncManager.panel("circuit_panel",
                (sm, sh) -> circuitPanel, true);

        return new ButtonWidget<>()
                .size(18)
                .onMousePressed((x, y, b) -> {
                    if (b == InputConstants.MOUSE_BUTTON_LEFT || b == InputConstants.MOUSE_BUTTON_RIGHT) {
                        circuitPanelHandler.openPanel();
                    } else if (b == InputConstants.MOUSE_BUTTON_MIDDLE) {
                        circuitSyncValue.setValue(0);
                    }
                    return true;
                })
                .onMouseScrolled((x, y, delta) -> {
                    int newValue = nextCircuitValue(machine.getCircuitInventory().getStackInSlot(0),
                            circuitSyncValue.getIntValue(), delta);
                    circuitSyncValue.setValue(newValue);
                    return true;
                })
                .overlay(new DynamicDrawable(() -> {
                    if (machine.getCircuitInventory().getStackInSlot(0).isEmpty()) {
                        return new DrawableStack(new ItemDrawable(IntCircuitBehaviour.stack(0)),
                                new ItemDrawable(Items.BARRIER)).asIcon().size(16);
                    }
                    return new ItemDrawable(machine.getCircuitInventory().getStackInSlot(0))
                            .asIcon().size(16);
                }))
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable("metaitem.int_circuit.configuration",
                        (machine.getCircuitInventory().getStackInSlot(0).isEmpty() ? 0 :
                                IntCircuitBehaviour
                                        .getCircuitConfiguration(machine.getCircuitInventory().getStackInSlot(0)))))));
    }

    private static int nextCircuitValue(ItemStack stack, int current, double delta) {
        if (delta > 0) {
            if (current == IntCircuitBehaviour.CIRCUIT_MAX) {
                // if at max, loop around to no circuit
                return 0;
            } else if (stack.isEmpty()) {
                // if at no circuit, skip 0 and return 1
                return 1;
            } else {
                // normal case: increment by 1
                return current + 1;
            }
        } else {
            if (stack.isEmpty() ||
                    (current == 0 && !ConfigHolder.INSTANCE.machines.ghostCircuit)) {
                // if at no circuit, loop around to max
                return IntCircuitBehaviour.CIRCUIT_MAX;
            } else if (current == 1 && ConfigHolder.INSTANCE.machines.ghostCircuit) {
                // if at 1, skip 0 and return no circuit
                return -1;
            } else {
                // normal case: decrement by 1
                return current - 1;
            }
        }
    }

    public static IDrawable.DrawableWidget createGTLogo() {
        return new IDrawable.DrawableWidget(GTGuiTextures.GREGTECH_LOGO);
    }
}
