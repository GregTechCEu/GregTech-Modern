package com.gregtechceu.gtceu.common.mui;

import brachy.modularui.factory.GuiManager;
import com.gregtechceu.gtceu.api.mui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.mui.factory.MachineUIFactory;

public class GTUIFactories {

    public static void init() {
        GuiManager.registerFactory(MachineUIFactory.INSTANCE);
        GuiManager.registerFactory(CoverUIFactory.INSTANCE);
    }
}
