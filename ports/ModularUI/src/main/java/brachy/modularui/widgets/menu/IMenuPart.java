package brachy.modularui.widgets.menu;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.widget.WidgetTree;

public interface IMenuPart extends IWidget {

    default boolean isSelfOrChildHovered() {
        return isBelowMouse() || !WidgetTree.foreachChild(this,
                w -> !(w instanceof IMenuPart menuPart ? menuPart.isSelfOrChildHovered() : w.isBelowMouse()),
                false);
    }
}
