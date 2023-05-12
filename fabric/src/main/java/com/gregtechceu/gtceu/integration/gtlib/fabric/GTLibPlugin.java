package com.gregtechceu.gtceu.integration.gtlib.fabric;

import com.gregtechceu.gtceu.common.data.GTSyncedFieldAccessors;
import com.gregtechceu.gtlib.fabric.IGTLibPlugin;

public class GTLibPlugin implements IGTLibPlugin {
    @Override
    public void onLoad() {
        GTSyncedFieldAccessors.init();
    }
}
