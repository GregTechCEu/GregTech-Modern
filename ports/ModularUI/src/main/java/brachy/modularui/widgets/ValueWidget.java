package brachy.modularui.widgets;

import brachy.modularui.api.widget.IValueWidget;
import brachy.modularui.widget.Widget;

import lombok.Getter;

public class ValueWidget<W extends ValueWidget<W, T>, T> extends Widget<W> implements IValueWidget<T> {

    @Getter
    private final T widgetValue;

    public ValueWidget(T widgetValue) {
        this.widgetValue = widgetValue;
    }
}
