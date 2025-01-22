package com.gregtechceu.gtceu.common.network;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.packets.*;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketAddHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketRemoveHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketSyncHazardZoneStrength;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketSyncLevelHazards;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectBedrockFluid;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectBedrockOre;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectOre;

import com.lowdragmc.lowdraglib.networking.INetworking;
import com.lowdragmc.lowdraglib.networking.forge.LDLNetworkingImpl;

public class GTNetwork {

    public static final INetworking NETWORK = LDLNetworkingImpl.createNetworking(GTCEu.id("network"), "0.0.1");

    public static void init() {
        NETWORK.registerC2S(CPacketKeysPressed.class);
        NETWORK.registerS2C(SPacketSyncOreVeins.class);
        NETWORK.registerS2C(SPacketSyncFluidVeins.class);
        NETWORK.registerS2C(SPacketSyncBedrockOreVeins.class);

        NETWORK.registerS2C(SPacketAddHazardZone.class);
        NETWORK.registerS2C(SPacketRemoveHazardZone.class);
        NETWORK.registerS2C(SPacketSyncHazardZoneStrength.class);
        NETWORK.registerS2C(SPacketSyncLevelHazards.class);
        NETWORK.registerS2C(SPacketProspectOre.class);
        NETWORK.registerS2C(SPacketProspectBedrockFluid.class);
        NETWORK.registerS2C(SPacketProspectBedrockOre.class);
        NETWORK.registerS2C(SPacketSendWorldID.class);

        NETWORK.registerBoth(SCPacketShareProspection.class);
    }
}
