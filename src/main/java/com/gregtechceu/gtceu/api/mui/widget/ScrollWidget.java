package com.gregtechceu.gtceu.api.mui.widget;

import com.gregtechceu.gtceu.api.mui.base.widget.IParentWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.scroll.HorizontalScrollData;
import com.gregtechceu.gtceu.api.mui.widget.scroll.VerticalScrollData;

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
