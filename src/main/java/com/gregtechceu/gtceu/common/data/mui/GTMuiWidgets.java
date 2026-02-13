package com.gregtechceu.gtceu.common.data.mui;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.filter.Filter;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.DrawableStack;
import com.gregtechceu.gtceu.api.mui.drawable.DynamicDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.ItemDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.factory.SidedPosGuiData;
import com.gregtechceu.gtceu.api.mui.theme.ThemeAPI;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.MouseData;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.*;
import com.gregtechceu.gtceu.api.mui.widget.EmptyWidget;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.*;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Grid;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.util.function.*;

public class GTMuiWidgets {

    public static Flow createTitleBar(MachineDefinition definition, int panelWidth) {
        UITexture background = GTGuiTextures.BACKGROUND;
        if (!definition.getThemeId().equals(ThemeAPI.DEFAULT_ID)) {
            background = (UITexture) ThemeAPI.INSTANCE.getTheme(definition.getThemeId()).getPanelTheme().getTheme()
                    .getBackground();
        }
        if (background == null) {
            background = GTGuiTextures.BACKGROUND;
        }

        return createTitleBar(definition, panelWidth, background);
    }

    public static Flow createTitleBar(MachineDefinition definition, int panelWidth, UITexture background) {
        return createTitleBar(definition.asStack(), panelWidth, background);
    }

    public static Flow createTitleBar(ItemStack stack, int panelWidth, UITexture background) {
        String machineName = stack.getHoverName().getString();
        machineName = machineName.replaceAll("§.", "").trim();

        int borderRadius = 5;
        int iconSize = 16;
        int minPanelWidth = (int) (panelWidth * 0.9f) - (iconSize + (borderRadius * 3));
        int textTitleWidth = TextRenderer.getFont().width(machineName);

        int textRows = (int) Math.ceil((double) textTitleWidth / minPanelWidth);
        int textHeightPerRow = (int) (IKey.renderer.getFontHeight());
        int textHeight = textHeightPerRow * textRows + borderRadius;

        int rowWidth = Math.min((int) (0.9 * panelWidth), (iconSize + (borderRadius * 4) + textTitleWidth));

        return Flow.row()
                .coverChildrenHeight()
                .mainAxisAlignment(Alignment.MainAxis.CENTER)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .width(rowWidth)
                .top(-(textHeight + borderRadius))
                .horizontalCenter()
                .background(background.getSubArea(0f, 0f, 1.0f, 0.75f))
                .child(new ItemDrawable(stack)
                        .asIcon().size(iconSize)
                        .asWidget()
                        .marginLeft(borderRadius))
                .child(IKey.str(machineName)
                        .asWidget()
                        .margin(borderRadius, borderRadius, borderRadius, 1)
                        .size(Math.min(minPanelWidth, textTitleWidth), textHeight));
    }

    public static ToggleButton createPowerButton(BooleanSupplier getter, BooleanConsumer setter,
                                                 PanelSyncManager syncManager) {
        BooleanSyncValue power = syncManager.getOrCreateSyncHandler("workingEnabled", BooleanSyncValue.class,
                () -> new BooleanSyncValue(getter, setter));
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

    public static FluidSlot createTankWidget() {
        return new FluidSlot().size(20, 58).alwaysShowFull(false);
    }

    public static ItemSlot createBatterySlot(SimpleTieredMachine tieredMachine, PanelSyncManager syncManager) {
        ItemSlotSyncHandler battery = new ItemSlotSyncHandler(new ModularSlot(tieredMachine.getChargerInventory(), 0));
        syncManager.syncValue("battery", battery);
        return new ItemSlot().syncHandler("battery").background(GTGuiTextures.SLOT, GTGuiTextures.CHARGER_OVERLAY);
    }

    public static ItemSlot createBatterySlot(IItemHandler itemHandler, int slot, PanelSyncManager syncManager) {
        ItemSlotSyncHandler battery = new ItemSlotSyncHandler(new ModularSlot(itemHandler, slot));
        syncManager.syncValue("battery", battery);
        return new ItemSlot().syncHandler("battery").background(GTGuiTextures.SLOT, GTGuiTextures.CHARGER_OVERLAY);
    }

    public static ToggleButton createVoidingButton(IVoidable machine, PanelSyncManager syncManager) {
        // TODO pull in voiding mode pr
        return new ToggleButton();
        // EnumSyncValue voidMode = new EnumSyncValue(IVoidable.VoidingMode.class, machine.)
    }

    public static ToggleButton createAutoOutputItemButton(AutoOutputTrait autoOutput, PanelSyncManager syncManager) {
        BooleanSyncValue itemOutputs = new BooleanSyncValue(autoOutput::isAutoOutputItems,
                autoOutput::setAllowAutoOutputItems);
        syncManager.syncValue("auto_output_items", itemOutputs);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(itemOutputs::getBoolValue, itemOutputs::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable("gtceu.gui.item_auto_output",
                        Component.translatable(itemOutputs.getBoolValue() ? "cover.voiding.label.enabled" :
                                "cover.voiding.label.disabled")))));
    }

    public static ToggleButton createAutoOutputFluidButton(AutoOutputTrait autoOutput, PanelSyncManager syncManager) {
        BooleanSyncValue fluidOutputs = new BooleanSyncValue(autoOutput::isAutoOutputFluids,
                autoOutput::setAllowAutoOutputFluids);
        syncManager.syncValue("auto_output_fluids", fluidOutputs);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(fluidOutputs::getBoolValue, fluidOutputs::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable("gtceu.gui.fluid_auto_output",
                        Component.translatable(fluidOutputs.getBoolValue() ? "cover.voiding.label.enabled" :
                                "cover.voiding.label.disabled")))));
    }

    public static ToggleButton createInputFromOutputItem(AutoOutputTrait autoOutput, PanelSyncManager syncManager) {
        BooleanSyncValue inputFromOutputItem = new BooleanSyncValue(autoOutput::allowsItemInputFromOutputSide,
                autoOutput::setAllowItemInputFromOutputSide);
        syncManager.syncValue("input_from_output_item", inputFromOutputItem);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(inputFromOutputItem::getBoolValue, inputFromOutputItem::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable("gtceu.gui.item_input_from_output",
                        Component.translatable(inputFromOutputItem.getBoolValue() ? "cover.voiding.label.enabled" :
                                "cover.voiding.label.disabled")))));
    }

    public static ToggleButton createInputFromOutputFluid(AutoOutputTrait autoOutput, PanelSyncManager syncManager) {
        BooleanSyncValue inputFromOutputFluid = new BooleanSyncValue(autoOutput::allowsFluidInputFromOutputSide,
                autoOutput::setAllowFluidInputFromOutputSide);
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
                (v) -> circuitSetter.accept(v < 0 ? ItemStack.EMPTY :
                        IntCircuitBehaviour.stack(v,
                                circuitGetter.get().isEmpty() ? 1 : circuitGetter.get().getCount())));
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
        if (GTValues.XMAS.getAsBoolean()) {
            return new IDrawable.DrawableWidget(GTGuiTextures.GREGTECH_LOGO_XMAS);
        }
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

    public static CycleButtonWidget createIOCycleButton(EnumSyncValue<IO> syncValue, boolean allowExtendedIO) {
        var cycleButton = new CycleButtonWidget()
                .stateCount(allowExtendedIO ? 4 : 2)
                .stateOverlay(IO.IN, IO.IN.getUiTexture())
                .stateOverlay(IO.OUT, IO.OUT.getUiTexture())
                .tooltipBuilder(
                        r -> r.addLine(IKey.dynamic(() -> Component.translatable(syncValue.getValue().getTooltip()))));

        if (allowExtendedIO) {
            cycleButton.stateOverlay(IO.BOTH, IO.BOTH.getUiTexture());
            cycleButton.stateOverlay(IO.NONE, IO.NONE.getUiTexture());
        }

        return cycleButton;
    }

    public static <T, S extends Filter<T, S>> ParentWidget<?> createFilterRow(FilterHandler<T, S> filterHandler,
                                                                              Function<ItemStack, S> filterLoader,
                                                                              SidedPosGuiData data,
                                                                              PanelSyncManager syncManager,
                                                                              UISettings settings) {
        var filterSlot = filterHandler.getFilterSlot();
        // TODO get the panel to use the right sync handler when swapping from one item filter to the next
        var panelHandler = syncManager.syncedPanel("filterPanel", true,
                (sm, sh) -> filterLoader.apply(filterSlot.getStackInSlot(0)).getPanel(data, sm, settings));

        DynamicSyncHandler filterButton = new DynamicSyncHandler()
                .widgetProvider((sm, buf) -> {
                    ItemStack stack = buf.readItem();
                    if (stack.isEmpty()) return new EmptyWidget();
                    stack = filterSlot.getStackInSlot(0);
                    S filter = filterLoader.apply(stack);

                    return new ButtonWidget<>()
                            .onMousePressed((x, y, b) -> {
                                panelHandler.openPanel();
                                return true;
                            });
                });

        return Flow.row()
                .coverChildrenHeight()
                .child(new ItemSlot()
                        .slot(new ModularSlot(filterSlot, 0)
                                .changeListener((stack, amount, client, init) -> {
                                    filterButton.notifyUpdate(packet -> packet.writeItem(stack));
                                }))
                        .marginLeft(2))
                .child(new DynamicSyncedWidget<>().syncHandler(filterButton));
    }

    private static int getIncrementValue(MouseData data) {
        return getIncrementValue(data, 1);
    }

    private static int getIncrementValue(MouseData data, int step) {
        int adjust = step;
        if (data.shift()) adjust *= 4;
        if (data.ctrl()) adjust *= 16;
        if (data.alt()) adjust *= 64;
        return adjust;
    }

    private static IKey createAdjustOverlay(boolean increment) {
        return createAdjustOverlay(increment, 1);
    }

    private static IKey createAdjustOverlay(boolean increment, int step) {
        final StringBuilder builder = new StringBuilder();
        builder.append(increment ? '+' : '-');
        builder.append(getIncrementValue(MouseData.create(-1), step));

        float scale = 1f;
        if (builder.length() == 3) {
            scale = 0.8f;
        } else if (builder.length() == 4) {
            scale = 0.6f;
        } else if (builder.length() > 4) {
            scale = 0.5f;
        }
        return IKey.str(builder.toString())
                .color(Color.WHITE.main)
                .scale(scale);
    }

    public static ParentWidget<?> createIntInputWithButtons(IntSyncValue syncValue, IntSupplier minValue,
                                                            IntSupplier maxValue) {
        return createIntInputWithButtons(syncValue, minValue, maxValue, 1, GTGuiTextures.DISPLAY);
    }

    public static ParentWidget<?> createIntInputWithButtons(IntSyncValue syncValue, IntSupplier minValue,
                                                            IntSupplier maxValue, int step) {
        return createIntInputWithButtons(syncValue, minValue, maxValue, step, GTGuiTextures.DISPLAY);
    }

    public static ParentWidget<?> createIntInputWithButtons(IntSyncValue syncValue, IntSupplier minValue,
                                                            IntSupplier maxValue, IDrawable background) {
        return createIntInputWithButtons(syncValue, minValue, maxValue, 1, background);
    }

    public static ParentWidget<?> createIntInputWithButtons(IntSyncValue syncValue, IntSupplier minValue,
                                                            IntSupplier maxValue, int step, IDrawable background) {
        StringSyncValue formattedValue = new StringSyncValue(syncValue::getStringValue,
                syncValue::setStringValue);

        return Flow.row()
                .coverChildrenHeight()
                .marginBottom(2)
                .widthRel(1.0f)
                .child(new ButtonWidget<>()
                        .left(0).width(18)
                        .onMousePressed((x, y, button) -> {
                            int val = syncValue.getIntValue() - getIncrementValue(MouseData.create(button), step);
                            val = Mth.clamp(val, minValue.getAsInt(), maxValue.getAsInt());
                            syncValue.setIntValue(val, true, true);
                            return true;
                        })
                        .onUpdateListener(w -> w.overlay(createAdjustOverlay(false, step))))
                .child(new TextFieldWidget()
                        .left(18).right(18)
                        .setTextAlignment(Alignment.Center)
                        .setTextColor(Color.WHITE.darker(1))
                        .setNumbers(minValue, maxValue)
                        .onMouseScrolled((mouseX, mouseY, delta) -> {
                            int inc = (int) delta * getIncrementValue(MouseData.create(-1));
                            int val = Mth.clamp(syncValue.getIntValue() + inc, minValue.getAsInt(),
                                    maxValue.getAsInt());
                            syncValue.setIntValue(val, true, true);
                            return true;
                        })
                        .value(formattedValue)
                        .background(background))
                .child(new ButtonWidget<>()
                        .right(0).width(18)
                        .onMousePressed((x, y, button) -> {
                            int val = syncValue.getIntValue() + getIncrementValue(MouseData.create(button), step);
                            val = Mth.clamp(val, minValue.getAsInt(), maxValue.getAsInt());
                            syncValue.setIntValue(val, true, true);
                            return true;
                        })
                        .onUpdateListener(w -> w.overlay(createAdjustOverlay(true, step))));
    }

    public static ParentWidget<?> createIntInputWithBucketMode(IntSyncValue intSyncValue,
                                                               EnumSyncValue<BucketMode> bucketModeSyncValue,
                                                               IntSupplier maxMB) {
        StringSyncValue formattedValue = new StringSyncValue(
                () -> String.valueOf(intSyncValue.getValue()),
                (v) -> intSyncValue.setValue(Integer.parseInt(v), true,
                        true));

        return Flow.row()
                .coverChildrenHeight()
                .marginBottom(2)
                .widthRel(1.0f)
                .child(new ButtonWidget<>()
                        .width(18)
                        .onMousePressed((x, y, button) -> {
                            int val = intSyncValue.getIntValue() - (getIncrementValue(MouseData.create(button)) *
                                    bucketModeSyncValue.getValue().multiplier);
                            val = Mth.clamp(val, 0, maxMB.getAsInt());
                            intSyncValue.setIntValue(val, true, true);
                            return true;
                        })
                        .onUpdateListener(w -> w.overlay(createAdjustOverlay(false))))
                .child(new TextFieldWidget()
                        .left(18).right(36)
                        .setTextAlignment(Alignment.Center)
                        .setTextColor(Color.WHITE.darker(1))
                        .setNumbers(0, maxMB.getAsInt())
                        .onMouseScrolled((mouseX, mouseY, delta) -> {
                            int inc = (int) delta * (getIncrementValue(MouseData.create(-1)) *
                                    bucketModeSyncValue.getValue().multiplier);
                            int val = Mth.clamp(intSyncValue.getIntValue() + inc, 0, maxMB.getAsInt());
                            intSyncValue.setIntValue(val, true, true);
                            return true;
                        })
                        .value(formattedValue)
                        .background(GTGuiTextures.DISPLAY))
                .child(new ButtonWidget<>()
                        .right(18)
                        .width(18)
                        .onMousePressed((x, y, button) -> {
                            int val = intSyncValue.getIntValue() + (getIncrementValue(MouseData.create(button)) *
                                    bucketModeSyncValue.getValue().multiplier);
                            val = Mth.clamp(val, 0, maxMB.getAsInt());
                            intSyncValue.setIntValue(val, true, true);
                            return true;
                        })
                        .onUpdateListener(w -> w.overlay(createAdjustOverlay(true))))
                .child(new CycleButtonWidget()
                        .right(0)
                        .width(18)
                        .value(bucketModeSyncValue)
                        .background(BucketMode.BUCKET.getIcon(), BucketMode.MILLI_BUCKET.getIcon()));
    }

    public static class EnumRowBuilder<T extends Enum<T>> {

        private EnumSyncValue<T> syncValue;
        private final Class<T> enumValue;
        private IKey lang;
        private IDrawable[] background;
        private IDrawable selectedBackground;
        private IDrawable[] overlay;

        public EnumRowBuilder(Class<T> enumValue) {
            this.enumValue = enumValue;
        }

        public EnumRowBuilder<T> value(EnumSyncValue<T> syncValue) {
            this.syncValue = syncValue;
            return this;
        }

        public EnumRowBuilder<T> lang(IKey lang) {
            this.lang = lang;
            return this;
        }

        public EnumRowBuilder<T> background(IDrawable... background) {
            this.background = background;
            return this;
        }

        public EnumRowBuilder<T> selectedBackground(IDrawable selectedBackground) {
            this.selectedBackground = selectedBackground;
            return this;
        }

        public EnumRowBuilder<T> overlay(IDrawable... overlay) {
            this.overlay = overlay;
            return this;
        }

        public EnumRowBuilder<T> overlay(int size, IDrawable... overlay) {
            this.overlay = new IDrawable[overlay.length];
            for (int i = 0; i < overlay.length; i++) {
                this.overlay[i] = overlay[i].asIcon().size(size);
            }
            return this;
        }

        private BoolValue.Dynamic boolValueOf(EnumSyncValue<T> syncValue, T value) {
            return new BoolValue.Dynamic(() -> syncValue.getValue() == value, $ -> syncValue.setValue(value));
        }

        public Flow build() {
            var row = Flow.row().marginBottom(2).coverChildrenHeight().widthRel(1f);
            if (this.enumValue != null && this.syncValue != null) {
                for (var enumVal : enumValue.getEnumConstants()) {
                    var button = new ToggleButton().size(18).marginRight(2)
                            .value(boolValueOf(this.syncValue, enumVal));

                    if (this.background != null && this.background.length > 0)
                        button.background(this.background);
                    else
                        button.background(GTGuiTextures.MC_BUTTON);

                    if (this.selectedBackground != null)
                        button.selectedBackground(this.selectedBackground);
                    else
                        button.selectedBackground(GTGuiTextures.MC_BUTTON_DISABLED);

                    if (this.overlay != null)
                        button.overlay(this.overlay[enumVal.ordinal()]);

                    if (enumVal instanceof StringRepresentable serializable) {
                        button.addTooltipLine(IKey.lang(serializable.getSerializedName()));
                    }
                    row.child(button);
                }
            }

            if (this.lang != null)
                row.child(this.lang.asWidget().align(Alignment.CenterRight).height(18));

            return row;
        }
    }
}
