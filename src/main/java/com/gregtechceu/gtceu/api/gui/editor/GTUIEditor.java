package com.gregtechceu.gtceu.api.gui.editor;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.ui.UIEditor;

/**
 * @author KilaBash
 * @date 2023/7/5
 * @implNote GTUIEditor
 */
@LDLRegister(name = "editor.gtceu", group = "editor")
public class GTUIEditor extends UIEditor {
    public GTUIEditor() {
        super(LDLib.location);
    }
}
