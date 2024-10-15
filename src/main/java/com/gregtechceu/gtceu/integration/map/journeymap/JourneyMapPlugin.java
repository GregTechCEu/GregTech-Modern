package com.gregtechceu.gtceu.integration.map.journeymap;

import com.gregtechceu.gtceu.GTCEu;

import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;
import lombok.Getter;

@ClientPlugin
public class JourneyMapPlugin implements IClientPlugin {

    @Getter
    private static boolean active = false;

    @Getter
    private static IClientAPI jmApi;

    @Override
    public void initialize(IClientAPI jmClientApi) {
        active = true;
        jmApi = jmClientApi;
    }

    @Override
    public String getModId() {
        return GTCEu.MOD_ID;
    }

    @Override
    public void onEvent(ClientEvent event) {}
}
