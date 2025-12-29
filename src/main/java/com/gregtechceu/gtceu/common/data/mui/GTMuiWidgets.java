package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
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
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.*;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Grid;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GTMuiWidgets {

    public static Flow createTitleBar(MachineDefinition definition, int panelWidth) {
        return createTitleBar(definition, panelWidth, GTGuiTextures.BACKGROUND);
    }

    public static Flow createTitleBar(MachineDefinition definition, int panelWidth, UITexture background) {
        var displayItem = definition.asStack();
        String hatchName = displayItem.getHoverName().getString();
        hatchName = hatchName.replaceAll("§.", "").trim();

        int borderRadius = 5;
        int iconSize = 16;
        int minPanelWidth = (int) (panelWidth * 0.9f) - (iconSize + (borderRadius * 3));
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
                .background(background.getSubArea(0f, 0f, 1.0f, 0.75f))
                .child(new ItemDrawable(displayItem)
                        .asIcon().size(iconSize)
                        .asWidget()
                        .marginLeft(borderRadius))
                .mainAxisAlignment(Alignment.MainAxis.START)
                .child(IKey.str(hatchName)
                        .alignment(Alignment.CENTER)
                        .asWidget()
                        .paddingTop(1)
                        .margin(borderRadius, borderRadius, borderRadius, 1)
                        .size(Math.min(minPanelWidth, textTitleWidth), textHeight));
    }

    public static ToggleButton createPowerButton(BooleanSupplier getter, BooleanConsumer setter,
                                                 PanelSyncManager syncManager) {
        BooleanSyncValue power = new BooleanSyncValue(getter, setter);
        syncManager.syncValue("working_enabled", power);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(power::getBoolValue, power::setBoolValue))
                .selectedBackground(GTGuiTextures.BUTTON_POWER[1])
                .background(GTGuiTextures.BUTTON_POWER[0])
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable(
                        power.getBoolValue() ? "behaviour.soft_hammer.enabled" :
                                "behaviour.soft_hammer.disabled"))));
    }

    public static ToggleButton createPowerButton(IRecipeLogicMachine recipeLogicMachine, PanelSyncManager syncManager) {
        return createPowerButton(
                () -> recipeLogicMachine.getRecipeLogic().isWorkingEnabled(),
                recipeLogicMachine::setWorkingEnabled,
                syncManager);
    }

    public static ProgressWidget createProgressBar(IRecipeLogicMachine workableMachine, UITexture texture, int size) {
        return new ProgressWidget()
                .texture(texture, size)
                .progress(() -> workableMachine.getProgress() / (double) workableMachine.getMaxProgress());
    }

    public static ItemSlot createBatterySlot(SimpleTieredMachine tieredMachine, PanelSyncManager syncManager) {
        ItemSlotSH battery = new ItemSlotSH(new ModularSlot(tieredMachine.getChargerInventory(), 0));
        syncManager.syncValue("battery", battery);
        return new ItemSlot().syncHandler("battery").background(GTGuiTextures.SLOT, GTGuiTextures.CHARGER_OVERLAY);
    }

    public static ItemSlot createBatterySlot(IItemHandler itemHandler, int slot, PanelSyncManager syncManager) {
        ItemSlotSH battery = new ItemSlotSH(new ModularSlot(itemHandler, slot));
        syncManager.syncValue("battery", battery);
        return new ItemSlot().syncHandler("battery").background(GTGuiTextures.SLOT, GTGuiTextures.CHARGER_OVERLAY);
    }

    public static ToggleButton createAutoOutputItemButton(IAutoOutputItem machine, PanelSyncManager syncManager) {
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

    private static IntSyncValue createCircuitSlotSyncValue(Consumer<ItemStack> circuitSetter,
                                                           Supplier<ItemStack> circuitGetter) {
        return new IntSyncValue(() -> {
            if (circuitGetter.get().isEmpty()) return -1;
            return IntCircuitBehaviour.getCircuitConfiguration(circuitGetter.get());
        },
                (v) -> circuitSetter.accept(v < 0 ? ItemStack.EMPTY : IntCircuitBehaviour.stack(v)));
    }

    public static ModularPanel createCircuitSlotPanel(IntSyncValue circuitSyncValue, PanelSyncManager syncManager) {
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

        return new Dialog<>("circuit_panel")
                .setDisablePanelsBelow(false)
                .setDraggable(true)
                .setCloseOnOutOfBoundsClick(true)
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
    }

    public static ModularPanel createCircuitSlotPanel(Consumer<ItemStack> circuitSetter,
                                                      Supplier<ItemStack> circuitGetter, PanelSyncManager syncManager) {
        IntSyncValue circuitSyncValue = createCircuitSlotSyncValue(circuitSetter, circuitGetter);
        return createCircuitSlotPanel(circuitSyncValue, syncManager);
    }

    public static ButtonWidget<?> createCircuitSlotPanel(IHasCircuitSlot machine, ModularPanel parentPanel,
                                                         PanelSyncManager syncManager) {
        IntSyncValue circuitSyncValue = createCircuitSlotSyncValue(
                i -> machine.getCircuitInventory().setStackInSlot(0, i),
                () -> machine.getCircuitInventory().getStackInSlot(0));
        ModularPanel circuitPanel = createCircuitSlotPanel(
                circuitSyncValue,
                syncManager)
                .relative(parentPanel)
                .leftRelOffset(0.0f, -180);
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

    public static String[] createGrid(int amount, int rowSize, boolean output, char key) {
        int rows = (int) Math.ceil((float) amount / rowSize);
        String[] grid = new String[rows];
        for (int i = 0; i < rows; i++) {
            StringBuilder r = new StringBuilder();
            if (output) {
                for (int j = 0; j < rowSize; j++) {
                    if ((i * rowSize + j) > (amount - 1)) {
                        r.insert(0, " ");
                    } else {
                        r.insert(0, key);
                    }
                }
            } else {
                for (int j = 0; j < rowSize; j++) {
                    if ((i * rowSize + j) > (amount - 1)) {
                        r.append(" ");
                    } else {
                        r.append(key);
                    }
                }
            }
            grid[i] = r.toString();
        }

        return grid;
    }

    public static ParentWidget<?> createXEIWidget(GTRecipeTypeUILayout layout) {
        return new ParentWidget<>();
    }
}
