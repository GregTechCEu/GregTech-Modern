package com.gregtechceu.gtceu.integration.top.forge;

import com.gregtechceu.gtceu.integration.top.TheOneProbePlugin;
import mcjty.theoneprobe.TheOneProbe;

/**
 * @author KilaBash
 * @date 2023/3/18
 * @implNote TheOneProbePluginImpl
 */
public class TheOneProbePluginImpl {
    public static void init() {
        TheOneProbePlugin.init(TheOneProbe.theOneProbeImp);
    }
}
