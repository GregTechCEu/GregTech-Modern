package com.lowdragmc.lowdraglib.gui.editor.configurator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfiguratorGroup extends Configurator {

    private final List<Configurator> configurators = new ArrayList<>();

    public void addConfigurators(Configurator... configurators) {
        this.configurators.addAll(Arrays.asList(configurators));
    }

    public List<Configurator> getConfigurators() {
        return configurators;
    }
}
