package com.gregtechceu.gtceu.api.gui.compass;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.DraggableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;

/**
 * @author KilaBash
 * @date 2022/9/3
 * @implNote LayoutWidget
 */
public class LayoutPageWidget extends DraggableScrollableWidgetGroup {

    @Getter
    protected int pageWidth;
    protected int offset = 0;

    public LayoutPageWidget(int width, int height) {
        super(4, 4, width - 8, height - 4);
        setClientSideWidget();
        this.pageWidth = width - 8;
    }

    public LayoutPageWidget addOffsetSpace(int offset) {
        this.offset += offset;
        return this;
    }

    public LayoutPageWidget addStreamWidget(Widget widget) {
        widget.setSelfPosition(new Position((pageWidth - widget.getSize().width) / 2, offset));
        addWidget(widget);
        offset += widget.getSize().height;
        return this;
    }

}
