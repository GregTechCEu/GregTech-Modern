package com.gregtechceu.gtceu.api.gui.factory;

import com.lowdragmc.lowdraglib.gui.factory.UIFactory;

public final class GTUIFactories {

    private GTUIFactories() {}

    public static void init() {
        UIFactory.register(MachineUIFactory.INSTANCE);
        UIFactory.register(CoverUIFactory.INSTANCE);
        UIFactory.register(GTHeldItemUIFactory.INSTANCE);
        UIFactory.register(GTUIEditorFactory.INSTANCE);
    }
}
