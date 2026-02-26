package com.gregtechceu.gtceu.common.network;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.common.network.packets.*;
import com.gregtechceu.gtceu.common.network.packets.hazard.*;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectBedrockFluid;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectBedrockOre;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectOre;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class GTNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registar = event.registrar(GTCEuAPI.NETWORK_VERSION);
        // spotless:off
        registar.playBidirectional(SCPacketMonitorGroupNBTChange.TYPE, SCPacketMonitorGroupNBTChange.CODEC, SCPacketMonitorGroupNBTChange::execute);

        registar.playToServer(CPacketImageRequest.TYPE, CPacketImageRequest.CODEC, CPacketImageRequest::execute);
        registar.playToClient(SPacketImageResponse.TYPE, SPacketImageResponse.CODEC, SPacketImageResponse::execute);

        registar.playToServer(CPacketKeyDown.TYPE, CPacketKeyDown.CODEC, CPacketKeyDown::execute);

        registar.playToClient(SPacketAddHazardZone.TYPE, SPacketAddHazardZone.CODEC, SPacketAddHazardZone::execute);
        registar.playToClient(SPacketRemoveHazardZone.TYPE, SPacketRemoveHazardZone.CODEC, SPacketRemoveHazardZone::execute);
        registar.playToClient(SPacketSyncHazardZoneStrength.TYPE, SPacketSyncHazardZoneStrength.CODEC, SPacketSyncHazardZoneStrength::execute);
        registar.playToClient(SPacketSyncLevelHazards.TYPE, SPacketSyncLevelHazards.CODEC, SPacketSyncLevelHazards::execute);

        registar.playToClient(SPacketProspectOre.TYPE, SPacketProspectOre.CODEC, SPacketProspectOre::execute);
        registar.playToClient(SPacketProspectBedrockOre.TYPE, SPacketProspectBedrockOre.CODEC, SPacketProspectBedrockOre::execute);
        registar.playToClient(SPacketProspectBedrockFluid.TYPE, SPacketProspectBedrockFluid.CODEC, SPacketProspectBedrockFluid::execute);

        registar.playToClient(SPacketSendWorldID.TYPE, SPacketSendWorldID.CODEC, SPacketSendWorldID::execute);
        registar.playToClient(SPacketNotifyCapeChange.TYPE, SPacketNotifyCapeChange.CODEC, SPacketNotifyCapeChange::execute);

        registar.playBidirectional(SCPacketShareProspection.TYPE, SCPacketShareProspection.CODEC, SCPacketShareProspection::execute);

        // spotless:on        
    }
}
