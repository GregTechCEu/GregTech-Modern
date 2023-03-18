package com.gregtechceu.gtceu.integration.top.fabric;

import com.gregtechceu.gtceu.integration.top.TheOneProbePlugin;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbePlugin;

public class TheOneProbePluginImpl implements ITheOneProbePlugin {

    public void onLoad(ITheOneProbe apiInstance) {
        TheOneProbePlugin.init(apiInstance);
    }

}
