package com.gregtechceu.gtceu.integration.map.journeymap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;

import com.lowdragmc.lowdraglib.LDLib;

import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;
import lombok.Getter;

@ClientPlugin
public class JourneyMapPlugin implements IClientPlugin {

    @Getter
    private static boolean isActive = false;

    @Override
    public void initialize(IClientAPI jmClientApi) {
        if (LDLib.isModLoaded(GTValues.MODID_JOURNEYMAP)) {
            isActive = true;
        }
    }

    @Override
    public String getModId() {
        return GTCEu.MOD_ID;
    }

    @Override
    public void onEvent(ClientEvent event) {}
}
