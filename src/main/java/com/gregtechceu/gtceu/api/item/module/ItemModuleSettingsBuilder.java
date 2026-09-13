package com.gregtechceu.gtceu.api.item.module;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.DrawableStack;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.drawable.progress.ProgressDrawable;
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
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.function.*;

public final class ItemModuleSettingsBuilder extends ArrayList<IWidget> {

    private final PanelSyncManager psm;
    private final int id;
    private int current = 0;

    ItemModuleSettingsBuilder(PanelSyncManager psm, int id) {
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

    public ItemModuleSettingsBuilder custom(Text label, SyncHandler<?> syncHandler, Widget<?> widget) {
        String key = registerSyncValue(syncHandler);
        add(wrap(label, widget
                .syncHandler(key, id)));
        return this;
    }

    public ItemModuleSettingsBuilder bool(Text label, BooleanSupplier getter, Consumer<Boolean> setter) {
        return custom(label,
                SyncHandlers.intNumber(() -> getter.getAsBoolean() ? 0 : 1, x -> setter.accept(x == 0))
                        .allowC2S(),
                new ToggleButton()
                        .invertSelected(true)
                        .overlay(false, GuiTextures.CHECKMARK));
    }

    public ItemModuleSettingsBuilder num(Text label, IntSupplier getter, IntConsumer setter, int min, int max) {
        return num(label, getter, setter, min, max, "%d");
    }

    public ItemModuleSettingsBuilder num(Text label, IntSupplier getter, IntConsumer setter, int min, int max, String sliderLabel) {
        custom(label,
                SyncHandlers.intNumber(getter, setter)
                        .allowC2S(),
                new SliderWidget()
                        .bounds(min, max)
                        .width(50)
                        .stopper(1))
                .<Flow>last()
                .child(new TextWidget<>(
                        Text.dynamic(() -> Component.literal(sliderLabel.formatted(getter.getAsInt())))));
        return this;
    }

    public ItemModuleSettingsBuilder num(Text label, DoubleSupplier getter, DoubleConsumer setter, double min, double max) {
        return num(label, getter, setter, min, max, "%.2f"::formatted);
    }

    public ItemModuleSettingsBuilder num(Text label, DoubleSupplier getter, DoubleConsumer setter, double min, double max,
                                         DoubleFunction<String> sliderLabel) {
        custom(label,
                SyncHandlers.doubleNumber(getter, setter)
                        .allowC2S(),
                new SliderWidget()
                        .bounds(min, max)
                        .width(50)
                        .stopper((max - min) / 10))
                .<Flow>last()
                .child(new TextWidget<>(
                        Text.dynamic(() -> Component.literal(sliderLabel.apply(getter.getAsDouble())))));
        return this;
    }

    public ItemModuleSettingsBuilder str(Text label, Supplier<String> getter, Consumer<String> setter) {
        return custom(label,
                SyncHandlers.string(getter, setter)
                        .allowC2S(),
                new TextFieldWidget());
    }

    public ItemModuleSettingsBuilder progress(Text label, DoubleSupplier getter, DoubleFunction<String> rightLabel) {
        DoubleSyncValue syncValue = SyncHandlers.doubleNumber(getter, null);
        custom(label, syncValue, new ProgressWidget()
                .texture(new Rectangle().hollow(1).color(0xFF555555),
                        new DrawableStack(
                                new Rectangle().color(0xFFEEE600),
                                new Rectangle().hollow(1).color(0xFF555555)),
                        ProgressDrawable.Direction.RIGHT)
                .width(50))
                .<Flow>last()
                .child(new TextWidget<>(
                        Text.dynamic(() -> Component.literal(rightLabel.apply(syncValue.getValue())))));
        return this;
    }
}
