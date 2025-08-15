package com.gregtechceu.gtceu.api.gui.editor;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.ui.UIEditor;
import com.lowdragmc.lowdraglib.gui.editor.ui.tool.WidgetToolBox;

import static com.lowdragmc.lowdraglib.gui.editor.ui.tool.WidgetToolBox.Default.registerTab;

@LDLRegister(name = "editor.gtceu", group = "editor")
public class GTUIEditor extends UIEditor {

    public static final WidgetToolBox.Default GT_CONTAINER = registerTab("widget.gtm_container",
            Icons.WIDGET_CONTAINER);

    public GTUIEditor() {
        super(LDLib.location);
    }
}
