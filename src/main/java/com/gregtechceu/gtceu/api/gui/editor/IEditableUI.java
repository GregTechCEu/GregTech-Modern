package com.gregtechceu.gtceu.api.gui.editor;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IEditableUI<W extends Widget, T> {

    W createDefault();

    void setupUI(WidgetGroup template, T instance);

    record Normal<A extends Widget, B>(Supplier<A> supplier, BiConsumer<WidgetGroup, B> binder)
            implements IEditableUI<A, B> {

        @Override
        public A createDefault() {
            return supplier.get();
        }

        @Override
        public void setupUI(WidgetGroup template, B instance) {
            binder.accept(template, instance);
        }
    }
}
