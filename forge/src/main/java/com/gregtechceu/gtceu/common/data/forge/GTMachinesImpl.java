package com.gregtechceu.gtceu.common.data.forge;

import com.gregtechceu.gtceu.GTCEu;

public class GTMachinesImpl {

    public static void initPlatformIntegrations() {
        if (GTCEu.isMekanismLoaded()) {
            GTMekanismMachines.init();
        }
    }
}
