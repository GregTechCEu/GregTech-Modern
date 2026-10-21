package com.gregtechceu.gtceu.api.item.module.ui;

import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.DrawableStack;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandler;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.SliderWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;

import java.util.ArrayList;
import java.util.function.*;

public final class ItemModuleSettingsBuilder extends ArrayList<IWidget> {

    private final PanelSyncManager psm;
    private final int id;
    private int current = 0;

    public ItemModuleSettingsBuilder(PanelSyncManager psm, int id) {
        this.psm = psm;
        this.id = id;
    }

    private String registerSyncValue(SyncHandler<?> syncHandler) {
        String key = "module_setting" + current;
        current++;
        psm.syncValue(key, id, syncHandler);
        return key;
    }

    private ItemModuleSettingsBuilder pop() {
        remove(size() - 1);
        return this;
    }

    private <W extends IWidget> W last() {
        // noinspection unchecked
        return (W) get(size() - 1);
    }

    private static Flow wrap(Text label, IWidget widget) {
        return Flow.row()
                .coverChildren()
                .childPadding(5)
                .child(new TextWidget<>(label.get()))
                .child(widget);
    }

    private ItemModuleSettingsBuilder addSetting(Text label, IWidget widget) {
        add(wrap(label, widget));
        return this;
    }

    public ItemModuleSettingsBuilder custom(Text label, SyncHandler<?> syncHandler, Widget<?> widget) {
        String key = registerSyncValue(syncHandler);
        add(wrap(label, widget.syncHandler(key, id)));
        return this;
    }

    public ItemModuleSettingsBuilder bool(Text label, BooleanSupplier getter, Consumer<Boolean> setter) {
        add(wrap(label, new ToggleButton()
                .value(new BooleanSyncValue(getter, setter::accept).allowC2S())
                .overlay(true, GuiTextures.CHECKMARK)));
        return this;
    }

    public ItemModuleSettingsBuilder num(Text label, IntSupplier getter, IntConsumer setter, int min, int max) {
        return num(label, getter, setter, min, max, "%d");
    }

    public ItemModuleSettingsBuilder num(Text label, IntSupplier getter, IntConsumer setter, int min, int max,
                                         String sliderLabel) {
        addSetting(label, new SliderWidget()
                .value(SyncHandlers.intNumber(getter, setter).allowC2S())
                .bounds(min, max)
                .width(50)
                .stopper(1))
                .<Flow>last()
                .child(new TextWidget<>(
                        Text.dynamic(() -> Component.literal(sliderLabel.formatted(getter.getAsInt()))))
                        .width(Math.max(Text.str(sliderLabel.formatted(min)).getDefaultWidth(),
                                Text.str(sliderLabel.formatted(max)).getDefaultWidth())));
        return this;
    }

    public ItemModuleSettingsBuilder num(Text label, DoubleSupplier getter, DoubleConsumer setter, double min,
                                         double max) {
        return num(label, getter, setter, min, max, "%.2f"::formatted);
    }

    public ItemModuleSettingsBuilder num(Text label, DoubleSupplier getter, DoubleConsumer setter, double min,
                                         double max,
                                         DoubleFunction<String> sliderLabel) {
        addSetting(label, new SliderWidget()
                .value(SyncHandlers.doubleNumber(getter, setter).allowC2S())
                .bounds(min, max)
                .width(50)
                .stopper((max - min) / 10))
                .<Flow>last()
                .child(new TextWidget<>(
                        Text.dynamic(() -> Component.literal(sliderLabel.apply(getter.getAsDouble()))))
                        .width(Math.max(Text.str(sliderLabel.apply(min)).getDefaultWidth(),
                                Text.str(sliderLabel.apply(max)).getDefaultWidth())));
        return this;
    }

    public ItemModuleSettingsBuilder str(Text label, Supplier<String> getter, Consumer<String> setter) {
        return addSetting(label, new TextFieldWidget()
                .value(SyncHandlers.string(getter, setter).allowC2S()));
    }

    public ItemModuleSettingsBuilder progress(Text label, DoubleSupplier getter, DoubleFunction<String> rightLabel) {
        DoubleSyncValue syncValue = SyncHandlers.doubleNumber(getter, null);
        addSetting(label, new ProgressWidget()
                .value(syncValue)
                .texture(new Rectangle().hollow(1).color(0xFF555555),
                        new DrawableStack(
                                new Rectangle().color(0xFFEEE600),
                                new Rectangle().hollow(1).color(0xFF555555)),
                        ProgressDrawable.Direction.RIGHT)
                .width(50))
                .<Flow>last()
                .child(new TextWidget<>(
                        Text.dynamic(() -> Component.literal(rightLabel.apply(syncValue.getValue()))))
                        .width(Math.max(Text.str(rightLabel.apply(0)).getDefaultWidth(),
                                Text.str(rightLabel.apply(1)).getDefaultWidth())));
        return this;
    }
}
