package com.gregtechceu.gtceu.api.mui.widgets.menu;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;

public interface IMenuPart extends IWidget {

    default boolean isSelfOrChildHovered() {
        return isBelowMouse() || !WidgetTree.foreachChild(this,
                w -> !(w instanceof IMenuPart menuPart ? menuPart.isSelfOrChildHovered() : w.isBelowMouse()),
                false);
    }
}
