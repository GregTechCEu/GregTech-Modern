package brachy.modularui.widget;

import brachy.modularui.api.widget.IParentWidget;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.widget.scroll.HorizontalScrollData;
import brachy.modularui.widget.scroll.VerticalScrollData;

public class ScrollWidget<W extends ScrollWidget<W>> extends AbstractScrollWidget<IWidget, W>
        implements IParentWidget<IWidget, W> {

    public ScrollWidget() {
        super(null, null);
    }

    public ScrollWidget(VerticalScrollData data) {
        super(null, data);
    }

    public ScrollWidget(HorizontalScrollData data) {
        super(data, null);
    }

    @Override
    public boolean addChild(IWidget child, int index) {
        return super.addChild(child, index);
    }
}
