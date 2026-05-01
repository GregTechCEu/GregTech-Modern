package com.lowdragmc.lowdraglib.gui.editor.ui;

import com.lowdragmc.lowdraglib.gui.editor.data.UIProject;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public class Editor extends WidgetGroup {

    private final ResourcePanel resourcePanel = new ResourcePanel();
    private final TabPages tabPages = new TabPages();
    private final ToolPanel toolPanel = new ToolPanel();
    private final ConfigPanel configPanel = new ConfigPanel();

    public ResourcePanel getResourcePanel() {
        return resourcePanel;
    }

    public TabPages getTabPages() {
        return tabPages;
    }

    public ToolPanel getToolPanel() {
        return toolPanel;
    }

    public ConfigPanel getConfigPanel() {
        return configPanel;
    }

    public void loadProject(UIProject project) {
        project.onLoad(this);
    }
}
