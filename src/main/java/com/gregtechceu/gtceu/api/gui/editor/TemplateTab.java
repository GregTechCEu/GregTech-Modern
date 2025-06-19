package com.gregtechceu.gtceu.api.gui.editor;

import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.ui.menu.MenuTab;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;

@LDLRegister(name = "template_tab", group = "editor.gtceu")
public class TemplateTab extends MenuTab {

    protected TreeBuilder.Menu createMenu() {
        return TreeBuilder.Menu.start();
    }
}
