package brachy.modularui.widget.sizer;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.widgets.layout.IExpander;

public class ExpanderResizer extends StandardResizer implements IExpander {

    private final GuiAxis axis;

    public ExpanderResizer(IWidget widget, GuiAxis axis) {
        super(widget);
        this.axis = axis;
    }

    @Override
    public ExpanderResizer copy() {
        return copy(getWidget());
    }

    @Override
    public ExpanderResizer copy(IWidget widget) {
        ExpanderResizer resizer = new ExpanderResizer(widget, this.axis);
        resizer.copyPropertiesOf(this);
        return resizer;
    }

    @Override
    public GuiAxis getExpandAxis() {
        return axis;
    }
}
