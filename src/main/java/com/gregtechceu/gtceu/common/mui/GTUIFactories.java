package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.api.mui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.mui.factory.MachineUIFactory;

import brachy.modularui.factory.GuiManager;

public class GTUIFactories {

    public static void init() {
        GuiManager.registerFactory(MachineUIFactory.INSTANCE);
        GuiManager.registerFactory(CoverUIFactory.INSTANCE);
    }
}
