package com.gregtechceu.gtceu.common.network;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.packets.*;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketAddHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketRemoveHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketSyncHazardZoneStrength;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketSyncLevelHazards;

import com.lowdragmc.lowdraglib.networking.INetworking;
import com.lowdragmc.lowdraglib.networking.forge.LDLNetworkingImpl;

public class GTNetwork {

    public static final INetworking NETWORK = LDLNetworkingImpl.createNetworking(GTCEu.id("network"), "0.0.1");

    public static void init() {
        NETWORK.registerC2S(CPacketKeysPressed.class);
        NETWORK.registerS2C(SPacketSyncTickCount.class);

        NETWORK.registerS2C(SPacketSyncOreVeins.class);
        NETWORK.registerS2C(SPacketSyncFluidVeins.class);
        NETWORK.registerS2C(SPacketSyncBedrockOreVeins.class);

        NETWORK.registerS2C(SPacketAddHazardZone.class);
        NETWORK.registerS2C(SPacketRemoveHazardZone.class);
        NETWORK.registerS2C(SPacketSyncHazardZoneStrength.class);
        NETWORK.registerS2C(SPacketSyncLevelHazards.class);
    }
}
