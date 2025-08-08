package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ListValueWidget<T, I extends IWidget, W extends ListValueWidget<T, I, W>> extends ListWidget<I, W> {

    private final Function<I, T> widgetToValue;

    public ListValueWidget(Function<I, T> widgetToValue) {
        this.widgetToValue = widgetToValue;
    }

    public List<T> getValues() {
        List<T> list = new ArrayList<>();
        for (I widget : getTypeChildren()) {
            list.add(this.widgetToValue.apply(widget));
        }
        return list;
    }

    public <V> W children(Iterable<V> values, Function<V, I> widgetCreator) {
        for (V value : values) {
            child(widgetCreator.apply(value));
        }
        return getThis();
    }
}
