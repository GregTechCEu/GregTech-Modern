package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;

import java.util.List;

public interface WidgetNode<T> {

    IWidget getWidget();

    T getParent();

    List<T> getChildren();
}
