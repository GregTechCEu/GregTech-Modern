package com.gregtechceu.gtceu.api.mui.widgets.menu;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;

public class Menu<W extends Menu<W>> extends ParentWidget<W> implements IMenuPart {

    private AbstractMenuButton<?> menuSource;

    void setMenuSource(AbstractMenuButton<?> source) {
        this.menuSource = source;
    }

    @Override
    public void onMouseLeaveArea() {
        super.onMouseLeaveArea();
        checkClose();
    }

    protected void checkClose() {
        if (this.menuSource != null && !this.menuSource.isBelowMouse() && !isSelfOrChildHovered()) {
            this.menuSource.closeMenu(true);
            this.menuSource.checkClose();
        }
    }

    @Override
    protected void onChildAdd(IWidget child) {
        super.onChildAdd(child);
        if (!child.resizer().hasHeight()) {
            child.resizer().height(12);
        }
        if (!child.resizer().hasWidth()) {
            child.resizer().widthRel(1f);
        }
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getWidgetTheme(IThemeApi.PANEL);
    }
}
