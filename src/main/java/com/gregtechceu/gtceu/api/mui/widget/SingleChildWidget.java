package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SingleChildWidget<W extends SingleChildWidget<W>> extends Widget<W> {

    private IWidget child;
    private List<IWidget> list = Collections.emptyList();

    @Override
    public @NotNull List<IWidget> getChildren() {
        return this.list;
    }

    private void updateList() {
        this.list = this.child == null ? Collections.emptyList() : Collections.singletonList(this.child);
    }

    public W child(IWidget child) {
        if (child == this || this.child == child) {
            return getThis();
        }

        this.child = child;
        if (isValid()) {
            child.initialise(this);
        }
        updateList();
        return getThis();
    }
}
