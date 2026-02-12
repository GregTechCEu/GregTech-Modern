package com.gregtechceu.gtceu.api.mui.widgets.menu;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class MenuPanel extends ModularPanel {

    public MenuPanel(String name, IWidget menu) {
        super(name);
        fullScreenInvisible();
        child(menu);
        themeOverride("modularui.context_menu");
    }

    public void openSubMenu(IWidget menuList) {
        child(menuList);
    }

    @Override
    protected void onChildAdd(IWidget child) {
        super.onChildAdd(child);
        child.scheduleResize();
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    @Override
    public boolean closeOnOutOfBoundsClick() {
        return true;
    }
}
