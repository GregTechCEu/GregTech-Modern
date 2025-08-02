package com.gregtechceu.gtceu.api.mui.base.widget;

/**
 * Marks a widget as containing a value
 *
 * @param <T>
 */
public interface IValueWidget<T> extends IWidget {

    /**
     * @return stored value
     */
    T getWidgetValue();
}
