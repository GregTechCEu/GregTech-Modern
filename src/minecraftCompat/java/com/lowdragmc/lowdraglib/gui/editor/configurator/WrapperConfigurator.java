package com.lowdragmc.lowdraglib.gui.editor.configurator;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

public class WrapperConfigurator extends Configurator {

    public final String name;
    public final Widget inner;

    public WrapperConfigurator(String name, Widget inner) {
        this.name = name;
        super.name = name;
        this.inner = inner;
    }
}
