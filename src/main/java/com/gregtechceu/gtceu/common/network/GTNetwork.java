package com.gregtechceu.gtceu.common.network;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.packets.CPacketKeysPressed;
import com.lowdragmc.lowdraglib.networking.INetworking;
import com.lowdragmc.lowdraglib.networking.forge.LDLNetworkingImpl;

public class GTNetwork {

    public static final INetworking NETWORK = LDLNetworkingImpl.createNetworking(GTCEu.id("network"), "0.0.1");

    public static void init() {
        NETWORK.registerC2S(CPacketKeysPressed.class);
    }

}