package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.widget.IValueWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;

import lombok.Getter;

public class ValueWidget<W extends ValueWidget<W, T>, T> extends Widget<W> implements IValueWidget<T> {

    @Getter
    private final T widgetValue;

    public ValueWidget(T widgetValue) {
        this.widgetValue = widgetValue;
    }
}
