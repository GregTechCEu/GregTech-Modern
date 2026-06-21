package com.gregtechceu.gtceu.integration.map.journeymap;

import com.gregtechceu.gtceu.GTCEu;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.event.RegistryEvent;
import journeymap.api.v2.common.JourneyMapPlugin;
import journeymap.api.v2.common.event.ClientEventRegistry;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@JourneyMapPlugin(apiVersion = IClientAPI.API_VERSION)
public class GTJourneyMapPlugin implements IClientPlugin {

    @Getter
    private static boolean active = false;

    @Getter
    private static IClientAPI jmApi;

    @Getter
    private static JourneymapOptions options;

    @Override
    public void initialize(@NotNull IClientAPI jmClientApi) {
        active = true;
        jmApi = jmClientApi;
        JourneymapEventListener.init();
        ClientEventRegistry.OPTIONS_REGISTRY_EVENT.subscribe(GTCEu.MOD_ID, GTJourneyMapPlugin::onOptionsRegistry);
    }

    @Override
    public String getModId() {
        return GTCEu.MOD_ID;
    }

    protected static void onOptionsRegistry(RegistryEvent.OptionsRegistryEvent event) {
        options = new JourneymapOptions();
    }
}
