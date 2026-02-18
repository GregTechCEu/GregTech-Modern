package com.gregtechceu.gtceu.api.mui.widgets.menu;

import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

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
    public void onClose() {
        super.onClose();
        // close all menus that are related to this panel
        closeAllMenus(false, false);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void closeAllMenus(boolean soft, boolean requireNoHover) {
        // need to collect menus first instead of closing while iterating to avoid CME
        List<Menu> menus = WidgetTree.flatListByType(this, Menu.class);
        for (Menu<?> menu : menus) {
            menu.checkClose(soft, requireNoHover);
        }
    }
}
