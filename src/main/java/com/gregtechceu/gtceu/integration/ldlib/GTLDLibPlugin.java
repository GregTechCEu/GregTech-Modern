package com.gregtechceu.gtceu.integration.ldlib;

import com.gregtechceu.gtceu.common.data.GTSyncedFieldAccessors;

import com.lowdragmc.lowdraglib.plugin.ILDLibPlugin;
import com.lowdragmc.lowdraglib.plugin.LDLibPlugin;

import org.apache.logging.log4j.LogManager;

@SuppressWarnings("unused")
@LDLibPlugin
public class GTLDLibPlugin implements ILDLibPlugin {

    @Override
    public void onLoad() {
        LogManager.getLogger().warn("LDLib plugin is loading!");
        GTSyncedFieldAccessors.init();
    }
}
