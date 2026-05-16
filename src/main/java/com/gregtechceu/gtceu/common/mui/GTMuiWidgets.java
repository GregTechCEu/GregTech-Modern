package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.filter.Filter;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IHasBatterySlot;
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.*;
import brachy.modularui.drawable.text.TextRenderer;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.theme.ThemeAPI;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.sync.*;
import brachy.modularui.widget.EmptyWidget;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.*;

public class GTMuiWidgets {

    public static Flow createTitleBar(MachineDefinition definition, int panelWidth) {
        UITexture background = GTGuiTextures.BACKGROUND;
        if (!definition.getThemeId().equals(ThemeAPI.DEFAULT_ID)) {
            background = (UITexture) ThemeAPI.INSTANCE.getTheme(definition.getThemeId()).getPanelTheme().theme()
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
        var name = stack.getHoverName().getString();
        name = name.replaceAll("§.", "").trim();
        return createTitleBar(new ItemDrawable(stack).asIcon(), name, panelWidth, background);
    }

    public static Flow createTitleBar(Icon icon, String text, int panelWidth, UITexture background) {
        int borderRadius = 5;
        int iconSize = 16;
        int minPanelWidth = (int) (panelWidth * 0.9f) - (iconSize + (borderRadius * 3));
        int textTitleWidth = TextRenderer.getFont().width(text);

        int textRows = (int) Math.ceil((double) textTitleWidth / minPanelWidth);
        int textHeightPerRow = (int) (Text.renderer.getFontHeight());
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
                .child(icon.size(iconSize)
                        .asWidget()
                        .marginLeft(borderRadius))
                .child(Text.str(text)
                        .asWidget()
                        .margin(borderRadius, borderRadius, borderRadius, 1)
                        .size(Math.min(minPanelWidth, textTitleWidth), textHeight));
    }

    public static ToggleButton createToggleButton(BooleanSupplier getter, BooleanConsumer setter, UITexture texture,
                                                  String langKey) {
        var value = new BooleanSyncValue(getter, setter).allowC2S();
        return new ToggleButton()
                .value(value)
                .overlay(texture)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(
                        (r) -> r.addLine(Text.lang(langKey + (value.getBoolValue() ? ".enabled" : ".disabled"))));
    }

    public static ToggleButton createToggleButton(BooleanSupplier getter, BooleanConsumer setter, UITexture background,
                                                  UITexture selectedBackground, String langKey) {
        var value = new BooleanSyncValue(getter, setter).allowC2S();
        return new ToggleButton()
                .value(value)
                .selectedBackground(selectedBackground)
                .background(background)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(
                        (r) -> r.addLine(Text.lang(langKey + (value.getBoolValue() ? ".enabled" : ".disabled"))));
    }

    public static ToggleButton createPowerButton(IRecipeLogicMachine recipeLogicMachine) {
        return createToggleButton(
                () -> recipeLogicMachine.getRecipeLogic().isWorkingEnabled(),
                recipeLogicMachine::setWorkingEnabled,
                GTGuiTextures.BUTTON_POWER[0],
                GTGuiTextures.BUTTON_POWER[1],
                "behaviour.soft_hammer");
    }

    public static ToggleButton createPowerButton(IControllable workable) {
        return createToggleButton(
                workable::isWorkingEnabled,
                workable::setWorkingEnabled,
                GTGuiTextures.BUTTON_POWER[0],
                GTGuiTextures.BUTTON_POWER[1],
                "behaviour.soft_hammer");
    }

    public static ProgressWidget createProgressBar(IRecipeLogicMachine workableMachine, PanelSyncManager syncManager,
                                                   UITexture texture, int size) {
        DoubleSyncValue progressPercent = syncManager.getOrCreateSyncHandler("progressPercent", DoubleSyncValue.class,
                () -> new DoubleSyncValue(() -> {
                    if (workableMachine.getMaxProgress() == 0.0f) return 0.0f;
                    return workableMachine.getProgress() / (double) workableMachine.getMaxProgress();
                }));

        return new ProgressWidget()
                .texture(texture, size)
                .value(progressPercent);
    }

    public static FluidSlot createTankWidget() {
        return new FluidSlot().size(20, 58).alwaysShowFull(false);
    }

    public static ItemSlot createBatterySlot(IHasBatterySlot batterySlot, PanelSyncManager syncManager) {
        ItemSlotSyncHandler battery = new ItemSlotSyncHandler(new ModularSlot(batterySlot.getChargerInventory(), 0));
        syncManager.syncValue("battery", battery);
        return new ItemSlot().syncHandler("battery").background(GTGuiTextures.SLOT, GTGuiTextures.CHARGER_OVERLAY);
    }

    public static CycleButtonWidget createVoidingButton(IVoidable voidable) {
        var value = new EnumSyncValue<>(IVoidable.VoidingMode.class, voidable::getVoidingMode,
                voidable::setVoidingMode).allowC2S();

        return new CycleButtonWidget()
                .overlay(new DynamicDrawable(() -> GTGuiTextures.BUTTON_VOID_MULTIBLOCK[value.getIntValue()]))
                .value(value)
                .tooltipBuilder(
                        r -> r.addLine(Text.dynamic(() -> Component.translatable(value.getValue().getTooltip()))));
    }

    public static ToggleButton createDistinctnessButton(IDistinctPart distinct) {
        return createToggleButton(distinct::isDistinct, distinct::setDistinct, GTGuiTextures.BUTTON_DISTINCT[0],
                GTGuiTextures.BUTTON_DISTINCT[1],
                "gtceu.multiblock.universal.distinct");
    }

    public static ToggleButton createAutoOutputItemButton(AutoOutputTrait autoOutput) {
        return createToggleButton(autoOutput::isAutoOutputItems, autoOutput::setAllowAutoOutputItems,
                GTGuiTextures.BUTTON_ITEM_OUTPUT, "gtceu.gui.item_auto_output");
    }

    public static ToggleButton createAutoOutputFluidButton(AutoOutputTrait autoOutput) {
        return createToggleButton(autoOutput::isAutoOutputFluids, autoOutput::setAllowAutoOutputFluids,
                GTGuiTextures.BUTTON_FLUID_OUTPUT, "gtceu.gui.fluid_auto_output");
    }

    public static ToggleButton createInputFromOutputItem(AutoOutputTrait autoOutput) {
        return createToggleButton(autoOutput::allowsItemInputFromOutputSide,
                autoOutput::setAllowItemInputFromOutputSide, GTGuiTextures.BUTTON_ITEM_OUTPUT,
                "gtceu.gui.item_input_from_output");
    }

    public static ToggleButton createInputFromOutputFluid(AutoOutputTrait autoOutput) {
        return createToggleButton(autoOutput::allowsFluidInputFromOutputSide,
                autoOutput::setAllowFluidInputFromOutputSide, GTGuiTextures.BUTTON_FLUID_OUTPUT,
                "gtceu.gui.fluid_input_from_output");
    }

    private static IntSyncValue createCircuitSlotSyncValue(Consumer<ItemStack> circuitSetter,
                                                           Supplier<ItemStack> circuitGetter) {
        return new IntSyncValue(() -> {
            if (circuitGetter.get().isEmpty()) return -1;
            return IntCircuitBehaviour.getCircuitConfiguration(circuitGetter.get());
        },
                (v) -> circuitSetter.accept(v < 0 ? ItemStack.EMPTY :
                        IntCircuitBehaviour.stack(v)))
                .allowC2S();
    }

    public static ModularPanel<?> createCircuitSlotPanel(IntSyncValue circuitSyncValue, PanelSyncManager syncManager) {
        Grid buttonGrid = new Grid()
                .coverChildren()
                .gridOfSizeWidth(32, 8, (x, y, i) -> new ToggleButton()
                        .size(18)
                        .padding(1)
                        .overlay(new ItemDrawable().setItem(IntCircuitBehaviour.stack(i + 1)))
                        .value(new BoolValue.Dynamic(() -> (i + 1) == circuitSyncValue.getIntValue(),
                                (v) -> {
                                    if (v) circuitSyncValue.setValue(i + 1);
                                })));

        return new Dialog<>("circuit_panel")
                .disablePanelsBelow(false)
                .draggable(true)
                .closeOnOutOfBoundsClick(true)
                .height(105)
                .child(Flow.col()
                        .padding(2)
                        .fullHeight()
                        .coverChildren()
                        .childPadding(7)
                        .top(3)
                        .leftRel(0.5f)
                        .child(Text.lang("item.gtceu.circuit.integrated.gui").asWidget())
                        .child(buttonGrid));
    }

    public static ModularPanel<?> createCircuitSlotPanel(Consumer<ItemStack> circuitSetter,
                                                         Supplier<ItemStack> circuitGetter,
                                                         PanelSyncManager syncManager) {
        IntSyncValue circuitSyncValue = createCircuitSlotSyncValue(circuitSetter, circuitGetter);
        syncManager.syncValue("circuit_slot", circuitSyncValue);
        return createCircuitSlotPanel(circuitSyncValue, syncManager);
    }

    public static ButtonWidget<?> createCircuitSlotPanel(IHasCircuitSlot machine, ModularPanel<?> parentPanel,
                                                         PanelSyncManager syncManager) {
        IntSyncValue circuitSyncValue = createCircuitSlotSyncValue(
                i -> machine.getCircuitInventory().setStackInSlot(0, i),
                () -> machine.getCircuitInventory().getStackInSlot(0));

        syncManager.syncValue("circuit_slot", circuitSyncValue);
        IPanelHandler circuitPanelHandler = syncManager.syncedPanel("circuit_panel", true,
                (sm, sh) -> createCircuitSlotPanel(circuitSyncValue, sm)
                        .relative(parentPanel)
                        .leftRel(0.0f, -4, 1f));

        return new ButtonWidget<>()
                .size(18)
                .onMousePressed((context, b) -> {
                    if (b == InputConstants.MOUSE_BUTTON_LEFT || b == InputConstants.MOUSE_BUTTON_RIGHT) {
                        circuitPanelHandler.openPanel();
                    } else if (b == InputConstants.MOUSE_BUTTON_MIDDLE) {
                        circuitSyncValue.setValue(0);
                    }
                    return true;
                })
                .onMouseScrolled((context, delta) -> {
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
                .tooltipBuilder((r) -> r.addLine(Text.lang("metaitem.int_circuit.configuration",
                        (machine.getCircuitInventory().getStackInSlot(0).isEmpty() ? 0 :
                                IntCircuitBehaviour
                                        .getCircuitConfiguration(machine.getCircuitInventory().getStackInSlot(0))))));
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

    public static CycleButtonWidget createIOCycleButton(EnumSyncValue<IO> syncValue, boolean allowExtendedIO) {
        // Done so the cycle button doesn't create states for every IO enum entry

        IntSyncValue syncVal = new IntSyncValue(syncValue::getIntValue, syncValue::setIntValue).allowC2S();

        var cycleButton = new CycleButtonWidget()
                .stateCount(allowExtendedIO ? 4 : 2)
                .stateOverlay(IO.IN, IO.IN.getUiTexture())
                .stateOverlay(IO.OUT, IO.OUT.getUiTexture())
                .value(syncVal)
                .tooltipBuilder(
                        r -> r.addLine(Text.dynamic(() -> Component.translatable(syncValue.getValue().getTooltip()))));

        if (allowExtendedIO) {
            cycleButton.stateOverlay(IO.BOTH, IO.BOTH.getUiTexture());
            cycleButton.stateOverlay(IO.NONE, IO.NONE.getUiTexture());
        }

        return cycleButton;
    }

    public static <T, S extends Filter<T, S>> ParentWidget<?> createFilterRow(ParentWidget<?> existingRow,
                                                                              FilterHandler<T, S> filterHandler,
                                                                              SidedPosGuiData data,
                                                                              PanelSyncManager syncManager,
                                                                              UISettings settings) {
        var filterSlot = filterHandler.getFilterSlot();
        // TODO get the panel to use the right sync handler when swapping from one item filter to the next
        var panelHandler = syncManager.syncedPanel("filterPanel", true,
                (sm, sh) -> filterHandler.loadFilter(filterSlot.getStackInSlot(0)).getPanel(data, sm, settings));

        DynamicSyncHandler filterButton = new DynamicSyncHandler()
                .widgetProvider((sm, buf) -> {
                    ItemStack stack = buf.readItem();
                    if (stack.isEmpty()) return new EmptyWidget();
                    stack = filterSlot.getStackInSlot(0);
                    S filter = filterHandler.loadFilter(stack);

                    return new ButtonWidget<>()
                            .onMousePressed((context, b) -> {
                                panelHandler.openPanel();
                                return true;
                            });
                });
        return existingRow.child(new ItemSlot()
                .slot(new ModularSlot(filterSlot, 0)
                        .changeListener((stack, amount, client, init) -> filterButton
                                .notifyUpdate(packet -> packet.writeItem(stack)))))
                .child(new DynamicSyncedWidget<>().syncHandler(filterButton));
    }

    public static <T, S extends Filter<T, S>> ParentWidget<?> createFilterRow(FilterHandler<T, S> filterHandler,
                                                                              SidedPosGuiData data,
                                                                              PanelSyncManager syncManager,
                                                                              UISettings settings) {
        return createFilterRow(Flow.row().coverChildrenHeight().childPadding(2), filterHandler, data, syncManager,
                settings);
    }

    private static int getIncrementValue(MouseData data, int step) {
        int adjust = step;
        if (data.shift()) adjust *= 4;
        if (data.ctrl()) adjust *= 16;
        if (data.alt()) adjust *= 64;
        return adjust;
    }

    private static long getIncrementValue(MouseData data, long step) {
        long adjust = step;
        if (data.shift()) adjust *= 4;
        if (data.ctrl()) adjust *= 16;
        if (data.alt()) adjust *= 64;
        return adjust;
    }

    private static Text createAdjustOverlay(boolean increment) {
        return createAdjustOverlay(increment, 1);
    }

    private static Text createAdjustOverlay(boolean increment, long step) {
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
        return Text.str(builder.toString())
                .color(Color.WHITE.main)
                .scale(scale);
    }

    public static ParentWidget<?> createIntInputWithButtons(IntSyncValue syncValue, IntSupplier minValue,
                                                            IntSupplier maxValue) {
        return createIntInputWithButtons(syncValue, minValue, maxValue, 1, GTGuiTextures.DISPLAY);
    }

    public static ParentWidget<?> createIntInputWithButtons(IntSyncValue syncValue, IntSupplier minValue,
                                                            IntSupplier maxValue, int step, IDrawable background) {
        StringSyncValue formattedValue = new StringSyncValue(syncValue::getStringValue,
                syncValue::setStringValue).allowC2S();

        var textField = new TextFieldWidget() {

            @Override
            public boolean onMouseScrolled(double delta) {
                int inc = (int) delta * getIncrementValue(MouseData.create(-1), 1);
                int val = Mth.clamp(syncValue.getIntValue() + inc, minValue.getAsInt(),
                        maxValue.getAsInt());
                syncValue.setIntValue(val, true, true);
                return true;
            }
        }.left(18).right(18)
                .setTextAlignment(Alignment.Center)
                .setTextColor(Color.WHITE.darker(1))
                .setNumbers(minValue, maxValue)
                .value(formattedValue)
                .background(background);

        return Flow.row()
                .coverChildrenHeight()
                .widthRel(1.0f)
                .child(new ButtonWidget<>()
                        .width(18)
                        .onMousePressed((context, button) -> {
                            int val = syncValue.getIntValue() - getIncrementValue(MouseData.create(button), step);
                            val = Mth.clamp(val, minValue.getAsInt(), maxValue.getAsInt());
                            syncValue.setIntValue(val, true, true);
                            return true;
                        })
                        .onUpdateListener(w -> w.overlay(createAdjustOverlay(false, step))))
                .child(textField)
                .child(new ButtonWidget<>()
                        .width(18).right(0)
                        .onMousePressed((context, button) -> {
                            int val = syncValue.getIntValue() + getIncrementValue(MouseData.create(button), step);
                            val = Mth.clamp(val, minValue.getAsInt(), maxValue.getAsInt());
                            syncValue.setIntValue(val, true, true);
                            return true;
                        })
                        .onUpdateListener(w -> w.overlay(createAdjustOverlay(true, step))));
    }

    public static ParentWidget<?> createLongInputWithButtons(LongSyncValue syncValue, LongSupplier minValue,
                                                             LongSupplier maxValue) {
        return createLongInputWithButtons(syncValue, minValue, maxValue, 1, GTGuiTextures.DISPLAY);
    }

    public static ParentWidget<?> createLongInputWithButtons(LongSyncValue syncValue, LongSupplier minValue,
                                                             LongSupplier maxValue, long step, IDrawable background) {
        StringSyncValue formattedValue = new StringSyncValue(syncValue::getStringValue,
                syncValue::setStringValue).allowC2S();

        var textField = new TextFieldWidget() {

            @Override
            public boolean onMouseScrolled(double delta) {
                long inc = (long) delta * getIncrementValue(MouseData.create(-1), 1);
                long value = syncValue.getLongValue() + inc;
                syncValue.setLongValue(GTMath.clamp(value, minValue.getAsLong(), maxValue.getAsLong()),
                        true, true);
                return true;
            }
        }.left(18).right(18)
                .setTextAlignment(Alignment.Center)
                .setTextColor(Color.WHITE.darker(1))
                .setNumbersLong(minValue, maxValue)
                .value(formattedValue)
                .background(background);

        return Flow.row()
                .coverChildrenHeight()
                .widthRel(1.0f)
                .child(new ButtonWidget<>()
                        .width(18)
                        .onMousePressed((context, button) -> {
                            long value = syncValue.getLongValue() - getIncrementValue(MouseData.create(button), step);
                            syncValue.setLongValue(GTMath.clamp(value, minValue.getAsLong(), maxValue.getAsLong()),
                                    true, true);
                            return true;
                        })
                        .onUpdateListener(w -> w.overlay(createAdjustOverlay(false, step))))
                .child(textField)
                .child(new ButtonWidget<>()
                        .width(18).right(0)
                        .onMousePressed((context, button) -> {
                            long value = syncValue.getLongValue() + getIncrementValue(MouseData.create(button), step);
                            long min = minValue.getAsLong();
                            long max = maxValue.getAsLong();
                            syncValue.setLongValue(value < min ? min : Math.min(value, max), true, true);
                            return true;
                        }).onUpdateListener(w -> w.overlay(createAdjustOverlay(true, step))));
    }

    public static ParentWidget<?> createIntInputWithBucketMode(IntSyncValue intSyncValue,
                                                               EnumSyncValue<BucketMode> bucketModeSyncValue,
                                                               IntSupplier maxMB) {
        StringSyncValue formattedValue = new StringSyncValue(
                () -> String.valueOf(intSyncValue.getValue()),
                (v) -> intSyncValue.setValue(Integer.parseInt(v), true,
                        true))
                .allowC2S();

        var textField = new TextFieldWidget() {

            @Override
            public boolean onMouseScrolled(double delta) {
                int inc = (int) delta * (getIncrementValue(MouseData.create(-1), 1) *
                        bucketModeSyncValue.getValue().multiplier);
                int val = Mth.clamp(intSyncValue.getIntValue() + inc, 0, maxMB.getAsInt());
                intSyncValue.setIntValue(val, true, true);
                return true;
            }
        }.left(18).right(36)
                .setTextAlignment(Alignment.Center)
                .setTextColor(Color.WHITE.darker(1))
                .setNumbers(0, maxMB.getAsInt())
                .value(formattedValue)
                .background(GTGuiTextures.DISPLAY);

        return Flow.row()
                .coverChildrenHeight()
                .widthRel(1.0f)
                .child(new ButtonWidget<>()
                        .width(18)
                        .onMousePressed((context, button) -> {
                            int val = intSyncValue.getIntValue() - (getIncrementValue(MouseData.create(button), 1) *
                                    bucketModeSyncValue.getValue().multiplier);
                            val = Mth.clamp(val, 0, maxMB.getAsInt());
                            intSyncValue.setIntValue(val, true, true);
                            return true;
                        })
                        .onUpdateListener(w -> w.overlay(createAdjustOverlay(false))))
                .child(textField)
                .child(new ButtonWidget<>()
                        .right(18)
                        .width(18)
                        .onMousePressed((context, button) -> {
                            int val = intSyncValue.getIntValue() + (getIncrementValue(MouseData.create(button), 1) *
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

        private @Nullable EnumSyncValue<T> syncValue;
        private final Class<T> enumValue;
        private @Nullable Component lang;
        private IDrawable @Nullable [] background;
        private @Nullable IDrawable selectedBackground;
        private IDrawable @Nullable [] overlay;

        public EnumRowBuilder(Class<T> enumValue) {
            this.enumValue = enumValue;
        }

        public EnumRowBuilder<T> value(EnumSyncValue<T> syncValue) {
            this.syncValue = syncValue;
            return this;
        }

        public EnumRowBuilder<T> lang(Component lang) {
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
            var row = Flow.row().coverChildrenHeight().widthRel(1f);
            if (syncValue != null) {
                for (var enumVal : enumValue.getEnumConstants()) {
                    var button = new ToggleButton().size(18).marginRight(2)
                            .value(boolValueOf(Objects.requireNonNull(syncValue), enumVal));

                    if (this.background != null && this.background.length > 0)
                        button.background(this.background);
                    else
                        button.background(GuiTextures.MC_BUTTON);

                    if (this.selectedBackground != null)
                        button.selectedBackground(this.selectedBackground);
                    else
                        button.selectedBackground(GuiTextures.MC_BUTTON_DISABLED);

                    if (this.overlay != null)
                        button.overlay(this.overlay[enumVal.ordinal()]);

                    if (enumVal instanceof StringRepresentable serializable) {
                        button.addTooltipLine(Text.lang(serializable.getSerializedName()));
                    }
                    row.child(button);
                }
            }

            if (this.lang != null)
                row.child(Text.of(lang).asWidget().posRel(Alignment.CenterRight).height(18));

            return row;
        }
    }
}
