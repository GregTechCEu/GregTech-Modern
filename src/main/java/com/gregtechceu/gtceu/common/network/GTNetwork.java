package com.gregtechceu.gtceu.common.network;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.packets.CPacketKeysPressed;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncBedrockOreVeins;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncFluidVeins;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncOreVeins;

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
    }
}
