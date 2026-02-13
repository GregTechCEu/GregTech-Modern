package com.gregtechceu.gtceu.api.mui.widget.sizer;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.IExpander;

public class ExpanderResizer extends StandardResizer implements IExpander {

    private final GuiAxis axis;

    public ExpanderResizer(IWidget widget, GuiAxis axis) {
        super(widget);
        this.axis = axis;
    }

    @Override
    public GuiAxis getExpandAxis() {
        return axis;
    }
}
