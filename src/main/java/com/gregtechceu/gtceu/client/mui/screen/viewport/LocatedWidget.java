package com.gregtechceu.gtceu.client.mui.screen.viewport;

import com.gregtechceu.gtceu.api.mui.base.layout.IViewport;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;

import it.unimi.dsi.fastutil.Hash;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocatedWidget extends LocatedElement<IWidget> {

    public static final Hash.Strategy<LocatedWidget> HASH_STRATEGY = new HashStrategy();

    private static final GuiViewportStack STACK = new GuiViewportStack();

    public static LocatedWidget of(IWidget widget) {
        if (widget == null) {
            return EMPTY;
        }
        // first make a list of all parents
        IWidget parent = widget;
        List<IWidget> ancestors = new ArrayList<>();
        while (true) {
            ancestors.add(0, parent);
            if (parent instanceof ModularPanel) {
                break;
            }
            parent = parent.getParent();
        }
        // iterate through each parent starting at the root and apply each transformation
        STACK.reset();
        for (IWidget widget1 : ancestors) {
            if (widget1 instanceof IViewport viewport) {
                STACK.pushViewport(viewport, widget1.getArea());
                widget1.transform(STACK);
                viewport.transformChildren(STACK);
            } else {
                STACK.pushMatrix();
                widget1.transform(STACK);
            }
        }
        return new LocatedWidget(widget, STACK.peek(), null);
    }

    public static final LocatedWidget EMPTY = new LocatedWidget(null, TransformationMatrix.EMPTY, null);
    @Getter
    private final Object additionalHoverInfo;

    public LocatedWidget(IWidget element, TransformationMatrix transformationMatrix, Object additionalHoverInfo) {
        super(element, transformationMatrix);
        this.additionalHoverInfo = additionalHoverInfo;
    }

    @Override
    public String toString() {
        return "LocatedWidget[" + getElement() + " | " + additionalHoverInfo + "]";
    }

    public static class HashStrategy implements Hash.Strategy<LocatedWidget> {

        @Override
        public int hashCode(LocatedWidget o) {
            return Objects.hashCode(o == null ? null : o.getElement());
        }

        @Override
        public boolean equals(LocatedWidget a, LocatedWidget b) {
            if (a == b) return true;
            if (a == null || b == null) return false;
            return Objects.equals(a.getElement(), b.getElement());
        }
    }
}
