package com.lowdragmc.lowdraglib.gui.editor.ui.tool;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import java.util.ArrayList;
import java.util.List;

public class WidgetToolBox {

    public static class Default {

        public static final List<Default> TABS = new ArrayList<>();
        public static final Default CONTAINER = registerTab("container", IGuiTexture.EMPTY);

        public final String groupName;
        public final IGuiTexture icon;

        public Default(String groupName, IGuiTexture icon) {
            this.groupName = groupName;
            this.icon = icon;
        }

        public static Default registerTab(String groupName, IGuiTexture icon) {
            Default tab = new Default(groupName, icon);
            TABS.add(tab);
            return tab;
        }

        public Object createToolBox() {
            return new Object();
        }
    }
}
