package com.gregtechceu.gtceu.client.mui.screen.viewport;

import com.gregtechceu.gtceu.api.mui.base.layout.IViewport;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;

import java.util.ArrayList;
import java.util.List;

public class LocatedWidget extends LocatedElement<IWidget> {

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
        GuiViewportStack stack = new GuiViewportStack();
        for (IWidget widget1 : ancestors) {
            if (widget1 instanceof IViewport viewport) {
                stack.pushViewport(viewport, widget1.getArea());
                widget1.transform(stack);
                viewport.transformChildren(stack);
            } else {
                stack.pushMatrix();
                widget1.transform(stack);
            }
        }
        return new LocatedWidget(widget, stack.peek());
    }

    public static final LocatedWidget EMPTY = new LocatedWidget(null, TransformationMatrix.EMPTY);

    public LocatedWidget(IWidget element, TransformationMatrix transformationMatrix) {
        super(element, transformationMatrix);
    }
}
