package com.gregtechceu.gtceu.api.mui.utils;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.LocatedWidget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.TransformationMatrix;

import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.Nullable;

public class HoveredWidgetList {

    private final ObjectList<LocatedWidget> delegate;

    public HoveredWidgetList(ObjectList<LocatedWidget> delegate) {
        this.delegate = delegate;
    }

    public void add(IWidget widget, TransformationMatrix viewports) {
        this.delegate.add(0, new LocatedWidget(widget, viewports));
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
