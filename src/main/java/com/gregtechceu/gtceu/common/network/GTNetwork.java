package com.gregtechceu.gtceu.common.network;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.packets.*;
import com.gregtechceu.gtceu.common.network.packets.hazard.*;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class GTNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registar = event.registrar(GTCEu.MOD_ID);

        registar.playToServer(CPacketKeysPressed.TYPE, CPacketKeysPressed.CODEC, CPacketKeysPressed::execute);
        registar.playToClient(SPacketSyncOreVeins.TYPE, SPacketSyncOreVeins.CODEC, SPacketSyncOreVeins::execute);
        registar.playToClient(SPacketSyncFluidVeins.TYPE, SPacketSyncFluidVeins.CODEC, SPacketSyncFluidVeins::execute);
        registar.playToClient(SPacketSyncBedrockOreVeins.TYPE, SPacketSyncBedrockOreVeins.CODEC,
                SPacketSyncBedrockOreVeins::execute);

        registar.playToClient(SPacketAddHazardZone.TYPE, SPacketAddHazardZone.CODEC, SPacketAddHazardZone::execute);
        registar.playToClient(SPacketRemoveHazardZone.TYPE, SPacketRemoveHazardZone.CODEC,
                SPacketRemoveHazardZone::execute);
        registar.playToClient(SPacketSyncHazardZoneStrength.TYPE, SPacketSyncHazardZoneStrength.CODEC,
                SPacketSyncHazardZoneStrength::execute);
        registar.playToClient(SPacketSyncLevelHazards.TYPE, SPacketSyncLevelHazards.CODEC,
                SPacketSyncLevelHazards::execute);
    }
}
