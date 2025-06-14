package com.gregtechceu.gtceu.api.gui.editor;

import com.gregtechceu.gtceu.api.gui.WidgetUtils;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class EditableUI<W extends Widget, T> implements IEditableUI<W, T> {

    @Getter
    final String id;
    final Class<W> clazz;
    @Getter
    final Supplier<W> widgetSupplier;
    @Getter
    final BiConsumer<W, T> binder;

    public EditableUI(String id, Class<W> clazz, Supplier<W> widgetSupplier, BiConsumer<W, T> binder) {
        this.id = id;
        this.clazz = clazz;
        this.widgetSupplier = widgetSupplier;
        this.binder = binder;
    }

    public W createDefault() {
        var widget = widgetSupplier.get();
        widget.setId(id);
        return widget;
    }

    public void setupUI(WidgetGroup template, T instance) {
        WidgetUtils.widgetByIdForEach(template, "^" + id + "$", clazz, w -> binder.accept(w, instance));
    }
}
