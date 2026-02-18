package com.gregtechceu.gtceu.api.mui.utils;

import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.LocatedWidget;

import org.jetbrains.annotations.Nullable;

public class HoveredWidgetList {

    private final ObjectList<LocatedWidget> delegate;

    public HoveredWidgetList(ObjectList<LocatedWidget> delegate) {
        this.delegate = delegate;
    }

    public void add(IWidget widget, IViewportStack viewports, Object additionalHoverInfo) {
        this.delegate.addFirst(new LocatedWidget(widget, viewports.peek(), additionalHoverInfo));
    }

    @Nullable
    public IWidget peek() {
        return isEmpty() ? null : this.delegate.get(0).getElement();
    }

    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    public int size() {
        return this.delegate.size();
    }
}
