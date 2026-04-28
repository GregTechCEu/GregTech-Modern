package com.lowdragmc.lowdraglib.gui.editor.ui;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public class MainPanel extends WidgetGroup {

    protected final Editor editor;
    protected final WidgetGroup root;

    public MainPanel(Editor editor, WidgetGroup root) {
        super(root.getSelfPosition(), root.getSize());
        this.editor = editor;
        this.root = root;
        addWidget(root);
    }

    @Override
    public MainPanel setBackground(IGuiTexture... textures) {
        super.setBackground(textures);
        return this;
    }
}
